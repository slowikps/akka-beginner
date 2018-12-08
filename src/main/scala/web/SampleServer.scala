package web
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.Promise
import scala.io.StdIn
import scala.concurrent.duration._
import scala.util.Success

object SampleServer extends App {

  implicit val system       = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val route =
    pathPrefix("sleep" / IntNumber) { sleep =>
      get {
        parameters('name) { name =>
          val startTime = System.currentTimeMillis()
          println(s"Receiving request from: $name at: $startTime ms")
          val p = Promise[String]()
          system.scheduler.scheduleOnce(sleep milliseconds) {
            println(s"Completing request from: $name after: ${System.currentTimeMillis() - startTime} ms")
            p.complete(Success(name))
          }
          onComplete(p.future) {
            case Success(name) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Success for: $name after sleeping: $sleep milliseconds</h1>"))
            case _ => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Errror Errrror</h1>"))
          }
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
