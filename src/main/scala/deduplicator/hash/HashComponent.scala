package deduplicator.hash

import deduplicator.io.FileAsyncIO

import scala.util.control.NonFatal
import java.nio.file.{Files, Paths}
import java.security.MessageDigest

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.codec.binary.Hex

import scala.util.{Failure, Success}

// the following is equivalent to `implicit val ec = ExecutionContext.global`
import scala.concurrent._
import ExecutionContext.Implicits.global

trait HashComponent {
  val hashService: HashService
}

object HashService {
  private lazy val md = MessageDigest.getInstance("MD5")

  def apply() = new HashService
}

class HashService extends LazyLogging {

  import HashService._

  def checksum(filePath: String): Option[String] = {
    require(filePath != null)
    try {
      val path = Paths.get(filePath).normalize
      if (!Files.isReadable(path) || Files.isDirectory(path)) // readable = existing and accessible
        None
      else {
        md.reset()

        // read block after block of data
        // note: tailrec is not required, since Futures are asynchronous
        def recurse(position: Long): Unit = {
          FileAsyncIO.read(path, position).onComplete {
            case Success(arr) => if (arr.length > 0) {
              md.update(arr);
              recurse(position + arr.length) // update the message digest and process the next block
            }
            case Failure(exc) =>
          }
        }

        //          var pos = 0L
        //          while(FileAsyncIO.read(path, pos) )


        Some(new String(Hex.encodeHex(md.digest()))) // or: md.digest.map("%02x".format(_)).mkString
        // note: do not intern that String!
      }
    }
    catch {
      case NonFatal(e) => {
        logger.info(s"checksum(stream: InputStream) throws ${e}");
        throw e
      }
    }

  }
}



