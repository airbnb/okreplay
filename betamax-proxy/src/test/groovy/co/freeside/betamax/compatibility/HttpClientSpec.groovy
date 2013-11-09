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

package co.freeside.betamax.compatibility

import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.junit.*
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import org.apache.http.HttpHost
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.junit.ClassRule
import spock.lang.*
import static co.freeside.betamax.TapeMode.READ_WRITE
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static java.net.HttpURLConnection.HTTP_OK
import static com.google.common.net.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER

@Betamax(mode = READ_WRITE)
@Timeout(10)
class HttpClientSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).sslEnabled(true).build()
    @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)

    @Shared @AutoCleanup("stop") def httpEndpoint = new SimpleServer(EchoHandler)
    @Shared @AutoCleanup("stop") def httpsEndpoint = new SimpleSecureServer(5001, HelloHandler)

    void setupSpec() {
        httpEndpoint.start()
        httpsEndpoint.start()
    }

    void "proxy intercepts HTTPClient connections when using ProxySelectorRoutePlanner"() {
        given:
        def http = HttpClients.custom().setRoutePlanner(new SystemDefaultRoutePlanner(null)).build()

        when:
        def request = new HttpGet(httpEndpoint.url)

        def response = http.execute(request)

        then:
        response.statusLine.statusCode == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
    }

    void "proxy intercepts HTTPClient connections when explicitly told to"() {
        given:
        def proxyHost = new HttpHost(configuration.proxyHost, configuration.proxyPort, "http")
        def http = HttpClients.custom().setProxy(proxyHost).build()

        when:
        def request = new HttpGet(httpEndpoint.url)
        def response = http.execute(request)

        then:
        response.statusLine.statusCode == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
    }

    void "proxy automatically intercepts SystemDefaultHttpClient connections"() {
        given:
        def http = HttpClients.createSystem()

        when:
        def request = new HttpGet(httpEndpoint.url)
        def response = http.execute(request)

        then:
        response.statusLine.statusCode == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
    }

    void "proxy can intercept HTTPS requests"() {
        given:
        def http = HttpClients.custom().useSystemProperties().setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER).build()

        when: "an HTTPS request is made"
        def request = new HttpGet(httpsEndpoint.url)
        def response = http.execute(request)

        and: "we read the response body"
        def responseBytes = new ByteArrayOutputStream()
        response.entity.writeTo(responseBytes)
        def responseString = responseBytes.toString("UTF-8")

        then: "the request is intercepted by the proxy"
        response.statusLine.statusCode == SC_OK
        response.getFirstHeader(VIA)?.value == "Betamax"

        and: "the response is decoded"
        responseString == HELLO_WORLD
    }
}
