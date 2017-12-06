package deduplicator.io

import java.io.IOException
import java.nio.file.FileVisitOption.FOLLOW_LINKS
import java.nio.file.FileVisitResult.{CONTINUE, SKIP_SUBTREE}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util

import com.typesafe.scalalogging.LazyLogging

import scala.util.control._

object FileSystemWalker extends LazyLogging {

  private val opts: util.EnumSet[FileVisitOption] = util.EnumSet.of(FOLLOW_LINKS)

  /** Walk a Directory Tree
    *
    * @param start   directory path to start with
    * @param f       lambda that will be run for every file found in the director(ies)
    * @param recurse if true, recurse through all sub-directories
    * @return
    */
  // see also: https://docs.oracle.com/javase/tutorial/essential/io/walk.html
  // alternatives:
  // Files.walk(start, FileVisitOption.FOLLOW_LINKS).iterator().asScala
  // https://docs.oracle.com/javase/7/docs/api/java/nio/file/DirectoryStream.html
  def walk(start: Path, f: (Path, BasicFileAttributes) => Unit, recurse: Boolean): Unit = {

    if (!Files.isDirectory(start)) {
      f(start, Files.readAttributes(start, classOf[BasicFileAttributes]))
    }
    else
      Files.walkFileTree(start, opts, Int.MaxValue, new Visitor(start, f, recurse))
  }

  // TODO what if f returns Future
  private class Visitor[T](start: Path, f: (Path, BasicFileAttributes) => Unit, recurse: Boolean) extends SimpleFileVisitor[Path] {

    //  Invoked for a directory before entries in the directory are visited.
    //  If this method returns CONTINUE, then entries in the directory are visited.
    // If this method returns SKIP_SUBTREE or SKIP_SIBLINGS then entries in the directory (and any descendants) will not be visited.
    override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
      if ((dir == start) || recurse) CONTINUE else SKIP_SUBTREE
    }

    override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
      // Print each directory visited.
      logger.debug("Directory: {}", dir)
      CONTINUE
    }

    // Invoked for a file in a directory.
    override def visitFile(file: Path, attr: BasicFileAttributes): FileVisitResult = {
      if (attr.isSymbolicLink) logger.debug("Symbolic link: {}", file)
      else if (attr.isRegularFile) logger.debug("Regular file: {}", file)
      else logger.debug("Other: {}", file)
      logger.debug("File size: " + attr.size + " bytes")
      f(file, attr)
      CONTINUE
    }

    // If there is some error accessing
    // the file, let the user know.
    // If you don't override this method
    // and an error occurs, an IOException
    // is thrown.
    override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
      exc match {
        case e: FileSystemLoopException => logger.warn(s"visited symbolic link: cycle detected: $file")
        case NonFatal(other) => logger.warn(s"$other")
      }
      CONTINUE
    }
  } // visitor class

} // class
