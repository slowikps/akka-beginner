package actors

import akka.actor.{Actor, Props}

object EchoActor {
  def props = Props[EchoActor]()
}

class EchoActor extends Actor {

  override def receive: Receive = {
    case message => {
      println(s"Message not handled properly and forwarded to EchoActor:: $message, from: $sender()")
      sender() ! message
    }
  }
}
