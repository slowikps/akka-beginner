package actors

import actors.BartenderActor.{Beer, Order, Water}
import akka.actor.{Actor, Props}

object BartenderActor {
  object Beer
  object Water

  case class Order(msg: String)

  def props = Props[BartenderActor]
}

class BartenderActor extends Actor {

  override def receive: Receive = {
    case Beer  => sender() ! Order("This is a beer for you")
    case Water => sender() ! Order("Don't get to crazy with this")
  }

  override def unhandled(message: Any): Unit = message match {
    case "Mojito please" => sender() ! Order("Here is your Mojito")
    case _               => super.unhandled(message)
  }
}
