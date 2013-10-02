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

package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.HelloHandler
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Issue('https://github.com/robfletcher/betamax/issues/54')
class PreExistingProxySpec extends Specification {

	@AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup('stop') SimpleServer proxyServer = new SimpleServer()

	void setupSpec() {
		proxyServer.start(HelloHandler)
		System.properties.'http.proxyHost' = InetAddress.localHost.hostAddress
		System.properties.'http.proxyPort' = proxyServer.port.toString()
	}

	void cleanupSpec() {
		System.clearProperty 'http.proxyHost'
		System.clearProperty 'http.proxyPort'
	}

	@Timeout(10)
	@Betamax(tape = 'existing proxy spec', mode = TapeMode.READ_WRITE)
	void 'pre-existing proxy settings are used for the outbound request from the Betamax proxy'() {
		given:
		HttpURLConnection connection = new URL('http://freeside.co/betamax').openConnection()
		connection.connect()

		expect:
		connection.responseCode == HTTP_OK
		connection.getHeaderField(VIA) == 'Betamax'
		connection.inputStream.text == HELLO_WORLD

		cleanup:
		connection.disconnect()
	}

}
