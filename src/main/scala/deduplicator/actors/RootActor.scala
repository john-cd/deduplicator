package deduplicator.actors

import akka.actor.{Actor, ActorLogging, Props}

object RootActor {
  def props(): Props = Props[RootActor]
}

class RootActor extends Actor with ActorLogging {

  override def preStart(): Unit = log.info(s"${self.path} started")

  override def postStop(): Unit = log.info(s"${self.path} stopped")

  // Not recommended to override preRestart and postRestart
  // Default implementation: https://doc.akka.io/docs/akka/current/actors.html#actor-api
  // def preRestart(reason: Throwable, message: Option[Any]): Unit = {
  //   context.children foreach { child =>
  //     context.unwatch(child)
  //     context.stop(child)
  //   }
  //   postStop()
  // }
  //
  // def postRestart(reason: Throwable): Unit = {
  //   preStart()
  // }

  // No need to handle any messages
  override def receive: Receive = Actor.emptyBehavior
}


