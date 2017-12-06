package deduplicator.hash

import java.io.IOException
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest

import com.typesafe.scalalogging.LazyLogging
import deduplicator.io.{Block, FileAsyncIO}
import org.apache.commons.codec.binary.Hex

import scala.concurrent.Future
import scala.util.Success
import scala.util.control.NonFatal

// the following is equivalent to `implicit val ec = ExecutionContext.global`
import scala.concurrent.ExecutionContext.Implicits.global


trait HashComponent {
  val hashService: HashService
}

object HashService {
  def apply() = new HashService
}

class HashService extends LazyLogging {

  def checksum(filePath: String): Future[String] = checksum(Paths.get(filePath).normalize)

  def checksum(path: Path): Future[String] = {
    require(path != null)
    try {
      val md = MessageDigest.getInstance("MD5")
      if (!Files.isReadable(path)) // readable = existing and accessible
        Future.failed(new IOException(s"File is not readable: $path"))
      else if (Files.isDirectory(path))
        Future.failed(new Exception(s"Can't hash a directory: $path"))
      else {
        md.reset()

        // read a block of data and update the message digest
        def readFrom(position: Long): Future[Block] = FileAsyncIO.read(path, position)
          .andThen { case Success(blk) => md.update(blk.data) }

        def recurse(position: Long): Future[Block] = readFrom(position).flatMap {
          blk => if (blk.nextPosition != -1L) recurse(blk.nextPosition) else Future.successful(blk)
        }

        recurse(0L).map { ignore => new String(Hex.encodeHex(md.digest())) }

        // or: md.digest.map("%02x".format(_)).mkString
        // note: do not intern that String!
      } // else
    } // try
    catch {
      case NonFatal(e) =>
        logger.info(s"checksum() throws $e")
        throw e
    }
  }
}



