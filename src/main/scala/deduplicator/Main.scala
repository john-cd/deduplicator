package deduplicator

import java.nio.file._

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.io.StdIn
import scala.util._
import scala.concurrent.ExecutionContext.Implicits.global


object Main extends LazyLogging {

  import deduplicator.registry.ComponentRegistry._

  def main(args: Array[String]): Unit = {
    commandLineService.parse(args) match {
      case Some(CommandLineConfig(pathStrings, recursive)) => doWork(pathStrings, recursive)
      case None => System.exit(1) // Bad arguments. Error message has been displayed
    }
  }

  private def doWork(pathStrings: Seq[String], recursive: Boolean, test: Boolean = true): Unit = {

    logger.info("Running migrations before doing anything else")
    migrationService.migrate()
    logger.info("Migrations done!")

    val path = if (test)
      Iterator(Paths.get(raw"src\test\resources\testfilesystem"))
    else
      for (path <- pathStrings) yield Paths.get(path)

    for (path <- path) {
      logger.info(s"Starting walking $path")
      val iter: Iterator[Future[Hash]] = fileSystemWalker.walk(path, hashService.checksum, recurse = true)
      val (i1, i2) = iter.duplicate

      i1.foreach( _.onComplete {
         case Success(h) => logger.info(s"${h.path} -> ${h.toHexString}")
         case Failure(exc) => logger.warn("Hash failure: ", exc)
      })

      val groupedFut = i2.grouped(500)   // TODO handle case when a Future in the batch fails - see deduplicator.utils._ package object
        .map {
          (group: Seq[Future[Hash]]) =>
            Future.traverse(group)(fut => fut)  // OR Future.sequence(group)
                .map( seq => daoService.insertHashes(seq.toIterator) )  // TODO insert should return something

        } //foreach
      logger.info(s"Walking complete for $path")
    } //for


    // TODO wait for all Futures to be completed
    // Future.sequence(groupedFut).wait()

    // TODO
    // daoService.findDuplicates

    println("------------------------------- DETAILS -------------------------------")
    // daoService.readHashes.foreach( h => println(s" ${h.id} [ ${h.timeStamp} ] ${h.path} --> ${h.hash}"))

    println(">>> Press ENTER to exit <<<")
    StdIn.readLine()
  } // doWork
} // Main