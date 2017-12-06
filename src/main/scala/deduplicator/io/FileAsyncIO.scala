package deduplicator.io

// derived from: https://gist.githubusercontent.com/tovbinm/f73849aff169d1ebeb97/raw/9d5f7e6ab4894d357955c1c34b5aa7d6865163d2/FileAsyncIO.scala
// see also: https://github.com/alexandru/shifter/blob/master/core/src/main/scala/shifter/io/AsyncFileChannel.scala

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousFileChannel, CompletionHandler}
import java.nio.file.StandardOpenOption._
import java.nio.file._

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

object Block {
  def apply(): Block = new Block(Array[Byte](), -1L)
}

case class Block(data: Array[Byte], nextPosition: Long) {
  require(nextPosition >= -1L)
} // nextPosition = -1 if end of file

/**
  * File asynchronous IO wrapper around java.nio.file for scala Futures
  */
object FileAsyncIO extends LazyLogging {

  private final val defaultBufferSize = 8 * 1024

  /**
    * Asynchronously read (a portion of) a file, starting at a given position.
    *
    * @param filePath    file path
    * @param position    file position at which the transfer begins; must be non-negative
    * @param desiredSize desired size of the Block to return; must be positive or set to zero for automatic sizing
    * @param ec          execution context (implicit)
    * @return file contents as bytes
    */
  def read(filePath: Path, position: Long, desiredSize: Int = 0)(implicit ec: ExecutionContext): Future[Block] = {
    require(position >= 0L)
    require(desiredSize >= 0)
    val p = Promise[Block]()
    try {
      val channel = AsynchronousFileChannel.open(filePath, READ)
      val fileSize = channel.size()
      val remainingBytes: Long = math.max(fileSize - position, 0L)
      if (fileSize == 0)
        p.success(Block()) // "empty file"  // TODO test
      if (remainingBytes <= 0)
        p.success(Block()) // "position past the end of the file - no bytes to read!"  // TODO test
      else {
        val maxBufferSize = math.min(remainingBytes.toInt, Int.MaxValue) // files may be of length > 2^32  // TODO test
        val buffer = ByteBuffer.allocate(math.min(if (desiredSize > 0) desiredSize else defaultBufferSize, maxBufferSize))
        channel.read(buffer, position, buffer, onComplete(channel, position, p))
      }
    }
    catch {
      case t: Throwable => p.failure(t)
    }
    p.future
  }

  private def closeSafely(channel: AsynchronousFileChannel) =
    try {
      channel.close()
    } catch {
      case e: IOException => logger.warn(s"channel.close() failed: ", e) // swallows exception
    }

  private def onComplete(channel: AsynchronousFileChannel, initialPosition: Long, p: Promise[Block]) = new CompletionHandler[Integer, ByteBuffer]() {
    def completed(bytesRead: Integer, buffer: ByteBuffer): Unit = {
      logger.info(s"Bytes read: $bytesRead")
      if (bytesRead == -1L)
        p.failure(new IOException("The given position was greater than or equal to the file's size at the time that the read is attempted."))
      else {
        val newPosition = if (initialPosition + bytesRead < channel.size()) initialPosition + bytesRead else -1L // -1 if there is no more data because the end of the file has been reached.
        p.complete(Try {
          new Block(buffer.array(), newPosition)
        })
        closeSafely(channel)
      }
    }

    def failed(t: Throwable, buffer: ByteBuffer): Unit = {
      p.failure(t)
      closeSafely(channel)
    }
  }

}


// Original code:
//  def read(file: String)(implicit ec: ExecutionContext): Future[Block] = {
//    val p = Promise[Block]()
//    try {
//      val channel = AsynchronousFileChannel.open(Paths.get(file), READ)
//      val buffer = ByteBuffer.allocate(channel.size().toInt)
//      channel.read(buffer, 0L, buffer, onComplete(channel, p))
//    }
//    catch {
//      case t: Throwable => p.failure(t)
//    }
//    p.future
//  }