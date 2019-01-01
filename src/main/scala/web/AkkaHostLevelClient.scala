package web
import java.util.concurrent.Executors

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

object AkkaHostLevelClient extends App {

  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  println("App started")

  val poolClientFlow = Http().cachedHostConnectionPool[String]("localhost", 8080)


  def pathToVisit(): Source[String, NotUsed] =
  // This could even be a lazy/infinite stream. For this example we have a finite one:
    Source(List(
      "/pawel", "/internalError", "/somethingElse"
    ))


  // you need to supply the list of files to upload as a Source[...]

  def createUploadRequest(path: String): Future[(HttpRequest, String)] = Future {
    (HttpRequest(uri = path), path)
  }


  pathToVisit()
    // The stream will "pull out" these requests when capacity is available.
    // When that is the case we create one request concurrently
    // (the pipeline will still allow multiple requests running at the same time)
    .mapAsync(10)(createUploadRequest)
    // then dispatch the request to the connection pool
    .via(poolClientFlow)
    // report each response
    // Note: responses will not come in in the same order as requests. The requests will be run on one of the
    // multiple pooled connections and may thus "overtake" each other.
    .runForeach {
    case (Success(response), pathToVisit) =>
      println(s"Result for $pathToVisit was successful: $response, code: ${response.status}")
      response.discardEntityBytes() // don't forget this
    case (Failure(ex), pathToVisit) =>
      println(s"Failed calling $pathToVisit failed with $ex")
  }


}
