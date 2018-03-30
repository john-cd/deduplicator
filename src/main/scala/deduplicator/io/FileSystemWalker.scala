package deduplicator.io

import java.nio.file._

import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.control.NonFatal


class FileSystemWalker extends LazyLogging {

  private def _walk(start: Path, recurse: Boolean, maxDepth: Int = Int.MaxValue): Iterator[Path] = {
    try {
      if (Files.isRegularFile(start, LinkOption.NOFOLLOW_LINKS)) {
        Iterator(start)
      }
      else if (recurse && (maxDepth > 0) && Files.isDirectory(start)) {   // TODO check maxDepth
        val ds = Files.newDirectoryStream(start)
        ds.iterator().asScala
          .flatMap(sub => _walk(sub, recurse, maxDepth - 1))
      }
      else {
        Iterator.empty  // special file, directory in non-recurse mode
      }
    }
    catch {
      case NonFatal(e) =>
        logger.error(s"FileSystemWalker.walk failure: ${e.getClass}")   // swallows exceptions
        Iterator.empty
    }
  }

  def walk[R](start: Path, f: Path => R, recurse: Boolean, maxDepth: Int = Int.MaxValue): Iterator[R] = {
    require(maxDepth > 0) // Int.Maxvalue for no limit
    _walk(start, recurse, maxDepth)
      .filter(p => Files.isRegularFile(p))    // files only, not directories
      .map(_.toAbsolutePath)
      .map(f)
  }
}


// OR
//    import java.io.IOException
//    import java.nio.file.DirectoryStream
//    import java.nio.file.Files
//    val filter = new DirectoryStream.Filter[Path]() {
//      @throws[IOException]
//      override def accept(file: Path): Boolean =  file.toFile.isFile
//    }
//    Files.newDirectoryStream(start, filter)
// OR
// Files.list(start).iterator().asScala.map(f)
// OR
// val pred = new BiPredicate[Path, BasicFileAttributes] { def test(p: Path, bfa: BasicFileAttributes): Boolean = { bfa.isRegularFile} }
// Files.find(start, maxDepth, )


// Older code using walkFileTree
//     private val opts: util.EnumSet[FileVisitOption] = util.EnumSet.of(FOLLOW_LINKS)
//  /** Walk a Directory Tree
//    *
//    * @param start   directory path to start with
//    * @param f       lambda that will be run for every file found in the director(ies)
//    * @param recurse if true, recurse through all sub-directories
//    * @return
//    * @see https://docs.oracle.com/javase/tutorial/essential/io/walk.html
//    *
//    *      Alternatives:
//    * Files.walk(start, FileVisitOption.FOLLOW_LINKS).iterator().asScala
//    *      https://docs.oracle.com/javase/7/docs/api/java/nio/file/DirectoryStream.html
//    */
//  def walk2(start: Path, f: (Path, BasicFileAttributes) => Unit, recurse: Boolean) = {
//
//    if (!Files.isDirectory(start)) {   // single file
//      f(start, Files.readAttributes(start, classOf[BasicFileAttributes]))
//    }
//    else
//      Files.walkFileTree(start, opts, Int.MaxValue, new Visitor(start, f, recurse))
//  }
//
//  private class Visitor[R](start: Path, f: (Path, BasicFileAttributes) => R, recurse: Boolean)
//    extends SimpleFileVisitor[Path] {
//
//    //  Invoked for a directory before entries in the directory are visited.
//    //  If this method returns CONTINUE, then entries in the directory are visited.
//    // If this method returns SKIP_SUBTREE or SKIP_SIBLINGS then entries in the directory (and any descendants) will not be visited.
//    override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
//      if ((dir == start) || recurse) CONTINUE else SKIP_SUBTREE
//    }
//
//    override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
//      // Print each directory visited.
//      logger.debug("Directory: {}", dir)
//      CONTINUE
//    }
//
//    // Invoked for a file in a directory.
//    override def visitFile(file: Path, attr: BasicFileAttributes): FileVisitResult = {
//      if (attr.isSymbolicLink) logger.debug("Symbolic link: {}", file)
//      else if (attr.isRegularFile) logger.debug("Regular file: {}", file)
//      else logger.debug("Other: {}", file)
//      logger.debug(s"File size: ${attr.size} bytes")
//      f(file, attr)
//      CONTINUE
//    }
//
//    // If there is some error accessing
//    // the file, let the user know.
//    // If you don't override this method
//    // and an error occurs, an IOException
//    // is thrown.
//    override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
//      exc match {
//        case e: FileSystemLoopException => logger.warn(s"visited symbolic link: cycle detected: $file")
//        case NonFatal(other) => logger.warn(s"$other")
//      }
//      CONTINUE
//    }
//  } // visitor class
