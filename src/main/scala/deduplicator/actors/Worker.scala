package deduplicator.actors

import akka.actor.{Actor, ActorLogging, Props}
import deduplicator.hash._

object Worker {
  //def props(daoService: DaoService): Props = Props(new Worker(daoService))
  def props(hasher: HashService): Props = Props(new Worker(hasher))


  sealed trait Message

  final case class HashFile(filepath: String) extends Message

  final case class FileHashed(filepath: String, hash: Option[String]) extends Message

  // final case class HashFilesInDirectory(dirpath: String)

}

class Worker(hashService: HashService) extends Actor with ActorLogging {

  import Worker._

  require(hashService != null)

  override def receive: Receive = {
    case HashFile(filepath) => {
      val hash = hashService.checksum(filepath)
      // TODO
      //pipe(hash) to sender()
      //sender() ! FileHashed(filepath, hash)
    }
  }
}

// // https://doc.akka.io/docs/akka/snapshot/actors.html#extending-actors-using-partialfunction-chaining
// trait ReadFileBehavior {
// this: Actor with ActorLogging =>

// val readFileBehavior: Receive = {
// case  =>
// log.info("{}", )
// }
// }

// class ProducerConsumer extends Actor with ActorLogging
// with readFileBehavior with ConsumerBehavior {

// def receive = readFileBehavior.orElse[Any, Unit](consumerBehavior)
// }	

// def props(daoService: DaoService): Props = Props(new Worker(daoService))
// final case class Work(name: String, command: String) extends Message
// final case class Done(name: String, command: String, success: Boolean) extends Message
//  private def doWork(work: Work): Unit = {
//    work.jobType match {
//      case Console =>
//        val result = work.command.! // note - the ! are different methods
//        sender ! Done(work.name, work.command, work.jobType, result == 0)
//      case Sql =>
//        val connection = daoService.getConnection()
//        try {
//          val statement = connection.prepareStatement(work.command)
//          val result: List[String] = daoService.executeSelect(statement) {
//            case rs =>
//              val metadata = rs.getMetaData
//              val numColumns = metadata.getColumnCount
//              daoService.readResultSet(rs) {
//                case row =>
//                  (1 to numColumns).map {
//                    case i =>
//                      row.getObject(i)
//                  }.mkString("\t")
//              }
//          }
//          logger.info("Sql query results: ")
//          result.foreach(r => logger.info(r))
//          sender ! Done(work.name, work.command, work.jobType, true)
//        } finally {
//          connection.close()
//        }
//    }
//  }
//


