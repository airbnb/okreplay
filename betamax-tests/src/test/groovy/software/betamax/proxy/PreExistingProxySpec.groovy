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

package software.betamax.proxy

import com.google.common.io.Files
import org.junit.Rule
import software.betamax.ProxyConfiguration
import software.betamax.TapeMode
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import software.betamax.util.server.HelloHandler
import software.betamax.util.server.SimpleServer
import spock.lang.*

import static HelloHandler.HELLO_WORLD
import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK

@Issue('https://github.com/robfletcher/betamax/issues/54')
class PreExistingProxySpec extends Specification {

    @Shared @AutoCleanup('deleteDir') def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).build()
    @Rule RecorderRule recorder = new RecorderRule(configuration)

    @Shared @AutoCleanup('stop') def proxyServer = new SimpleServer(HelloHandler)

    void setupSpec() {
        proxyServer.start()
        System.properties.'http.proxyHost' = "localhost"
        System.properties.'http.proxyPort' = proxyServer.port.toString()
    }

    void cleanupSpec() {
        System.clearProperty 'http.proxyHost'
        System.clearProperty 'http.proxyPort'
    }

    @Timeout(10)
    @Betamax(tape = 'existing proxy spec', mode = TapeMode.READ_WRITE)
    void 'pre-existing proxy settings are used for the outbound request from the Betamax proxy'() {
        given:
        HttpURLConnection connection = new URL('http://freeside.co/betamax').openConnection()
        connection.connect()

        expect:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == 'Betamax'
        connection.inputStream.text == HELLO_WORLD

        cleanup:
        connection.disconnect()
    }

}
