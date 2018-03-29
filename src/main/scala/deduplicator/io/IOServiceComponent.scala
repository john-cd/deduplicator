package deduplicator.io

trait IOServiceComponent {

  val fileSystemWalker: FileSystemWalker
  val fileAsyncIO: FileAsyncIO

}