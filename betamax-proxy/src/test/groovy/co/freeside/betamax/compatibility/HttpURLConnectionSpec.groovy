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
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.*
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK

class HttpURLConnectionSpec extends Specification {

	@AutoCleanup('deleteDir') def tapeRoot = newTempDir('tapes')
    def recorder = new ProxyRecorder(tapeRoot: tapeRoot, defaultMode: WRITE_ONLY, sslSupport: true)
    @Rule RecorderRule recorderRule = new RecorderRule(recorder)
	@Shared @AutoCleanup('stop') def endpoint = new SimpleServer()
	@Shared @AutoCleanup('stop') def httpsEndpoint = new SimpleSecureServer(5001)

	void setupSpec() {
		endpoint.start(EchoHandler)
		httpsEndpoint.start(HelloHandler)
	}

	@Timeout(10)
	@Betamax(tape = 'http url connection spec', mode = TapeMode.READ_WRITE)
	void 'proxy intercepts URL connections'() {
		given:
		HttpURLConnection connection = new URL(endpoint.url).openConnection()
		connection.connect()

		expect:
		connection.responseCode == HTTP_OK
		connection.getHeaderField(VIA) == 'Betamax'

		cleanup:
		connection.disconnect()
	}

	@Betamax(tape = 'http url connection spec', mode = WRITE_ONLY)
	void 'proxy intercepts HTTPS requests'() {
		when:
		HttpURLConnection connection = httpsEndpoint.url.toURL().openConnection()

		then:
		connection.responseCode == SC_OK
		connection.getHeaderField(VIA) == 'Betamax'
		connection.inputStream.text == 'Hello World!'
	}
}
