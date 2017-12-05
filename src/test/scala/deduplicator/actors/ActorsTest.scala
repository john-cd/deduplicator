package deduplicator.actors

import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef, TestProbe}
import scala.concurrent.duration._

// import Greeter._
// import Printer._

// class AkkaQuickstartSpec(_system: ActorSystem)
// extends TestKit(_system)
// with Matchers
// with FlatSpecLike
// with BeforeAndAfterAll {

// def this() = this(ActorSystem("AkkaQuickstartSpec"))

// override def afterAll: Unit = {
// shutdown(system)
// }

// "A Greeter Actor" should "pass on a greeting message when instructed to" in {
// val testProbe = TestProbe()
// val helloGreetingMessage = "hello"
// val helloGreeter = system.actorOf(Greeter.props(helloGreetingMessage, testProbe.ref))
// val greetPerson = "Akka"
// helloGreeter ! WhoToGreet(greetPerson)
// helloGreeter ! Greet
// testProbe.expectMsg(500 millis, Greeting(s"$helloGreetingMessage, $greetPerson"))
// }
// }