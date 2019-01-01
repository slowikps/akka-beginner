package web
import java.util.concurrent.Executors

import org.apache.cxf.jaxrs.client.WebClient
import org.apache.cxf.jaxrs.impl.ResponseImpl
import org.apache.cxf.transport.http.HTTPConduit

import scala.concurrent.ExecutionContext

object SingleReqCxfTest extends App {
  println("Start Simulation")

  val appStartup = System.currentTimeMillis()

  protected def createClient(basePath: String): WebClient = {
    val client = WebClient
      .create(s"http://localhost")
      .path(basePath)
    val config = WebClient.getConfig(client)
    val http   = config.getConduit.asInstanceOf[HTTPConduit]
    http.getClient.setConnectionTimeout(300000)
    http.getClient.setReceiveTimeout(31000)
    client
  }

  def sendRequest(name: String, sleepDuration: Int): Unit = {
    val client     = createClient(s"/sleep/$sleepDuration").query("name", name)
    val reqStartAt = System.currentTimeMillis()

    try {
      val response = client.get.asInstanceOf[ResponseImpl]
      println(
        s"[$name-$sleepDuration] Got response after ${System.currentTimeMillis() - reqStartAt}, status: ${response.getStatus}")
    } catch {
      case e: Exception =>
        println(s"[$name] Req started: $reqStartAt, elapsed: ${System.currentTimeMillis() - reqStartAt}, ${e.getMessage}")
    }
  }

  implicit val ex = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(800))

  sendRequest(s"pawe", 1000)

  println("End Simulation")

}
