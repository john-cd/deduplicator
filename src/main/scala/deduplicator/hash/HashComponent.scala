package deduplicator.hash

import java.io.IOException
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest

import com.typesafe.scalalogging.LazyLogging
import deduplicator.io.{Block, IOServiceComponent}
import org.apache.commons.codec.binary.Hex

import scala.concurrent._
import scala.util._
import scala.util.control.NonFatal


trait HashComponent {
  this: IOServiceComponent =>

  val hashService: HashService

  case class Hash(id: Option[Long] = None, timeStamp: Long = System.currentTimeMillis(), path: Path, hash: Array[Byte]){

    def toHexString: String = new String(Hex.encodeHex(hash))
    // or: md.digest.map("%02x".format(_)).mkString
    // note: do not intern that String!

  }

  class HashService(implicit ec: ExecutionContext) extends LazyLogging {

    def checksum(filePath: String): Future[Hash] = checksum(Paths.get(filePath).normalize)

    /**
      *
      * @param path
      * @return
      */
    def checksum(path: Path): Future[Hash] = {
      require(path != null)
      logger.info(s"Starting checksum of ${path}")
      try {
        val md = MessageDigest.getInstance("MD5")
        if (!Files.isReadable(path)) // readable = existing and accessible
          Future.failed(new IOException(s"File is not readable: $path"))
        else if (Files.isDirectory(path))
          Future.failed(new Exception(s"Can't hash a directory: $path"))
        else {
          md.reset()

          // read a block of data and update the message digest
          def readFrom(position: Long): Future[Block] = fileAsyncIO.read(path, position)
            .andThen { case Success(blk: Block) => md.update(blk.data) }

          def recurse(position: Long): Future[Block] = readFrom(position).flatMap {
            blk => if (blk.nextPosition != -1L) recurse(blk.nextPosition) else Future.successful(blk)
          }

          recurse(0L).map { ignore => Hash(path = path, hash = md.digest()) }
        } // else
      } // try
      catch {
        case NonFatal(e) =>
          logger.info(s"checksum() throws $e")
          throw e
      }
      finally {
        logger.info(s"Completed checksum of $path")
      }
    }
  } // HashService

}


