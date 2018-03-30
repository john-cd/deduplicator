package deduplicator.io

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

//noinspection SpellCheckingInspection
class IOTest {

  //val currentDirectory = new java.io.File(".").getCanonicalPath
  //println(currentDirectory)

  def printAttr(path: Path, attr: BasicFileAttributes): Unit = {
    println(s"$path ->")
    //      println("creationTime     = " + attr.creationTime)
    //      println("lastAccessTime   = " + attr.lastAccessTime)
    //      println("lastModifiedTime = " + attr.lastModifiedTime)
    //      println("isDirectory      = " + attr.isDirectory)
    //      println("isOther          = " + attr.isOther)
    //      println("isRegularFile    = " + attr.isRegularFile)
    //      println("isSymbolicLink   = " + attr.isSymbolicLink)
    //      println("size             = " + attr.size)
  }
}
