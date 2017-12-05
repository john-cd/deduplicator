package deduplicator

import com.typesafe.scalalogging.LazyLogging
import scala.io.StdIn

object Main extends LazyLogging {

  import deduplicator.registry.ComponentRegistry._

  def main(args: Array[String]): Unit = {

    commandLineService.parse(args) match {
      case Some(CommandLineConfig(paths, recursive)) => doWork(paths, recursive)
      case None => System.exit(1) // Bad arguments. Error message has been displayed
    }
  }

  private def doWork(paths: Seq[String], recursive: Boolean) = {
    logger.info("Running migrations before doing anything else.")
    migrationService.migrate()
    logger.info("Migrations done!")

    logger.info("Starting actor system. Use CTRL+C to exit.")
    actorService.run()

    //println(">>> Press ENTER to exit <<<
    // StdIn.readLine()
  }
}