package web
import java.util.concurrent.Executors

import org.apache.cxf.jaxrs.client.WebClient
import org.apache.cxf.jaxrs.client.ClientConfiguration
import org.apache.cxf.jaxrs.client.WebClient
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.jaxrs.client.WebClient
import org.apache.cxf.jaxrs.impl.ResponseImpl
import sys.process._
import scala.concurrent.{ExecutionContext, Future}
object ApacheCxfTest extends App {
  println("Start Simulation")

  val appStartup = System.currentTimeMillis()

  protected def createClient(basePath: String): WebClient = {
    val client = WebClient
      .create("http://localhost:8080")
      .path(basePath)
    val config = WebClient.getConfig(client)
    val http   = config.getConduit.asInstanceOf[HTTPConduit]
    http.getClient.setConnectionTimeout(3000)
    http.getClient.setReceiveTimeout(10000)
    client
  }

  def sendRequest(name: String, sleepDuration: Int): Unit = {

    val client = createClient(s"/sleep/$sleepDuration").query("name", name)

    val reqStartAt = System.currentTimeMillis()
    println(s"[$name-$sleepDuration] About to send request at ${reqStartAt - appStartup}")
    val response = client.get.asInstanceOf[ResponseImpl]

    println(
      s"[$name-$sleepDuration] Got response after ${System.currentTimeMillis() - reqStartAt}, status: ${response.getStatus}")
  }

  implicit val ex = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10000))

  for(i <- 1 to 100000) {
    Future {
      sendRequest(s"pawel-$i", 20000)
    }
  }

  println("End Simulation")
}
