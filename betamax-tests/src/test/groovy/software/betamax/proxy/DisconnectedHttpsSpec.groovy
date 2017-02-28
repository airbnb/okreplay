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

package software.betamax.proxy

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Rule
import software.betamax.Configuration
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification

import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK
import static software.betamax.Headers.X_BETAMAX

@Ignore
@Issue("https://github.com/robfletcher/betamax/issues/117")
class DisconnectedHttpsSpec extends Specification {
  static final TAPE_ROOT = new File(DisconnectedHttpsSpec.getResource("/betamax/tapes").toURI())
  def configuration = Configuration.builder().tapeRoot(TAPE_ROOT).sslEnabled(true).build()
  def interceptor = new BetamaxInterceptor()
  @Rule RecorderRule recorder = new RecorderRule(configuration, interceptor)
  def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  @Betamax(tape = "disconnected https spec")
  void "can play back a recorded HTTPS response without contacting the original server"() {
    when:
    def request = new Request.Builder().url("https://freeside.bv/").build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Betamax"
    response.header(X_BETAMAX) == "PLAY"
    response.body().string() == "O HAI!"
  }
}
