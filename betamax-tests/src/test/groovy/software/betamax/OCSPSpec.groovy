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

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.ClassRule
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import software.betamax.proxy.BetamaxInterceptor
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK

@Issue("https://github.com/robfletcher/betamax/issues/52")
@Betamax(tape = "ocsp")
class OCSPSpec extends Specification {

  static final TAPE_ROOT = new File(SmokeSpec.getResource("/betamax/tapes").toURI())
  @Shared def configuration = Configuration.builder().tapeRoot(TAPE_ROOT).build()
  @Shared def interceptor = new BetamaxInterceptor()
  @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration, interceptor)
  @Shared def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  void "OCSP messages"() {
    when:
    def request = new Request.Builder()
        .url(url)
        .method("POST", RequestBody.create(MediaType.parse("text/plain"), ""))
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Betamax"
    response.body().bytes().length == 2529

    where:
    url = "http://ocsp.ocspservice.com/public/ocsp"
  }
}
