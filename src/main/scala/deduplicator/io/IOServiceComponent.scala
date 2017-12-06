package deduplicator.io

trait IOServiceComponent {

  val ioService: IOService

  class IOService {

    // TODO expose FileSystemWalker.walk()

  }
}


/*  Older example code using java.io
	
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
