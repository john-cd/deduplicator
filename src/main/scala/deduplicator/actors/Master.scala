package deduplicator.actors

import collection.mutable.ListBuffer
import akka.actor.{Props, Cancellable, Actor, ActorLogging}
import akka.routing.RoundRobinPool

object Master {
  def props(numWorkers: Int): Props = Props(classOf[Master], numWorkers)

  final case class FindDuplicates(paths: Seq[String], recursive: Boolean)

  final case class Done()

}

class Master(numWorkers: Int) extends Actor with ActorLogging {

  val cancelables = ListBuffer[Cancellable]()

  // TODO
  // val router = context.actorOf(
  // Worker.props(daoService).withRouter(RoundRobinPool(numWorkers)),
  // "master-worker-router"
  // )

  // TODO
  override def receive: Receive = {
    case _ => log.info(s"Unknown message received from ${sender()}")
  }

  //  override def receive: Receive = {
  //    case Done(name, command, success) =>
  //      if (success) {
  //        logger.info("Successfully completed {} ({}).", name, command)
  //      } else {
  //        logger.error("Failure! Command {} ({}) returned a non-zero result code.", name, command)
  //      }
  //    case Schedule(configs) =>
  //      configs.foreach {
  //        case config =>
  //          val cancellable = this.context.system.scheduler.schedule(
  //            config.timeOptions.getInitialDelay(LocalDateTime.now(), config.frequency),
  //            config.frequency match {
  //              case Hourly => Duration.create(1, TimeUnit.HOURS)
  //              case Daily => Duration.create(1, TimeUnit.DAYS)
  //            },
  //            router,
  //            Work(config.name, config.command, config.jobType)
  //          )
  //          cancellable +: cancelables
  //          logger.info("Scheduled: {}", config)
  //      }
  //  }

  override def postStop(): Unit = {
    cancelables.foreach(_.cancel())
  }
}

