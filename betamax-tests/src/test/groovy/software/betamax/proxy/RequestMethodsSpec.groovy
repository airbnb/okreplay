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

package software.betamax.proxy

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.ClassRule
import software.betamax.Configuration
import software.betamax.junit.Betamax
import software.betamax.android.RecorderRule
import spock.lang.*

import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK
import static software.betamax.TapeMode.READ_WRITE

@Betamax(mode = READ_WRITE)
@Unroll
@Timeout(10)
class RequestMethodsSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def configuration = Configuration.builder().tapeRoot(tapeRoot).build()
  @Shared def interceptor = new BetamaxInterceptor()
  @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration, interceptor)
  @Shared def endpoint = new MockWebServer()

  def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  void setupSpec() {
    endpoint.start()
  }

  void "proxy handles #method requests"() {
    def mockResponse = new MockResponse().setBody("OK")
    if (method == 'HEAD') {
      mockResponse.clearHeaders()
    }
    when:
    endpoint.enqueue(mockResponse)
    MediaType mediaType = MediaType.parse("text/plain")
    def body = method == "GET" || method == 'HEAD' ? null : RequestBody.create(mediaType, "")
    def request = new Request.Builder()
        .url(endpoint.url("/"))
        .method(method, body)
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Betamax"

    where:
    method << ["GET", "POST", "PUT", "HEAD", "DELETE", "OPTIONS"]
  }
}
