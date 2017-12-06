package deduplicator

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn
import scala.util.{Failure, Success}

object Main extends LazyLogging {

  import deduplicator.registry.ComponentRegistry._

  def main(args: Array[String]): Unit = {

    commandLineService.parse(args) match {
      case Some(CommandLineConfig(paths, recursive)) => doWork(paths, recursive)
      case None => System.exit(1) // Bad arguments. Error message has been displayed
    }
  }

  // TODO cleanup
  private def doWork(paths: Seq[String], recursive: Boolean): Unit = {


    //    logger.info("Running migrations before doing anything else.")
    //    migrationService.migrate()
    //    logger.info("Migrations done!")
    //
    //    logger.info("Starting actor system. Use CTRL+C to exit.")
    // actorService.run()

    // file system walking tests
    var hasher = deduplicator.hash.HashService()


    def test(path: Path, attrs: BasicFileAttributes = null): Future[String] = {
      hasher.checksum(path).andThen {
        case Success(h) => logger.info(s"$path -> $h")
        case Failure(exc) => logger.warn("Hash failure: ", exc)
      }
    }
    //    println(Await.ready( test(Paths.get(raw"src\test\resources\testfilesystem\d.txt")), 10 seconds ))  // blocking


    deduplicator.io.FileSystemWalker.walk(
      Paths.get(raw"src\test\resources\testfilesystem"),
      test(_, _),
      recurse = true)

    //    println(">>> Press ENTER to exit <<<")
    StdIn.readLine()
  }
}