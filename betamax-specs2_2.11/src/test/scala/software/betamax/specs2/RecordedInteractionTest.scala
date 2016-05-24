package software.betamax.specs2

import org.apache.commons.io.IOUtils
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
  sequential

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
    "replay a gzip'd request from https://www.cultizm.com/" in RecordedInteraction(tape = "cultizm") {
      withHttpClient { client =>
        val response = client.execute(new HttpGet("https://www.cultizm.com/"))
        IOUtils.toString(response.getEntity.getContent) must contain("<!DOCTYPE html PUBLIC")
        response.getStatusLine.getStatusCode must beEqualTo(902) // obviously not from cultizm
      }
    }
  }
}