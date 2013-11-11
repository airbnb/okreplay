/*
 * Copyright 2011 the original author or authors.
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

package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.junit.*
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.junit.ClassRule
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static com.google.common.net.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER

@Issue("https://github.com/robfletcher/betamax/issues/34")
@Unroll
@Betamax(mode = WRITE_ONLY)
class HttpsSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().sslEnabled(true).tapeRoot(tapeRoot).build()
    @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)

    @Shared @AutoCleanup("stop") def httpsEndpoint = new SimpleSecureServer(5001, HelloHandler)
    @Shared @AutoCleanup("stop") def httpEndpoint = new SimpleServer(HelloHandler)

    @Shared URI httpUri
    @Shared URI httpsUri

    HttpClient http

    void setupSpec() {
        httpEndpoint.start()
        httpsEndpoint.start()

        httpUri = httpEndpoint.url.toURI()
        httpsUri = httpsEndpoint.url.toURI()
    }

    void setup() {
        http = HttpClients.custom().useSystemProperties().setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER).build()
    }

    void "proxy is selected for #scheme URIs"() {
        given:
        def proxySelector = ProxySelector.default

        expect:
        def proxy = proxySelector.select(uri).first()
        proxy.type() == Proxy.Type.HTTP

        and:
        def proxyURI = "${scheme}://${proxy.address()}".toURI()
        InetAddress.getByName(proxyURI.host) == configuration.proxyHost
        proxyURI.port == configuration.proxyPort

        where:
        uri << [httpUri, httpsUri]
        scheme = uri.scheme
    }

    void "proxy can intercept #scheme requests"() {
        when: "a request is made"
        def request = new HttpGet(url)
        def response = http.execute(request)

        then: "it is intercepted by the proxy"
        response.statusLine.statusCode == SC_OK
        response.getFirstHeader(VIA)?.value == "Betamax"

        and: "the response body is readable"
        response.entity.content.text == HELLO_WORLD

        where:
        url << [httpEndpoint.url, httpsEndpoint.url]
        scheme = url.toURI().scheme
    }

}


