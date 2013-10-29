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

import co.freeside.betamax.*
import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import co.freeside.betamax.junit.*
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import groovyx.net.http.*
import org.apache.http.HttpHost
import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.apache.http.params.HttpParams
import org.junit.ClassRule
import spock.lang.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY

@Betamax(tape = "http builder spec", mode = TapeMode.READ_WRITE)
@Timeout(10)
class HttpBuilderSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def recorder = new ProxyRecorder(tapeRoot: tapeRoot)
    @Shared @ClassRule RecorderRule recorderRule = new RecorderRule(recorder)

    @Shared @AutoCleanup("stop") def endpoint = new SimpleServer(EchoHandler)

    void setupSpec() {
        endpoint.start()
    }

    void "proxy intercepts HTTPClient connections when using ProxySelectorRoutePlanner"() {
        given:
        def http = new RESTClient(endpoint.url)
        BetamaxRoutePlanner.configure(http.client)

        when:
        HttpResponseDecorator response = http.get(path: "/")

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
    }

    void "proxy intercepts HTTPClient connections when explicitly told to"() {
        given:
        def http = new RESTClient(endpoint.url)
        http.client.params.setParameter(DEFAULT_PROXY, new HttpHost(recorder.proxyHost, recorder.proxyPort, "http"))

        when:
        HttpResponseDecorator response = http.get(path: "/")

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
    }

    void "proxy intercepts HttpURLClient connections"() {
        given:
        def http = new HttpURLClient(url: endpoint.url)

        when:
        def response = http.request(path: "/")

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
    }

    void "proxy automatically intercepts connections when the underlying client is a SystemDefaultHttpClient"() {
        given:
        def http = new BetamaxRESTClient(endpoint.url)

        when:
        HttpResponseDecorator response = http.get(path: "/")

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
    }

}
