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

import java.security.KeyStore
import co.freeside.betamax.ProxyRecorder
import co.freeside.betamax.httpclient.BetamaxHttpsSupport
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory
import co.freeside.betamax.util.server.*
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.SystemDefaultHttpClient
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK

@Issue('https://github.com/robfletcher/betamax/issues/72')
class CustomSecureSocketFactorySpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Shared @AutoCleanup('stop') SimpleServer httpsEndpoint = new SimpleSecureServer(5001)
	@Shared KeyStore trustStore

	@Shared URI httpsUri

	HttpClient http

	void setupSpec() {
		httpsEndpoint.start(HelloHandler)

		httpsUri = httpsEndpoint.url.toURI()

		trustStore = KeyStore.getInstance(KeyStore.defaultType)
		trustStore.load(null, null)
	}

	void setup() {
		http = new SystemDefaultHttpClient()
		BetamaxHttpsSupport.configure(http)
	}

	@Ignore("until HTTPS support implemented")
	void 'proxy can use a custom SSL socket factory'() {

		given: 'a recorder configured with a custom SSL socket factory'
		def sslSocketFactory = Spy(DummySSLSocketFactory, constructorArgs: [trustStore])
		def recorder = new ProxyRecorder(tapeRoot: tapeRoot, sslSupport: true, sslSocketFactory: sslSocketFactory)
		recorder.start 'custom secure socket factory spec'

		when: 'an HTTPS request is made'
		def request = new HttpGet(httpsEndpoint.url)
		def response = http.execute(request)

		then: 'it is intercepted by the proxy'
		response.statusLine.statusCode == SC_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'

		and: 'the response body is readable'
		response.entity.content.text == HELLO_WORLD

		and: 'the custom ssl socket factory was used'
		(1.._) * sslSocketFactory._

		cleanup:
		recorder.stop()

		where:
		url << [httpsEndpoint.url]
		scheme = url.toURI().scheme

	}

}
