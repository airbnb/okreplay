package software.betamax

import software.betamax.specs2.RecordedInteraction
import org.specs2.mutable.Specification
import play.api.libs.ws.WS
import play.api.test.{DefaultAwaitTimeout, WithServer}

import scala.concurrent.Await


/**
  * Created by sean on 2/15/16.
  */
class BetamaxTest extends Specification with DefaultAwaitTimeout {
  "A recorded interation" should {
    "replay /" in RecordedInteraction("index") {
      val port = 3333
      new WithServer(port = port) {
        val response = Await.result(WS.url(s"http://localhost:$port").get(), defaultAwaitTimeout.duration)
        response.body must beEqualTo("Hello from Betamax")
      }
    }
  }
}
