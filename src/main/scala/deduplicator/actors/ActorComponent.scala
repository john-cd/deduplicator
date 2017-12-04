package deduplicator.actors

// TO READ:
// https://developer.lightbend.com/guides/akka-distributed-workers-scala/back-end.html
// http://letitcrash.com/post/29044669086/balancing-workload-across-nodes-with-akka-2

import akka.actor.{Props, ActorSystem}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.typesafe.scalalogging.LazyLogging


trait ActorComponent extends LazyLogging {

  val actorService: ActorService

  trait ActorService {
    def run(actorSystemName: String): Unit
  }
  
  class ActorServiceImpl extends ActorService {
  	 
	override def run(actorSystemName: String = "main actor system"): Unit = {

	  // Create the actor system
	  val system: ActorSystem = ActorSystem(actorSystemName)

	  try {
	    // Create top level supervisor
        val supervisor = system.actorOf(Supervisor.props(), "supervisor")

        //supervisor ! FindDuplicates() 

	  } finally {
		system.terminate()
		//TODO system.awaitTermination()
	  }
	}
  }
}
