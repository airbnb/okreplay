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

package software.betamax.proxy.matchRules

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import software.betamax.Configuration
import software.betamax.Recorder
import software.betamax.proxy.BetamaxInterceptor
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static software.betamax.TapeMode.READ_ONLY
import static software.betamax.TapeMode.READ_WRITE

/**
 * Testing a custom matcher when being used in the proxy.
 */
@Unroll
class CustomMatcherSpec extends Specification {
  @Shared def tapeRoot = new File(CustomMatcherSpec.class.getResource("/betamax/tapes/").toURI())
  static def simplePost(OkHttpClient client, String url, String payload) {
    def request = new Request.Builder()
        .method("POST", RequestBody.create(MediaType.parse('text/plain'), payload))
        .url(url)
        .build()
    return client.newCall(request).execute()
  }

  void "Using a custom matcher it should replay"() {
    given:
    def imr = new InstrumentedMatchRule()
    def configuration = Configuration.builder()
        .sslEnabled(true)
        .tapeRoot(tapeRoot)
        .defaultMode(READ_ONLY)
        .defaultMatchRule(imr)
        .build()
    def interceptor = new BetamaxInterceptor(configuration)
    def recorder = new Recorder(configuration, interceptor)
    def client = new OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()
    recorder.start("httpBinTape")
    imr.requestValidations << { r ->
      //Will run this request validation on both requests being matched
      //No matter what, either recorded, or sent, I should have a payload of "BUTTS"
      //I'm posting "BUTTS" and the recorded interaction should have "BUTTS"
      assert r.hasBody()
    }

    when:
    def response = simplePost(client, "https://httpbin.org/post", "BUTTS")

    then:
    def content = response.body().string()

    content == "Hey look some text: BUTTS"

    cleanup:
    recorder.stop()
  }

  void "Using a custom matcher it should record a new one"() {
    given:
    def tapeRoot = Files.createTempDir() //Using a temp dir this time
    def imr = new InstrumentedMatchRule()
    def configuration = Configuration.builder()
        .sslEnabled(true)
        .tapeRoot(tapeRoot)
        .defaultMode(READ_WRITE)
        .defaultMatchRule(imr)
        .build()
    def interceptor = new BetamaxInterceptor(configuration)
    def recorder = new Recorder(configuration, interceptor)
    def client = new OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()
    recorder.start("httpBinTape")

    when:
    def response = simplePost(client, "https://httpbin.org/post", "LOLWUT")

    then:
    response.toString()
    recorder.stop()
    // The tape is written when it's referenced not in this dir
    // make sure there's a file in there
    def recordedTape = new File(tapeRoot, "httpBinTape.yaml")
    // It should have recorded it to the tape
    recordedTape.exists()
  }
}
