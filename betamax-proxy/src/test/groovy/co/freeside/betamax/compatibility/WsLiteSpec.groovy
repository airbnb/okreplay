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

import co.freeside.betamax.ProxyRecorder
import co.freeside.betamax.junit.*
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import org.junit.ClassRule
import org.junit.Rule
import spock.lang.*
import wslite.rest.RESTClient
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.TapeMode.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Betamax(tape = 'wslite spec', mode = READ_WRITE)
class WsLiteSpec extends Specification {

    @Shared @AutoCleanup('deleteDir') def tapeRoot = Files.createTempDir()
    @Shared def recorder = new ProxyRecorder(tapeRoot: tapeRoot, defaultMode: WRITE_ONLY, sslSupport: true)
    @Shared @ClassRule RecorderRule recorderRule = new RecorderRule(recorder)

    @Shared @AutoCleanup('stop') def endpoint = new SimpleServer(EchoHandler)
    @Shared @AutoCleanup('stop') def httpsEndpoint = new SimpleSecureServer(5001, HelloHandler)

    void setupSpec() {
        endpoint.start()
        httpsEndpoint.start()
    }

    void 'can record a connection made with WsLite'() {
        given:
        'a properly configured wslite instance'
        def http = new RESTClient(endpoint.url)

        when:
        'a request is made'
        def response = http.get(path: '/', proxy: recorder.proxy)

        then:
        'the request is intercepted'
        response.statusCode == HTTP_OK
        response.headers[VIA] == 'Betamax'
        response.headers[X_BETAMAX] == 'REC'
    }

    void 'proxy intercepts HTTPS requests'() {
        given:
        'a properly configured wslite instance'
        def http = new RESTClient(httpsEndpoint.url)

        when:
        'a request is made'
        def response = http.get(path: '/', proxy: recorder.proxy)

        then:
        'the request is intercepted'
        response.statusCode == HTTP_OK
        response.headers[VIA] == 'Betamax'
        response.headers[X_BETAMAX] == 'REC'

        and:
        'the response body is decoded'
        response.contentAsString == 'Hello World!'
    }

}
