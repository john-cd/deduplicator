package deduplicator

import java.nio.file._
import java.sql.Timestamp

import com.typesafe.scalalogging.LazyLogging
import deduplicator.utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util._
import scala.util.control.NonFatal

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
        case Failure(exc) => logger.warn(s"Hash failure: ${exc.getClass} ${exc.getMessage}")
      }

      def insertGroupOfHashes(group: (Seq[Future[Hash]], Int)): Future[Int] = {
        val result = successSequence(group._1) // future Seq of Hash, but only for successful Futures
          .map(seq => daoService.insertHashes(seq.iterator)) // insert the batch of hashes in the DB; return Future[Traversable[Int]]
          .map(insertResults => {
          val sb = new StringBuilder()
          println(insertResults.addString(sb, "<", ",", ">").result()) // print insert results and discard
          group._2 // return group index
        })
        result.failed.foreach( ex => logger.warn(s"Insert failures for group ${group._2}", ex))  // log if the future is failed
        result
      }

      // batch hash results and insert in DB; returns list of group
      val i3: Future[Iterator[Int]] = Future.sequence( i2.grouped(50000).zipWithIndex.map(insertGroupOfHashes) )
      i3.andThen {
        case _ => logger.info(s"Walking complete for $path")
      }
    }

    // wait for all Futures to complete
    try {
      Await.result(Future.sequence(allDone), Duration.Inf)
    }
    catch {
      case NonFatal(e) =>
        logger.error("Main.doWork throws ", e)
    }

    import java.io._
    var pw: PrintWriter = null
    try {
      pw = new PrintWriter(new File("results.txt"))
      pw.println("------------------------------- DETAILS -------------------------------")
      daoService.readHashes.foreach(h => pw.println(s" ${h.id.getOrElse(-1L)} [ ${new Timestamp(h.timeStamp)} ] ${h.path} --> ${h.toHexString}"))
      pw.println("------------------------------- DUPES -------------------------------")
      daoService.findDuplicates.foreach(d => pw.println(s" ${d.id} [ ${d.timeStamp} ] ${d.path} --> ${d.toHexString}"))
      pw.flush()
    } finally {
      pw.close
    }

    //println(">>> Press ENTER to exit <<<")
    //import scala.io.StdIn
    //StdIn.readLine()
  } // doWork
} // Main