package deduplicator.io

import java.io.IOException
import java.nio.file.FileVisitOption._
import java.nio.file.FileVisitResult._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{Files, Path, _}
import java.util
import scala.collection.JavaConverters._

import com.typesafe.scalalogging.LazyLogging

trait IOServiceComponent {

  val ioService: IOService

  case class FilesAndDirs(val files: List[String])

  class IOService {

    // Files.walk(start, FileVisitOption.FOLLOW_LINKS).iterator().asScala
    // see also:
    // https://docs.oracle.com/javase/7/docs/api/java/nio/file/DirectoryStream.html
//    def getAllFiles(start: Path): Iterator[String] = {
//
//
//
//    }

  }

}





/*  Example code with java.io
	
    import java.io.File
	
    def getAllFilesWithExtension(basePath: String, extension: String): List[String] = {
      val dir = new File(basePath)
      if (dir.exists() && dir.isDirectory) {
        dir.listFiles().filter(f => f.isFile && f.getPath.toLowerCase.endsWith(s".${extension}")).map {
          case f => f.getAbsolutePath
        }.toList
      } else {
        List.empty
      }
    }
	
	def getAllFiles(dir: String): List[File] = {
		val d = new File(dir)
		if (d.exists && d.isDirectory) {
			d.listFiles.filter(_.isFile).toList
		} else {
			List[File]()
        }
	}
	
	def getListOfSubDirectories(dir: File): List[String] =
		dir.listFiles
		   .filter(_.isDirectory)
		   .map(_.getName)
		   .toList
*/
