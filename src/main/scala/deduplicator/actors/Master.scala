package deduplicator.actors

import com.typesafe.scalalogging.LazyLogging
import collection.mutable.ListBuffer
import akka.actor.{Props, Cancellable, Actor}
import akka.routing.RoundRobinPool


object Master {
  def props(numWorkers: Int, actorFactory: ActorFactory): Props = Props(new Master(numWorkers, actorFactory))
  //def props: Props = Props[Master]

  final case class FindDuplicates()
  
}

class Master(numWorkers: Int, actorFactory: ActorFactory) extends Actor with LazyLogging {
  import Master._
  
  val cancelables = ListBuffer[Cancellable]()

  val router = context.actorOf(
    Props(actorFactory.createWorkerActor()).withRouter(RoundRobinPool(numWorkers)),
    "master-worker-router"
  )

  override def receive: Receive = { case _ => }

  //  override def receive: Receive = {
  //    case Done(name, command, jobType, success) =>
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

