/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.betamax

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.ClassRule
import software.betamax.encoding.GzipEncoder
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import software.betamax.proxy.BetamaxInterceptor
import spock.lang.*

import static Headers.X_BETAMAX
import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK

@Unroll
@Betamax
class SmokeSpec extends Specification {
  def interceptor = new BetamaxInterceptor()
  static final TAPE_ROOT = new File(SmokeSpec.getResource("/betamax/tapes").toURI())
  @Shared def configuration = Configuration.builder().sslEnabled(true).tapeRoot(TAPE_ROOT).build()
  @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration, interceptor)

  def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  void "#type response data"() {
    when:
    def request = new Request.Builder()
        .url(uri)
        .addHeader("Accept-Encoding", "gzip")
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Betamax"
    response.header(X_BETAMAX) == "PLAY"
    response.body().string().contains(expectedContent)

    where:
    type   | uri                             | expectedContent
    "txt"  | "http://httpbin.org/robots.txt" | "User-agent: *"
    "html" | "http://httpbin.org/html"       | "<!DOCTYPE html>"
    "json" | "http://httpbin.org/get"        | '"url": "http://httpbin.org/get"'
  }

  void "gzipped response data"() {
    when:
    def request = new Request.Builder()
        .url(uri)
        .addHeader("Accept-Encoding", "gzip")
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Betamax"
    response.header(X_BETAMAX) == "PLAY"
    encoder.decode(response.body().byteStream()).contains('"gzipped": true')

    where:
    uri = "http://httpbin.org/gzip"
    encoder = new GzipEncoder()
  }

  void "redirects are followed"() {
    when:
    def request = new Request.Builder()
        .url(uri)
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Betamax"
    response.header(X_BETAMAX) == "PLAY"
    response.body().string().contains('"url": "http://httpbin.org/get"')

    where:
    uri = "http://httpbin.org/redirect/1"
  }

  void "https proxying"() {
    when:
    def request = new Request.Builder()
        .url(uri)
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Betamax"
    response.header(X_BETAMAX) == "PLAY"
    response.body().string().contains('"url": "http://httpbin.org/get"')

    where:
    uri = "https://httpbin.org/get"
  }

  @Ignore
  void "can POST to https"() {
    when:
    def request = new Request.Builder()
        .url(uri)
        .method("POST", RequestBody.create(MediaType.parse("text/plain"), "message=O HAI"))
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Betamax"
    response.header(X_BETAMAX) == "PLAY"
    response.body().string().contains('"message": "O HAI"')

    where:
    uri = "https://httpbin.org/post"
  }

  @Issue(["https://github.com/robfletcher/betamax/issues/61", "http://jira.codehaus.org/browse/JETTY-1533"])
  void "can cope with URLs that do not end in a slash"() {
    when:
    def request = new Request.Builder()
        .url(uri)
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Betamax"
    response.header(X_BETAMAX) == "PLAY"

    where:
    uri = "http://httpbin.org"
  }
}
