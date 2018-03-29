package deduplicator.dao

import java.nio.file.{Path, Paths}
import java.sql._

import com.typesafe.scalalogging.LazyLogging
import deduplicator.hash.HashComponent
import org.apache.commons.codec.binary.Hex
import org.h2.jdbcx.JdbcConnectionPool

import scala.util._

trait DaoServiceComponent {
  this: DatabaseServiceComponent with HashComponent =>

  val daoService: DaoService

  //  case class Duplicate(id: Long, timeStamp: Date = new Date(), path: Path, hash: Array[Byte]){
  //    def toHexString: String = new String(Hex.encodeHex(hash))
  //  }

  class DaoService extends LazyLogging {

    private var _ds: JdbcConnectionPool = _
    private var _conn: Connection = _

    /** Loan pattern for JDBC connection
      *
      * Don't use when returning an iterator...
      *
      * @param f the function to wrap / provide a connection to
      * @tparam R return type of f
      * @return the return value of f
      */
    private def provideConnection[R](f: Connection => R): R = { // loan pattern
      try {
        if (_ds == null)
          _ds = JdbcConnectionPool.create(databaseService.connectionString, databaseService.username, databaseService.password)
        if ((_conn == null) || _conn.isClosed)
          _conn = _ds.getConnection
        f(_conn)
      } finally {

        if (!_conn.isClosed) {
          if (!_conn.getAutoCommit) _conn.commit()
          _conn.close()
          _conn = null // not strictly necessary but GC friendly
        }
        if (_ds != null) {
          _ds.dispose()
          _ds = null // no way to know if object is disposed - null the var instead
        }
      }
    }

    def insertHashes(hashes: Iterator[Hash]): Traversable[Try[String]] = provideConnection(conn => insertHashes(conn, hashes))

    private def insertHashes(connection: Connection, hashes: Iterator[Hash]): Traversable[Try[String]] = {
      var preparedStatement: PreparedStatement = null
      try {
        logger.info("Starting insertHashes")
        preparedStatement = connection.prepareStatement(
          "INSERT INTO hashes(ts, filepath, hash) VALUES (?, ?, ?)"
        )
        hashes.foreach { hash =>
          preparedStatement.setTimestamp(1, new Timestamp(hash.timeStamp))
          preparedStatement.setString(2, hash.path.toString) // should be already absolute paths
          preparedStatement.setBytes(3, hash.hash)
          preparedStatement.addBatch()
        }
        preparedStatement.executeBatch() // returns int[] which are > 0 if insert successful
          .collect {
          case Statement.SUCCESS_NO_INFO => Success("SUCCESS_NO_INFO")
          case Statement.EXECUTE_FAILED => Failure(new Exception("EXECUTE_FAILED"))
          case n if n > 0 => Success(s"Inserted $n row(s)")
        }
      } finally {
        if (preparedStatement != null)
          preparedStatement.close()
        logger.info("Stopping insertHashes")
      }
    } // insertHash


    case class Dupes(id: Long, timeStamp: Timestamp, path: Path, hash: scala.Array[Byte], fileCount: Int) {
      def toHexString: String = new String(Hex.encodeHex(hash))
    }

    def findDuplicates: Iterator[Dupes] = {
      logger.info("Starting findDuplicates")
      val ds: JdbcConnectionPool = JdbcConnectionPool.create(databaseService.connectionString, databaseService.username, databaseService.password)
      val conn: Connection = ds.getConnection
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
      val preparedStatement: PreparedStatement = conn.prepareStatement(sql)
      val rs: ResultSet = preparedStatement.executeQuery()
      val it = Iterator.continually((rs.next(), rs)).takeWhile(_._1).map {
        case (_, row) =>
          val id = row.getLong(1)
          val ts = row.getTimestamp(2)
          val filepath = row.getString(3)
          val hash = row.getBytes(4)
          val fileCount = row.getInt(5)
          Dupes(id, ts, Paths.get(filepath), hash, fileCount)
      }

      it.map { x =>
        if (!it.hasNext) {
          // it's the last element
          if (!preparedStatement.isClosed) preparedStatement.close()
          if (!conn.isClosed) conn.close()
          ds.dispose()
          logger.info("Stopping findDuplicates")
        }
        x
      }
    } // findDuplicates

    def readHashes: Iterator[Hash] = {
      logger.info("Starting readHashes")
      val ds: JdbcConnectionPool = JdbcConnectionPool.create(databaseService.connectionString, databaseService.username, databaseService.password)
      val conn: Connection = ds.getConnection
      val preparedStatement: PreparedStatement = conn.prepareStatement(
        "SELECT h.id, h.ts, h.filepath, h.hash FROM hashes AS h"
      )
      val rs: ResultSet = preparedStatement.executeQuery()
      val it = Iterator.continually((rs.next(), rs)).takeWhile(_._1).map {
        case (_, row) =>
          val id = Some(row.getLong(1))
          val ts = row.getTimestamp(2)
          val filePath = row.getString(3)
          val hash = row.getBytes(4)
          Hash(id, ts.getTime, Paths.get(filePath), hash)
      }

      it.map { x =>
        if (!it.hasNext) {
          // it's the last element
          if (!preparedStatement.isClosed) preparedStatement.close()
          if (!conn.isClosed) conn.close()
          ds.dispose()
          logger.info("Stopping readHashes")
        }
        x
      }
    } // readHashes

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