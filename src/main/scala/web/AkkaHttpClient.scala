package web
import java.net.{DatagramSocket, ServerSocket, Socket}
import java.util.concurrent.{ConcurrentHashMap, Executors}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.io.Inet.SocketOption
import akka.stream.ActorMaterializer
import sun.nio.ch.SelChImpl

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object MySocketOption extends SocketOption {

  val map: ConcurrentHashMap[Int, Long] = new ConcurrentHashMap()

  override def beforeDatagramBind(ds: DatagramSocket): Unit = println(s"beforeDatagramBind: $ds")

  /**
    * Action to be taken for this option before bind() is called
    */
  override def beforeServerSocketBind(ss: ServerSocket): Unit = println(s"beforeServerSocketBind: $ss")

  /**
    * Action to be taken for this option before calling connect()
    */
  override def beforeConnect(s: Socket): Unit = {
    map.put(s.getChannel.asInstanceOf[SelChImpl].getFDVal, System.currentTimeMillis())
//    s.setPerformancePreferences()

    println(s"beforeConnect: $s")
  }

  /**
    * Action to be taken for this option after connect returned (i.e. on
    * the slave socket for servers).
    */
  override def afterConnect(s: Socket): Unit = {
    val took = System.currentTimeMillis() - map.remove(s.getChannel.asInstanceOf[SelChImpl].getFDVal)
    if (took > 150) {
      println(s"afterConnect: $s [took: $took ms]")
    }
    println(s"afterConnect: $s [took: $took ms]")
  }

}

object AkkaHttpClient extends App {
  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
//  implicit val executionContext = system.dispatcher
  println("App started")

  implicit val ex = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

  val timeoutSettings =
    ConnectionPoolSettings(system.settings.config)
      .withConnectionSettings(
        ConnectionPoolSettings(system.settings.config).connectionSettings
          .withConnectingTimeout(1 seconds)
//          .withIdleTimeout(10 seconds)
          .withSocketOptions(List(MySocketOption))
//            .withLocalAddress()
      )
//      .withMaxConnections(50)
//      .withMaxOpenRequests(536870912)

  val uri: String = s"http://localhost:8080/retry/5000?name=pawel"
//  val uri = s"http://localhost:8080/test/internalError"
  val start = System.currentTimeMillis()
  val responseFuture: Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = uri), settings = timeoutSettings)

//  Await.result(responseFuture, 400.second)
  responseFuture
    .onComplete {
      case Success(res) =>
        println(s"$res [time: ${System.currentTimeMillis() - start}]")
        res.discardEntityBytes()
      case Failure(ex) =>
        ex.printStackTrace()
        println(s"something wrong: ${ex.getMessage} [time: ${System.currentTimeMillis() - start}]")
    }

  def parallel(block: Int => Any, repeats: Int = 1): Unit = {
    for (i <- 0 to repeats) {
      Future {
        block(i)
      }.onComplete {
        case Success(_) =>
        case Failure(exc)  =>
          sys.error(s"something wrong: ${exc.getMessage}")
          exc.printStackTrace()
      }
    }
  }
}
