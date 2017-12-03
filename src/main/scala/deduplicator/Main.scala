package deduplicator

import akka.actor.{Props, ActorSystem}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends LazyLogging {

  import deduplicator.registry.ComponentRegistry._
  
  def main(args: Array[String]): Unit = {
  
	commandLineService.parse(args) match {
	  case Some(config) => {
		// do stuff
	  }

	  case None => System.exit(1)
		// arguments are bad, error message will have been displayed
	}
  
	logger.info("Running migrations before doing anything else.")
	migrationService.migrate()
	logger.info("Migrations done!")

	
    val system = ActorSystem("deduplicator")

    val master = system.actorOf(
      Props(actorFactory.createMasterActor()),
      "master"
    )

    sys.addShutdownHook({
      logger.info("Awaiting actor system termination.")
      // not great...
      Await.result(system.terminate(), Duration.Inf)
      logger.info("Actor system terminated. Bye!")
    })

    //master ! FindDuplicates()
    logger.info("Started! Use CTRL+C to exit.")
  }
}