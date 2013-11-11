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
import javax.net.ssl.HttpsURLConnection
import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.Recorder

import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import spock.lang.*
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static com.google.common.net.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK

@Issue('https://github.com/robfletcher/betamax/issues/72')
@Ignore
class CustomSecureSocketFactorySpec extends Specification {

	@Shared @AutoCleanup('deleteDir') def tapeRoot = Files.createTempDir()
	@Shared @AutoCleanup('stop') def httpsEndpoint = new SimpleSecureServer(5001, HelloHandler)
	@Shared KeyStore trustStore

	@Shared URI httpsUri

	void setupSpec() {
		httpsEndpoint.start()

		httpsUri = httpsEndpoint.url.toURI()

		trustStore = KeyStore.getInstance(KeyStore.defaultType)
		trustStore.load(null, null)
	}

	void 'proxy can use a custom SSL socket factory'() {

		given: 'a recorder configured with a custom SSL socket factory'
		def sslSocketFactory = Spy(DummySSLSocketFactory, constructorArgs: [trustStore])
        def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).sslEnabled(true).build()
        def recorder = new Recorder(configuration)
		recorder.start 'custom secure socket factory spec'

		when: 'an HTTPS request is made'
        HttpsURLConnection connection = httpsEndpoint.url.toURL().openConnection()

		then: 'it is intercepted by the proxy'
		connection.responseCode == SC_OK
		connection.getHeaderField(VIA) == 'Betamax'

		and: 'the response body is readable'
		connection.inputStream.text == HELLO_WORLD

		and: 'the custom ssl socket factory was used'
		(1.._) * sslSocketFactory._

		cleanup:
		recorder.stop()

		where:
		url << [httpsEndpoint.url]
		scheme = url.toURI().scheme

	}

}
