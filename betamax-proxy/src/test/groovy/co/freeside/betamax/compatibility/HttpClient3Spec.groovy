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
import co.freeside.betamax.junit.*
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import org.apache.commons.httpclient.*
import org.apache.commons.httpclient.methods.GetMethod
import org.junit.Rule
import spock.lang.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

class HttpClient3Spec extends Specification {

    @AutoCleanup('deleteDir') def tapeRoot = Files.createTempDir()
    def recorder = new ProxyRecorder(tapeRoot: tapeRoot)
    @Rule RecorderRule recorderRule = new RecorderRule(recorder)
    @Shared @AutoCleanup('stop') def endpoint = new SimpleServer(EchoHandler)

    void setupSpec() {
        endpoint.start()
    }

    @Timeout(10)
    @Betamax(tape = 'http client 3 spec', mode = TapeMode.READ_WRITE)
    void 'proxy intercepts HTTPClient 3.x connections'() {
        given:
        def client = new HttpClient()
        client.hostConfiguration.proxyHost = new ProxyHost(recorder.proxyHost, recorder.proxyPort)

        and:
        def request = new GetMethod(endpoint.url)

        when:
        def status = client.executeMethod(request)

        then:
        status == HTTP_OK
        request.getResponseHeader(VIA)?.value == 'Betamax'
    }

}
