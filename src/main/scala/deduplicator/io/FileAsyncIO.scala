package deduplicator.io

// derived from: https://gist.githubusercontent.com/tovbinm/f73849aff169d1ebeb97/raw/9d5f7e6ab4894d357955c1c34b5aa7d6865163d2/FileAsyncIO.scala
// see also: https://github.com/alexandru/shifter/blob/master/core/src/main/scala/shifter/io/AsyncFileChannel.scala

import com.typesafe.scalalogging.LazyLogging
import java.io.IOException
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

import java.nio.file._
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousFileChannel, CompletionHandler}
import java.nio.file.StandardOpenOption._

/**
  * File asynchronous IO wrapper around java.nio.file for scala Futures
  */
object FileAsyncIO extends LazyLogging {

  private val defaultBufferSize = 8192

  /**
    * Asynchronously read (a portion of) a file, starting at a given position.
    *
    * @param filePath    file path
    * @param position    file position at which the transfer begins; must be non-negative
    * @param desiredSize desired size of the Block to return; must be positive or set to zero for automatic sizing
    * @param ec          execution context
    * @return file contents as bytes
    */
  def read(filePath: Path, position: Long, desiredSize: Int = 0)(implicit ec: ExecutionContext): Future[Array[Byte]] = {
    require(position >= 0L)
    require(desiredSize >= 0)
    val p = Promise[Array[Byte]]()
    try {
      val channel = AsynchronousFileChannel.open(filePath, READ)

      val fileSize = channel.size()
      val remainingBytes: Long = math.max(fileSize - position, 0L)
      if (fileSize == 0)
        p.failure(new Exception("empty file"))
      if (remainingBytes <= 0)
        p.failure(new Exception("position past the end of the file - no bytes to read!"))
      else {
        val maxBufferSize = math.min(remainingBytes.toInt, Int.MaxValue)
        val buffer = ByteBuffer.allocate(math.min(if (desiredSize > 0) desiredSize else defaultBufferSize, maxBufferSize))
        channel.read(buffer, position, buffer, onComplete(channel, p))

      }
    }
    catch {
      case t: Throwable => p.failure(t)
    }
    p.future
  }


  // Original code:
  //  def read(file: String)(implicit ec: ExecutionContext): Future[Array[Byte]] = {
  //    val p = Promise[Array[Byte]]()
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


  private def closeSafely(channel: AsynchronousFileChannel) =
    try {
      channel.close()
    } catch {
      case e: IOException => logger.warn(s"closeSafely: channel.close() threw $e") // swallows exception
    }

  private def onComplete(channel: AsynchronousFileChannel, p: Promise[Array[Byte]]) = new CompletionHandler[Integer, ByteBuffer]() {
    def completed(bytesRead: Integer, buffer: ByteBuffer): Unit = {
      logger.info(s"FileAsyncIO - bytesRead: $bytesRead")
      p.complete(Try {
        buffer.array()
      })
      closeSafely(channel)
    }

    def failed(t: Throwable, buffer: ByteBuffer): Unit = {
      p.failure(t)
      closeSafely(channel)
    }
  }

}