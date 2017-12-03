package deduplicator.actors

import akka.actor.{Props, ActorSystem}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.typesafe.scalalogging.LazyLogging


trait ActorComponent extends LazyLogging {
  //this: ActorFactoryComponent =>

  val actorService: ActorService

  trait ActorService {
  	 
	def start(actorSystemName: String): Unit = {

	  // Create the actor system
	  val system: ActorSystem = ActorSystem(actorSystemName)

	  try {
		//  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")
		
		//val master = system.actorOf(Props(actorFactory.createMasterActor()),"master")

    sys.addShutdownHook({
      logger.info("Awaiting actor system termination.")
      // not great...
      Await.result(system.terminate(), Duration.Inf)
      logger.info("Actor system terminated. Bye!")
    })

    //master ! FindDuplicates()

	  } finally {
		system.terminate()
	  }
	}
  }
}
