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
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.*
import groovyx.net.http.*
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static org.apache.http.HttpHeaders.VIA

@Issue('https://github.com/robfletcher/betamax/issues/16')
@Unroll
class IgnoreHostsSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer(EchoHandler)
	@AutoCleanup('ejectTape') Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot)
	@AutoCleanup('stop') ProxyServer proxy = new ProxyServer(recorder)
	RESTClient http

	void setupSpec() {
		endpoint.start()
	}

	void setup() {
		http = new BetamaxRESTClient()

		recorder.insertTape('ignore hosts spec')
	}

	void 'does not proxy a request to #requestURI when ignoring #ignoreHosts'() {
		given: 'proxy is configured to ignore local connections'
		recorder.ignoreHosts = [ignoreHosts]
		proxy.start()

		when: 'a request is made'
		HttpResponseDecorator response = http.get(uri: requestURI)

		then: 'the request is not intercepted by the proxy'
		!response.headers[VIA]

		and: 'nothing is recorded to the tape'
		recorder.tape.size() == old(recorder.tape.size())

		where:
		ignoreHosts               | requestURI
		endpoint.url.toURI().host | endpoint.url
		'localhost'               | "http://localhost:${endpoint.url.toURI().port}"
		'127.0.0.1'               | "http://localhost:${endpoint.url.toURI().port}"
		endpoint.url.toURI().host | "http://localhost:${endpoint.url.toURI().port}"
	}

	void 'does not proxy a request to #requestURI when ignoreLocalhost is true'() {
		given: 'proxy is configured to ignore local connections'
		recorder.ignoreLocalhost = true
		proxy.start()

		when: 'a request is made'
		HttpResponseDecorator response = http.get(uri: requestURI)

		then: 'the request is not intercepted by the proxy'
		!response.headers[VIA]

		and: 'nothing is recorded to the tape'
		recorder.tape.size() == old(recorder.tape.size())

		where:
		requestURI << [endpoint.url, "http://localhost:${endpoint.url.toURI().port}"]
	}

}
