package deduplicator.io

import java.nio.file._
import scala.collection.JavaConverters._

trait IOServiceComponent {

  val ioService: IOService


  case class FilesAndDirs(val files: List[String])

  class IOService {

    //    def getAllFiles(fileOrDirPath: String): Iterator[String] = {
    //
    //      Files.walk(dir).iterator().asScala.filter(Files.isRegularFile(_)).foreach(println)
    //
    //    } // see also: https://docs.oracle.com/javase/7/docs/api/java/nio/file/DirectoryStream.html    https://docs.oracle.com/javase/tutorial/essential/io/walk.html
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
