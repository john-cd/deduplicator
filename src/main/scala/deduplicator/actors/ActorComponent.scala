package deduplicator.actors

// TO READ:
// https://developer.lightbend.com/guides/akka-distributed-workers-scala/back-end.html
// http://letitcrash.com/post/29044669086/balancing-workload-across-nodes-with-akka-2

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Await
import scala.concurrent.duration._

trait ActorComponent extends LazyLogging {

  val actorService: ActorService

  trait ActorService {
    def run(): Unit
  }


  object ActorServiceImpl {
    // Loan pattern for actor system
    private def using[A](f: ActorSystem => A): A = {
      val system: ActorSystem = ActorSystem("mainActorSystem")
      try {

        // do something with it
        f(system)
      } finally {
        system.terminate()
        Await.ready(system.whenTerminated, Duration(15, SECONDS)) // blocking
      }
    }
  }

  class ActorServiceImpl extends ActorService {

    import ActorServiceImpl._

    // TODO
    override def run(): Unit = using(system => {
      //val supervisor = system.actorOf(Supervisor.props(), "supervisor")
      //supervisor ! FindDuplicates()

      //      import deduplicator.io._
      //      import FileSystemWatchActor._
      //      import java.nio.file._
      //      val watch = system.actorOf(FileSystemWatchActor.props(null), "fileSystemWatch")
      //      watch ! Register(Paths.get(raw"src\test\resources\testfilesystem"), recurse = false)

//      println(">>> Press ENTER to exit <<<")
//      scala.io.StdIn.readLine()

    })
  }

}
