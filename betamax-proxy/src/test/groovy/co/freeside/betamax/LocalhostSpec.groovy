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

package co.freeside.betamax

import co.freeside.betamax.junit.Betamax
import co.freeside.betamax.junit.RecorderRule
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.*
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Issue('https://github.com/robfletcher/betamax/issues/62')
@Issue('http://bugs.sun.com/view_bug.do?bug_id=6737819')
@Unroll
class LocalhostSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') def tapeRoot = newTempDir('tapes')
    def recorder = new ProxyRecorder(tapeRoot: tapeRoot)
    @Rule RecorderRule recorderRule = new RecorderRule(recorder)

	@Shared @AutoCleanup('stop') def endpoint = new SimpleServer()

	@Shared def http = new BetamaxRESTClient()

	void setupSpec() {
		endpoint.start(EchoHandler)
	}

	@IgnoreIf({ javaVersion >= 1.6 && javaVersion < 1.7 })
	@Betamax(tape = 'localhost', mode = WRITE_ONLY)
	void 'can proxy requests to local endpoint at #uri'() {
		when:
		HttpResponseDecorator response = http.get(uri: uri)

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'

		where:
		uri << [endpoint.url, "http://localhost:$endpoint.port/", "http://127.0.0.1:$endpoint.port/"]
	}

}
