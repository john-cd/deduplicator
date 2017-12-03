package deduplicator

import com.typesafe.scalalogging.LazyLogging
import scala.io.StdIn

object Main extends LazyLogging {

  import deduplicator.registry.ComponentRegistry._
  
  def main(args: Array[String]): Unit = {
  
	commandLineService.parse(args) match {
	  case Some(cliconfig) => doWork(cliconfig)
	  case None => System.exit(1) // Bad arguments. Error message has been displayed
	}
  }
  
  private def doWork(cliconfig: CommandLineConfig) = { 
  
	logger.info("Running migrations before doing anything else.")
	migrationService.migrate()
	logger.info("Migrations done!")

	//logger.info("Starting actor system. Use CTRL+C to exit.")
    //actorService.start("deduplicator")

	println(">>> Press ENTER to exit <<<")
    StdIn.readLine()
  }  
}