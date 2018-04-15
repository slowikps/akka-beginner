package actors.experiments

import akka.actor.{Actor, ActorSystem, Props}

import scala.io.StdIn

class PrintMyActorRefActor extends Actor {
  override def receive: Receive = {
    case "printit" ⇒
      val secondRef = context.actorOf(Props.empty, "second-actor")
      println(s"Second: $secondRef")
    case ("recursive", x: Int) ⇒
      if(x < 0) {
        val secondRef = context.actorOf(Props.empty)
        println(s"Second recursive: $secondRef")
      } else {
        context.actorOf(Props[PrintMyActorRefActor], s"first-actor_$x") ! ("recursive", x - 1)
      }
  }
}

object ActorHierarchyExperiments extends App {
  val system = ActorSystem("testSystem")

  val firstRef = system.actorOf(Props[PrintMyActorRefActor], "first-actor")
  println(s"First: $firstRef")
  firstRef ! "printit"
  firstRef ! ("recursive", 1000)

  println(">>> Press ENTER to exit <<<")

  try StdIn.readLine()
  finally system.terminate()
}