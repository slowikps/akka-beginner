package web
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import org.apache.cxf.jaxrs.client.WebClient
import org.apache.cxf.jaxrs.impl.ResponseImpl
import org.apache.cxf.transport.http.HTTPConduit

import scala.concurrent.{ExecutionContext, Future}
object ApacheCxfTest extends App {
  println("Start Simulation")

  val appStartup = System.currentTimeMillis()

  val port = new AtomicInteger()

  protected def createClient(basePath: String): WebClient = {
    val client = WebClient
      .create(s"http://localhost:808${1 + port.incrementAndGet() % 9}")
      .path(basePath)
    val config = WebClient.getConfig(client)
    val http   = config.getConduit.asInstanceOf[HTTPConduit]
    http.getClient.setConnectionTimeout(30000)
    http.getClient.setReceiveTimeout(31000)
    client
  }

  def sendRequest(name: String, sleepDuration: Int): Unit = {
    val client = createClient(s"/sleep/$sleepDuration").query("name", name)

    val reqStartAt = System.currentTimeMillis()
//    println(s"[$name-$sleepDuration] About to send request at ${reqStartAt - appStartup}")
    val reqStarted = System.currentTimeMillis()
    try {
     val response = client.get.asInstanceOf[ResponseImpl]
    } catch {
      case e:Exception => println(s"[$name] Req started: $reqStartAt, elapsed: ${System.currentTimeMillis() - reqStartAt}, ${e.getMessage}" )
    }

//    println(
//      s"[$name-$sleepDuration] Got response after ${System.currentTimeMillis() - reqStartAt}, status: ${response.getStatus}")
  }

  implicit val ex = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(800))

  for(i <- 1 to 100000) {
    Future {
      sendRequest(s"pawel-$i", 20)
    }
  }

  println("End Simulation")
}
