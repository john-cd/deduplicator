package deduplicator.actors

import com.typesafe.scalalogging.LazyLogging
import akka.actor.{Props, Cancellable, Actor}
import akka.routing.RoundRobinPool

import deduplicator.dao._
import sys.process._

object Worker {
  def props(daoService: DaoService): Props = Props(new Worker(daoService))
  //def props(): Props = Props[Worker] 
  
  sealed trait Message
  final case class Work(name: String, command: String) extends Message
  final case class Done(name: String, command: String, success: Boolean) extends Message
}

class Worker(daoService: DaoService) extends Actor with LazyLogging {
	import Worker._

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
override def receive: Receive = {
//    case w @ Work(name, command, jobType) => doWork(w)
      case _ =>
 }
}
