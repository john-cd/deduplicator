package deduplicator.dao

import java.nio.file.Paths
import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}

import com.typesafe.scalalogging.LazyLogging
import deduplicator.hash.HashComponent
import org.h2.jdbcx.JdbcConnectionPool

trait DaoServiceComponent {
  this: DatabaseServiceComponent with HashComponent =>

  val daoService: DaoService

  //  case class Duplicate(id: Long, timeStamp: Date = new Date(), path: Path, hash: Array[Byte]){
  //    def toHexString: String = new String(Hex.encodeHex(hash))
  //  }

  class DaoService extends LazyLogging {

    private def provideConnection[R](f: Connection => R): R = { // loan pattern
      var ds: JdbcConnectionPool = null
      var conn: Connection = null
      try {
        val ds = JdbcConnectionPool.create(databaseService.connectionString, databaseService.username, databaseService.password)
        conn = ds.getConnection
        f(conn)
      } finally {
        if (conn != null)
          conn.close()
        if (ds != null)
          ds.dispose()
      }
    }

    def readHashes: Iterator[Hash] = provideConnection(conn => readHashes(conn))

    private def readHashes(connection: Connection): Iterator[Hash] = {
      var preparedStatement: PreparedStatement = null
      try {
        logger.info("Starting readHashes")
        preparedStatement = connection.prepareStatement(
          "SELECT h.id, h.ts, h.filepath, h.hash FROM hashes AS h"
        )
        val rs: ResultSet = preparedStatement.executeQuery()
        Iterator.continually((rs.next(), rs)).takeWhile(_._1).map {
          case (_, row) => {
            val id = Some(row.getLong(1))
            val ts = row.getTimestamp(2)
            val filePath = row.getString(3)
            val hash = row.getBytes(4)
            Hash(id, ts.getTime, Paths.get(filePath), hash)
          }
        }
      }
      finally {
        if (preparedStatement != null)
          preparedStatement.close()
      }
    } // readHashes

    def insertHashes(hashes: Iterator[Hash]): Unit = provideConnection(conn => insertHashes(conn, hashes))

    private def insertHashes(connection: Connection, hashes: Iterator[Hash]): Unit = {
      var preparedStatement: PreparedStatement = null
      try {
        logger.info("Starting insertHashes")
        preparedStatement = connection.prepareStatement(
          "INSERT INTO hashes(ts, filepath, hash) VALUES (?, ?, ?)"
        )
        hashes.zipWithIndex.foreach { res =>
          val (hash, index) = res
          preparedStatement.setTimestamp(1, new Timestamp(hash.timeStamp))
          preparedStatement.setString(2, hash.path.toString) // should be already absolute paths
          preparedStatement.setBytes(3, hash.hash)
          preparedStatement.addBatch()
          if (index % 100 == 0) preparedStatement.executeBatch()
        }
        preparedStatement.executeBatch()    // TODO error reporting
      } finally {
        if (preparedStatement != null)
          preparedStatement.close()
        logger.info("Stopping readHashes")
      }
    } // insertHash


    def findDuplicates: Unit = provideConnection(conn => findDuplicates(conn))

    private def findDuplicates(connection: Connection): Unit = {
      var statement: PreparedStatement = null
      try {
        logger.info("Starting findDuplicates")
        val sql =
          """
            |SELECT h.id AS id, h.ts AS ts, h.filepath AS filepath, h.hash AS hash, hwd.file_count
            |FROM hashes AS h
            |INNER JOIN
            |( SELECT h2.hash, COUNT(DISTINCT h2.id) AS file_count FROM hashes AS h2
            |  GROUP BY h2.hash HAVING COUNT(DISTINCT h2.id) >= 2 ) AS hwd
            |ON h.hash = hwd.hash
            |ORDER BY file_count DESC, h.hash, h.filepath
          """.stripMargin
        statement = connection.prepareStatement(sql)
        val rs: ResultSet = statement.executeQuery()
        Iterator.continually((rs.next(), rs)).takeWhile(_._1).map {
          case (_, row) =>
            val id = row.getLong(1)
            val ts = row.getTimestamp(2)
            val filepath = row.getString(3)
            val hash = row.getBytes(4)
            val fileCount = row.getInt(5)
            logger.info(s"$fileCount duplicates: $id ${ts.toLocalDateTime} $filepath")  // TODO return something useful
        }
      }
      finally {
        if (statement != null) {
          statement.close()
        }
        logger.info("Stopping findDuplicates")
      }
    }
  } // DaoService
} // DaoServiceComponent


// Templates:

// private def executeSelect(preparedStatement: PreparedStatement): ResultSet =
//      try {
//        preparedStatement.executeQuery() // Returns one ResultSet object.
//      } finally {
//        if (preparedStatement != null)
//            preparedStatement.close()
//      }
//
//    private def readResultSet[T](rs: ResultSet)(f: ResultSet => T): Iterator[T] =
//      Iterator.continually((rs.next(), rs)).takeWhile(_._1).map {
//        case (_, row) =>
//          f(row)
//      }