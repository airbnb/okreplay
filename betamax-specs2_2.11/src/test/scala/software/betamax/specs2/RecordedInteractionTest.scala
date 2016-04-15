package software.betamax.specs2

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

/**
  * Created by sean on 2/11/16.
  */
@RunWith(classOf[JUnitRunner])
class RecordedInteractionTest extends Specification {
  def withHttpClient[T](block: HttpClient => T): T = {
    val client = HttpClientBuilder.create().useSystemProperties().build()
    block(client)
  }

  "A Betamax test" should {
    "replay google.com" in RecordedInteraction(tape = "google") {
      withHttpClient { client =>
        val response = client.execute(new HttpGet("https://www.google.com"))
        response.getStatusLine.getStatusCode must beEqualTo(902) // obviously not from Google
      }
    }
  }
}