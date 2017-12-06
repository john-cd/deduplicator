package deduplicator.io.filewatch

import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import scala.collection.mutable

/** Wrapper around WatchService - monitor changes to the file system
  * - register a given directory, optionally recursively
  * - poll the
  *
  * val dir = Paths.get(...)
  * val wd = new WatchDir()
  * wd.register(dir, true)
  *
  * see also: [[https://docs.oracle.com/javase/tutorial/essential/io/notification.html]]
  */
object WatchDir {
  def apply(created: Path => Unit, modified: Path => Unit, deleted: Path => Unit): WatchDir = new WatchDir(created, modified, deleted)
}

@throws[IOException]
class WatchDir(created: Path => Unit, modified: Path => Unit, deleted: Path => Unit) extends LazyLogging {

  private lazy val watcher: WatchService = FileSystems.getDefault.newWatchService
  private lazy val keys = mutable.Map[WatchKey, Path]()

  /**
    * Register the given directory, and all its sub-directories if recursive = true.
    */
  @throws[IOException]
  def register(start: Path, recursive: Boolean = false): Unit = {
    if (recursive) {
      logger.info(s"Scanning $start ...")
      registerAll(start, trace = false)
      logger.info("Scanning done.")
    }
    else registerOne(start, trace = false)
    // trace = true by default after initial registration
  }

  /**
    * Register the given directory, and all its sub-directories, with the WatchService.
    */
  @throws[IOException]
  private def registerAll(start: Path, trace: Boolean = true): Unit = { // register directory and sub-directories
    registerOne(start)
    Files.walkFileTree(start, new SimpleFileVisitor[Path]() {
      @throws[IOException]
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        registerOne(dir, trace)
        FileVisitResult.CONTINUE
      }
    })
  }

  /**
    * Register the given directory with the WatchService
    */
  @throws[IOException]
  private def registerOne(dir: Path, trace: Boolean = true): Unit = {
    val key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY)
    if (trace)
      keys.get(key) match {
        case Some(prev) if dir != prev => logger.info(s"Registration update: $prev -> $dir")
        case None => logger.info(s"New registration: $dir")
      }
    keys(key) = dir
  }

  def unregisterOne(dir: Path): Unit = {
    keys.filter(_._2 == dir).foreach(key_path => {
      val k = key_path._1;
      k.cancel();
      keys -= k
    })
  }

  // Call watcher.close() when done
  @throws[IOException]
  def close(): Unit = {
    watcher.close()
  }

  // for cleaner resource management, consider https://github.com/jsuereth/scala-arm


  // ---- polling --------------------------------------------------

  /** Poll the WatchService queue, read any available key and associated events
    * and call the appropriate created/modified/deleted lambdas
    *
    * @return   true if a queued key was found, false otherwise.
    *
    *           poll() should be called repeatedly e.g. by a timer / Akka scheduler
    *           val t = new java.util.Timer()
    *           val task = new java.util.TimerTask {
    *           def run() = while (wd.poll()) {}      // true = key found; more may be queued
    *           }
    *           t.schedule(task, 1000L, 1000L)
    *           task.cancel()
    *           // OR
    *           val system = ActorSystem("mySystem", config)
    *           ...now with system in current scope:
    *           import system.dispatcher
    *           system.scheduler.schedule(10 seconds, 1 seconds) {
    *           while (wd.poll()) {}
    *           }
    */
  def poll(): Boolean = {
    try {
      // Returns a queued key, if available. Returns immediately with a null value, if unavailable.
      val key: WatchKey = watcher.poll()
      if (key == null)
        return false
      else {
        // Retrieves and removes all pending events for this watch key, returning a List of the events that were retrieved.
        // Note that this method does not wait if there are no events pending.
        key.pollEvents().asScala.foreach(e => {
          val event = e.asInstanceOf[WatchEvent[Path]]

          // watchable() returns the object / Path for which this watch key was created.
          val registeredpath = key.watchable().asInstanceOf[Path]

          //  the context is a Path that is the relative path between the directory registered with the watch service,
          // and the entry that is created, deleted, or modified.
          val relativePath: Path = event.context()

          // Resolve the filename against the directory.
          // If the filename is "test" and the directory is "foo",
          // the resolved name is "test/foo".
          val path = registeredpath.resolve(relativePath)

          event.kind() match {

            // an entry has been created in the registered directory or renamed into the directory.
            case StandardWatchEventKinds.ENTRY_CREATE =>
              logger.info(s"Entry created: $path")
              // TODO provide option to register or not, recursively or not
              if (Files.isDirectory(path)) {
                registerAll(path)
              }
              created(path) //  example: notifyActor ! Created(path)

            // an entry is deleted or renamed out of the registered directory.
            case StandardWatchEventKinds.ENTRY_DELETE => // Directory entry deleted.
              logger.info(s"Entry deleted: $path")
              deleted(path) // example: notifyActor ! Deleted(path)

            // an entry in the registered directory has been modified.
            case StandardWatchEventKinds.ENTRY_MODIFY =>
              logger.info(s"Entry modified: $path")
              modified(path)

            // An OVERFLOW event can occur if events are lost or discarded.
            case x =>
              logger.warn(s"Unknown event: $x")
          }
        }) // foreach

        // Reset the key -- this step is critical if you want to
        // receive further watch events.
        val valid: Boolean = key.reset()
        // A watch key is valid upon creation and remains until it is cancelled, or its watch service is closed.
        if (!valid) {
          logger.warn(s"Key not valid: $key")
        }
        return true
      } // else
    }
    catch {
      case e: ClosedWatchServiceException =>
        logger.warn("ClosedWatchServiceException")
        return false
    }
  } // poll
} // class