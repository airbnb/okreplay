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

package co.freeside.betamax

import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.TapeMode.READ_WRITE
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static java.net.HttpURLConnection.HTTP_OK
import static com.google.common.net.HttpHeaders.VIA

@Issue("https://github.com/robfletcher/betamax/issues/107")
@Unroll
@Timeout(10)
class NoJUnitSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).sslEnabled(true).build()
    @Shared Recorder recorder = new Recorder(configuration)

    @Shared @AutoCleanup("stop") def httpEndpoint = new SimpleServer(HelloHandler)
    @Shared @AutoCleanup("stop") def httpsEndpoint = new SimpleSecureServer(5001, HelloHandler)

    void setupSpec() {
        httpEndpoint.start()
        httpsEndpoint.start()
    }

    void setup() {
        recorder.start("no junit spec", READ_WRITE)
    }

    void cleanup() {
        recorder.stop()
    }

    void "proxy intercepts #scheme URL connections"() {
        given:
        HttpURLConnection connection = url.toURL().openConnection()
        connection.connect()

        expect:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "REC"
        connection.inputStream.text == HELLO_WORLD

        cleanup:
        connection.disconnect()

        where:
        url << [httpEndpoint.url, httpsEndpoint.url]
        scheme = url.toURI().scheme
    }
}
