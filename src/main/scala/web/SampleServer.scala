package web
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContext, Promise}
import scala.io.StdIn
import scala.concurrent.duration._
import scala.util.Success

object SampleServer extends App {

  implicit val system       = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val ex = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(50))

  val numberOfConnections1 = new AtomicInteger()
  val numberOfConnections2 = new AtomicInteger()
  val numberOfConnections3 = new AtomicInteger()
  val numberOfConnections4 = new AtomicInteger()
  val numberOfConnections5 = new AtomicInteger()

  def route(port: Int) =
    pathPrefix("sleep" / IntNumber) { sleep =>
      get {
        val numberOfConnections = port match {
          case 1 => numberOfConnections1
          case 2 => numberOfConnections2
          case 3 => numberOfConnections3
          case 4 => numberOfConnections4
          case 5 => numberOfConnections5
          case _ => numberOfConnections1
        }
        numberOfConnections.incrementAndGet()

        parameters('name) { name =>
          val startTime = System.currentTimeMillis()
          println(s"Receiving request from: $name at: $startTime ms")
          val p = Promise[String]()
          system.scheduler.scheduleOnce(sleep milliseconds) {
            println(s"Completing request from: $name after: ${System.currentTimeMillis() - startTime} ms")
            p.complete(Success(name))
            numberOfConnections.decrementAndGet()
          }
          onComplete(p.future) {
            case Success(name) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Success for: $name after sleeping: $sleep milliseconds</h1>"))
            case _ => {
              println("BOOOOM")
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Errror Errrror</h1>"))
            }
          }
        }
      }
    }

  system.scheduler.schedule(0 second, 1 second) {
    println(s"Number of connections: ${numberOfConnections1.get()}, ${numberOfConnections2.get()}, ${numberOfConnections3.get()}, ${numberOfConnections4.get()}, ${numberOfConnections5.get()}")
  }
  for (i <- 1 to 10) {
    println(i)
    Http().bindAndHandle(route(i%6), "localhost", 8080 + i)
  }


  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
//  bindingFuture
//    .flatMap(_.unbind()) // trigger unbinding from the port
//    .onComplete(_ => system.terminate()) // and shutdown when done

}
