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
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import org.junit.ClassRule
import spock.lang.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Betamax(tape = 'request methods spec', mode = TapeMode.READ_WRITE)
@Unroll
@Timeout(10)
class RequestMethodsSpec extends Specification {

    @Shared @AutoCleanup('deleteDir') def tapeRoot = Files.createTempDir()
    @Shared def recorder = new ProxyRecorder(tapeRoot: tapeRoot)
    @Shared @ClassRule RecorderRule recorderRule = new RecorderRule(recorder)

    @Shared @AutoCleanup('stop') def endpoint = new SimpleServer(EchoHandler)

    void setupSpec() {
        endpoint.start()
    }

    void 'proxy handles #method requests'() {
        given:
        def http = new BetamaxRESTClient(endpoint.url)

        when:
        def response = http."$method"(path: '/')

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == 'Betamax'

        cleanup:
        http.shutdown()

        where:
        method << ['get', 'post', 'put', 'head', 'delete', 'options']
    }

}
