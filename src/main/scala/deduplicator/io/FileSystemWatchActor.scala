package deduplicator.io

import java.nio.file.Path
import scala.concurrent.duration._
import akka.actor.Timers
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

/**
  * Non-blocking actor facade for WatchService
  * Monitors changes to the file system
  */
object FileSystemWatchActor {

  def props(actorToNotify: ActorRef): Props = Props(classOf[FileSystemWatchActor], actorToNotify)

  // timer messages
  private case object TickKey

  private case object Tick

  // messages
  final case class Register(start: Path, recurse: Boolean)

  final case class Created(path: Path)

  final case class Modified(path: Path)

  final case class Deleted(path: Path)

}

class FileSystemWatchActor(actorToNotify: ActorRef) extends Actor with Timers with ActorLogging {

  import FileSystemWatchActor._

  private var wd: WatchDir = _


  // TODO review the notification interfaces
  private def created(path: Path): Unit = {
    log.debug(s"created $path")
    if (actorToNotify != null)
      actorToNotify ! Created(path)
  }

  private def deleted(path: Path): Unit = {
    log.debug(s"deleted $path")
    if (actorToNotify != null)
      actorToNotify ! Deleted(path)
  }

  private def modified(path: Path): Unit = {
    log.debug(s"modified $path")
    if (actorToNotify != null)
      actorToNotify ! Modified(path)
  }

  override def preStart(): Unit = {
    super.preStart()
    wd = WatchDir(created, modified, deleted)
    timers.startSingleTimer(TickKey, Tick, 1 second)
  }

  override def postStop(): Unit = {
    super.postStop()
    timers.cancelAll()
    if (wd != null)
      wd.close()
  }

  def receive: Receive = {
    case Register(start, recurse) => if (wd != null) wd.register(start, recurse)
    case Tick => {
      //log.debug("tick!")
      if (wd != null)
        // poll() is not blocking:
        // true = a key was found; there may be more
        // false = nothing in the queue at this time; try later
        while (wd.poll()) {}
      // re-arm the timer - better implementation than using periodic timer
      timers.startSingleTimer(TickKey, Tick, 5 second)
    }
  }

}