package web
import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.http.scaladsl.model._
import akka.stream.DelayOverflowStrategy

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
object AkkaLowLevelServer extends App {

  import akka.actor.ActorSystem
  import akka.http.scaladsl.Http
  import akka.stream.ActorMaterializer
  import akka.stream.scaladsl._

  implicit val system           = ActorSystem()
  implicit val materializer     = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val map = mutable.Map[String, String]()
  val (host, port) = ("localhost", 8080)

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] = Http().bind(host, port)

  val reactToConnectionFailure = Flow[HttpRequest]
    .recover[HttpRequest] {
      case ex =>
        println(s"Kaboom: $ex")
        throw ex
    }

  val httpEcho: Flow[HttpRequest, HttpResponse, NotUsed] = Flow[HttpRequest]
    .via(reactToConnectionFailure)
    .map { request =>
      // simple streaming (!) "echo" response:
      println(request.uri.toString())
      if(request.uri.toString().contains("retry") && map.get(request.uri.toString()).isEmpty) {
        map(request.uri.toString()) = "blah"
        HttpResponse(status = StatusCodes.InternalServerError)
      } else if (request.uri.toString().contains("pawel")) {
        map.remove(request.uri.toString())
        HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Witam"))
      } else if (request.uri.toString().contains("internalError")) {
        HttpResponse(status = StatusCodes.InternalServerError)
      } else {
        TimeUnit.SECONDS.sleep(20)
        println(s"           About to send resp [connectionCloseExpected: ${request.connectionCloseExpected}]")
        val rsp: HttpResponse = HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, request.entity.dataBytes))
        rsp
      }
    }

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource
      .to(Sink.foreach { connection: Http.IncomingConnection => // foreach materializes the source
        println("Accepted new connection from " + connection.remoteAddress + " on local address: " + connection.localAddress)
        connection.handleWith(httpEcho)
        // ... and then actually handle the connection
      })
      .run()

}
