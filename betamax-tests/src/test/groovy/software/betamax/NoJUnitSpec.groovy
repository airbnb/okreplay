/*
 * Copyright 2013 the original author or authors.
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

import com.google.common.io.Files
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import software.betamax.proxy.BetamaxInterceptor
import spock.lang.*

import static Headers.X_BETAMAX
import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK

@Issue("https://github.com/robfletcher/betamax/issues/107")
@Unroll
@Timeout(10)
class NoJUnitSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def configuration = Configuration.builder()
      .tapeRoot(tapeRoot)
      .sslEnabled(true)
      .build()
  @Shared BetamaxInterceptor interceptor = new BetamaxInterceptor()
  @Shared Recorder recorder = new Recorder(configuration, interceptor)

  @Shared def httpEndpoint = new MockWebServer()

  def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  void setupSpec() {
    httpEndpoint.start()
    httpEndpoint.enqueue(new MockResponse().setBody("Hello World!"))
  }

  void setup() {
    recorder.start("no junit spec", TapeMode.READ_WRITE)
  }

  void cleanup() {
    recorder.stop()
  }

  void "proxy intercepts #scheme URL connections"() {
    given:
    def request = new Request.Builder().url(url).build()
    def response = client.newCall(request).execute()

    expect:
    response.code() == HTTP_OK
    response.header(VIA) == "Betamax"
    response.header(X_BETAMAX) == "REC"
    response.body().string() == "Hello World!"

    where:
    url << [httpEndpoint.url("/")]
  }
}
