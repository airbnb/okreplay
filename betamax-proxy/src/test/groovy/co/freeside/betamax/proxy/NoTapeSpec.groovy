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
import co.freeside.betamax.proxy.jetty.*
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.*
import spock.lang.*
import static java.net.HttpURLConnection.HTTP_FORBIDDEN

@Issue('https://github.com/robfletcher/betamax/issues/18')
class NoTapeSpec extends Specification {

	@Shared Recorder recorder = new ProxyRecorder()
	@Shared @AutoCleanup('stop') ProxyServer proxy = new ProxyServer(recorder)
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	RESTClient http = new BetamaxRESTClient(endpoint.url)

	void setupSpec() {
		proxy.start()
		endpoint.start(EchoHandler)
	}

	void 'an error is returned if the proxy intercepts a request when no tape is inserted'() {
		when:
		http.get(path: '/')

		then:
		def e = thrown(HttpResponseException)
		e.statusCode == HTTP_FORBIDDEN
		e.message == 'No tape'
	}
}
