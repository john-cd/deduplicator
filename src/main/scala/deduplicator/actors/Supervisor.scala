package deduplicator.actors

import akka.actor.{Props, Terminated}

object Supervisor {
  def props(): Props = Props[Supervisor]
}

class Supervisor extends RootActor {

  // TODO
  def createChild() = {
    // star child, then keep an eye on it
    // If child is killed or stopped, the Parent actor is sent a Terminated(child) message
    //val child = context.actorOf(Child.props(), name = "Child")
    //context.watch(child)
  }

  override def receive: Receive = {
    case Terminated(child) => {
      log.info("Child $child killed")
    }
    case _ => println("Parent received an unknown message")
  }
}

// class Terminator(ref: ActorRef) extends Actor with ActorLogging {
// context watch ref
// def receive = {
// case Terminated(_) =>
// log.info("{} has terminated, shutting down system", ref.path)
// context.system.terminate()
// }