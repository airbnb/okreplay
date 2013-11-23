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

package co.freeside.betamax.proxy.matchRules

import javax.net.ssl.HttpsURLConnection
import co.freeside.betamax.*
import com.google.common.io.Files
import spock.lang.*
import static co.freeside.betamax.TapeMode.READ_ONLY
import static co.freeside.betamax.TapeMode.READ_WRITE
/**
 * Testing a custom matcher when being used in the proxy.
 */
@Unroll
class CustomMatcherSpec extends Specification {
    @Shared
    def tapeRoot = new File(CustomMatcherSpec.class.getResource("/betamax/tapes/").toURI())

    def simplePost(String url, String payload) {
        HttpsURLConnection conn = new URL(url).openConnection()
        try {
            conn.doOutput = true
            conn.requestMethod = "POST"
            conn.fixedLengthStreamingMode = payload.bytes.length
            def out = new PrintWriter(conn.outputStream)
            out.print(payload)
            out.flush()
            out.close()

            return conn.inputStream.text
        } finally {
            conn.disconnect()
        }
    }

    void "Using a custom matcher it should replay"() {
        given:
        def imr = new InstrumentedMatchRule()
        def proxyConfig = ProxyConfiguration.builder()
                .sslEnabled(true)
                .tapeRoot(tapeRoot)
                .defaultMode(READ_ONLY)
                .defaultMatchRule(imr)
                .build()

        def recorder = new Recorder(proxyConfig)
        recorder.start("httpBinTape")
        imr.requestValidations << { r ->
            //Will run this request validation on both requests being matched
            //No matter what, either recorded, or sent, I should have a payload of "BUTTS"
            //I'm posting "BUTTS" and the recorded interaction should have "BUTTS"
            assert r.hasBody()
        }

        when:
        def response = simplePost("https://httpbin.org/post", "BUTTS")

        then:
        def content = response.toString()

        content == "Hey look some text: BUTTS"

        cleanup:
        recorder.stop()
    }

    void "Using a custom matcher it should record a new one"() {
        given:
        def tapeRoot = Files.createTempDir() //Using a temp dir this time
        def imr = new InstrumentedMatchRule()
        def proxyConfig = ProxyConfiguration.builder()
                .sslEnabled(true)
                .tapeRoot(tapeRoot)
                .defaultMode(READ_WRITE)
                .defaultMatchRule(imr)
                .build()

        def recorder = new Recorder(proxyConfig)
        recorder.start("httpBinTape")

        when:
        def response = simplePost("https://httpbin.org/post", "LOLWUT")

        then:
        def content = response.toString()
//        content ==

        recorder.stop()
        // The tape is written when it's referenced not in this dir
        // make sure there's a file in there
        def recordedTape = new File(tapeRoot, "httpBinTape.yaml")
        // It should have recorded it to the tape
        recordedTape.exists()
    }

}
