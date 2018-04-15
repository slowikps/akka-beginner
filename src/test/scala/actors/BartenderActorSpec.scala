package actors

import java.util.concurrent.TimeUnit

import actors.BartenderActor.Order
import akka.actor.{ActorSystem, UnhandledMessage}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class BartenderActorSpec
    extends TestKit(ActorSystem("MySpec"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Bartender actor" must {
    system.eventStream.subscribe(system.actorOf(EchoActor.props, "debuggingActor"), classOf[UnhandledMessage])
    val bartender = system.actorOf(BartenderActor.props, "bartender")

    "provide me with a beer" in {
      println(bartender)
      bartender ! BartenderActor.Beer
      expectMsg(Order("This is a beer for you"))
    }

    "sent some water when I am hangover" in {
      bartender ! BartenderActor.Water
      expectMsg(Order("Don't get to crazy with this"))
    }

    "unusual way of ordering Mojito should be handeled" in {
      bartender ! "Mojito please"
      expectMsg(Order("Here is your Mojito"))
    }

    "Sorry - Gin and tonic is not available" in {
      bartender ! "Gin and tonic"
      expectNoMessage()
      TimeUnit.SECONDS.sleep(1)
    }

  }
}
