package deduplicator

import java.nio.file._
import java.sql.Timestamp

import com.typesafe.scalalogging.LazyLogging
import deduplicator.utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util._

//noinspection SpellCheckingInspection,SpellCheckingInspection
object Main extends LazyLogging {

  import deduplicator.registry.ComponentRegistry._

  def main(args: Array[String]): Unit = {
    commandLineService.parse(args) match {
      case Some(CommandLineConfig(pathStrings, recursive, test)) => doWork(pathStrings, recursive, test)
      case None => System.exit(1) // Bad arguments. Error message has been displayed
    }
  }

  private def doWork(pathStrings: Seq[String], recursive: Boolean, test: Boolean): Unit = {

    logger.info("Running migrations before doing anything else")
    migrationService.migrate()
    logger.info("Migrations done!")

    val paths = if (test)
      Iterator(Paths.get(raw"src\test\resources\testfilesystem"))
    else
      for (path <- pathStrings) yield Paths.get(path)

    val allDone = for (path <- paths) yield {
      logger.info(s"Starting walking $path")
      val iter: Iterator[Future[Hash]] = fileSystemWalker.walk(path, hashService.checksum, recurse = true)

      // add a side effect (logging) to each future, without changing their return type
      val i2 = for {fut <- iter} yield fut.andThen {
        case Success(h) => logger.debug(s"${h.path} -> ${h.toHexString}")
        case Failure(exc) => logger.warn("Hash failure: ", exc)
      }

      val groupedFut = i2.grouped(500) // batch 500 future results
        .map {
        (group: Seq[Future[Hash]]) =>
          successSequence(group) // convert to a future Seq of Hash, only for successful ones
            .map(seq => daoService.insertHashes(seq.iterator)) // insert the batch of hashes in the DB
      }


      // return a Future of insert results, flattened
      (for {iter <- Future.sequence(groupedFut)} yield for {res <- iter; tryInsert <- res} yield tryInsert)
        .andThen { case _ => logger.info(s"Walking complete for $path") }
    }

    // wait for all Futures to complete
    Await.result(Future.sequence(allDone), Duration.Inf)

    println("------------------------------- DETAILS -------------------------------")
    daoService.readHashes.foreach(h => println(s" ${h.id.getOrElse(-1L)} [ ${new Timestamp(h.timeStamp)} ] ${h.path} --> ${h.toHexString}"))


    println("------------------------------- DUPES -------------------------------")
    daoService.findDuplicates.foreach(d => println(s" ${d.id} [ ${d.timeStamp} ] ${d.path} --> ${d.toHexString}"))

    //println(">>> Press ENTER to exit <<<")
    //import scala.io.StdIn
    //StdIn.readLine()


  } // doWork
} // Main