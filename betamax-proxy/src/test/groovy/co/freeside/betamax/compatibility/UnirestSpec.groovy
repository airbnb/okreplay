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

package co.freeside.betamax.compatibility

import co.freeside.betamax.*
import co.freeside.betamax.junit.*
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import com.mashape.unirest.http.Unirest
import org.apache.http.impl.client.SystemDefaultHttpClient
import org.junit.ClassRule
import spock.lang.*
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK

@Betamax(tape = "unirest spec", mode = TapeMode.READ_WRITE)
@Timeout(10)
class UnirestSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def recorder = new ProxyRecorder(tapeRoot: tapeRoot, sslSupport: true)
    @Shared @ClassRule RecorderRule recorderRule = new RecorderRule(recorder)

    @Shared @AutoCleanup("stop") def httpEndpoint = new SimpleServer(HelloHandler)
    @Shared @AutoCleanup("stop") def httpsEndpoint = new SimpleSecureServer(5001, HelloHandler)

    void setupSpec() {
        httpEndpoint.start()
        httpsEndpoint.start()

        Unirest.httpClient = new SystemDefaultHttpClient()
    }

    void "proxy intercepts Unirest"() {
        given:
        def response = Unirest.get(httpEndpoint.url).asString()

        expect:
        response.code == SC_OK
        response.headers[VIA.toLowerCase()] == "Betamax"
        response.body == HELLO_WORLD

        where:
        url << [httpEndpoint.url, httpsEndpoint.url]
    }
}
