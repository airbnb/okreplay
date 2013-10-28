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
import groovyx.net.http.HttpResponseException
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.junit.ClassRule
import org.junit.Rule
import spock.lang.*
import static java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT

@Issue("https://github.com/robfletcher/betamax/issues/20")
@Issue("https://github.com/adamfisk/LittleProxy/issues/96")
@Betamax(tape = "proxy timeout spec", mode = TapeMode.READ_WRITE)
class ProxyTimeoutSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def recorder = new ProxyRecorder(tapeRoot: tapeRoot, proxyTimeoutSeconds: 1)
    @Shared @ClassRule RecorderRule recorderRule = new RecorderRule(recorder)

    @AutoCleanup("stop") def endpoint = new SimpleServer(SlowHandler)
    def http = new BetamaxRESTClient(endpoint.url)

    void setup() {
        http.client.httpRequestRetryHandler = new DefaultHttpRequestRetryHandler(0, false)
    }

    void "proxy responds with 504 if target server takes too long to respond"() {
        given:
        endpoint.start()

        when:
        http.get(path: "/")

        then:
        def e = thrown(HttpResponseException)
        e.statusCode == HTTP_GATEWAY_TIMEOUT
    }

}
