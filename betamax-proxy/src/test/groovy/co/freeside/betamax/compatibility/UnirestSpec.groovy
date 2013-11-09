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
import org.apache.http.impl.client.HttpClients
import org.junit.ClassRule
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static com.google.common.net.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER

@Betamax(mode = TapeMode.READ_WRITE)
@Timeout(10)
@Unroll
class UnirestSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).sslEnabled(true).build()
    @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)

    @Shared @AutoCleanup("stop") def httpEndpoint = new SimpleServer(HelloHandler)
    @Shared @AutoCleanup("stop") def httpsEndpoint = new SimpleSecureServer(5001, HelloHandler)

    void setupSpec() {
        httpEndpoint.start()
        httpsEndpoint.start()

        Unirest.httpClient = HttpClients.custom().useSystemProperties().setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER).build()
    }

    void "proxy intercepts #scheme request using Unirest"() {
        given:
        def response = Unirest.get(url).asString()

        expect:
        response.code == SC_OK
        response.headers[VIA.toLowerCase()] == "Betamax"
        response.headers[X_BETAMAX.toLowerCase()] == "REC"
        response.body == HELLO_WORLD

        where:
        url << [httpEndpoint.url, httpsEndpoint.url]
        scheme = url.toURI().scheme
    }
}
