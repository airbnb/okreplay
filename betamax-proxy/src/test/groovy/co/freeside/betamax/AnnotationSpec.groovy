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

import co.freeside.betamax.junit.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.*
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.TapeMode.READ_WRITE
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Stepwise
class AnnotationSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') def tapeRoot = newTempDir('tapes')
	def recorder = new ProxyRecorder(tapeRoot: tapeRoot)
    @Rule RecorderRule recorderRule = new RecorderRule(recorder)
    @AutoCleanup('stop') def endpoint = new SimpleServer()
	RESTClient http

	void setup() {
		http = new BetamaxRESTClient(endpoint.url)
	}

	void 'no tape is inserted if there is no annotation on the feature'() {
		expect:
		recorder.tape == null
	}

	@Betamax(tape = 'annotation_spec', mode = READ_WRITE)
	void 'annotation on feature causes tape to be inserted'() {
		expect:
		recorder.tape.name == 'annotation_spec'
	}

	void 'tape is ejected after annotated feature completes'() {
		expect:
		recorder.tape == null
	}

	@Betamax(tape = 'annotation_spec', mode = READ_WRITE)
	void 'annotated feature can record'() {
		given:
		endpoint.start(EchoHandler)

		when:
		HttpResponseDecorator response = http.get(path: '/')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
		response.getFirstHeader(X_BETAMAX)?.value == 'REC'
	}

	@Betamax(tape = 'annotation_spec', mode = READ_WRITE)
	void 'annotated feature can play back'() {
		when:
		HttpResponseDecorator response = http.get(path: '/')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
		response.getFirstHeader(X_BETAMAX)?.value == 'PLAY'
	}

	void 'can make unproxied request after using annotation'() {
		given:
		endpoint.start(EchoHandler)

		when:
		HttpResponseDecorator response = http.get(path: '/')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA) == null
	}

}
