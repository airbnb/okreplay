/*
 * Copyright 2012 the original author or authors.
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

package software.betamax.compatibility

import com.google.common.io.Files
import org.junit.ClassRule
import software.betamax.Configuration
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import software.betamax.util.server.HelloHandler
import software.betamax.util.server.SimpleSecureServer
import software.betamax.util.server.SimpleServer
import spock.lang.*

import static HelloHandler.HELLO_WORLD
import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK
import static software.betamax.Headers.X_BETAMAX
import static software.betamax.TapeMode.READ_WRITE

@Betamax(mode = READ_WRITE)
@Timeout(10)
@Unroll
class HttpURLConnectionSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def configuration = Configuration.builder().tapeRoot(tapeRoot).sslEnabled(true).build()
    @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)

    @Shared @AutoCleanup("stop") def httpEndpoint = new SimpleServer(HelloHandler)
    @Shared @AutoCleanup("stop") def httpsEndpoint = new SimpleSecureServer(5001, HelloHandler)

    void setupSpec() {
        httpEndpoint.start()
        httpsEndpoint.start()
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
