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

package co.freeside.betamax.tape

import co.freeside.betamax.message.tape.RecordedRequest
import co.freeside.betamax.util.message.BasicRequest
import com.google.common.collect.ImmutableList
import spock.lang.*
import static co.freeside.betamax.MatchRules.*
import static org.apache.http.HttpHeaders.*

@Issue('https://github.com/robfletcher/betamax/issues/9')
class RequestMatcherSpec extends Specification {

	void 'can match method and url'() {
		given:
		def request1 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
		def request2 = new RecordedRequest(method: 'HEAD', uri: 'http://freeside.co/betamax'.toURI())
		def request3 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax?q=1'.toURI())

		and:
		def request = new BasicRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
		def requestMatcher = new RequestMatcher(request, ImmutableList.of(method, uri))

		expect:
		requestMatcher.matches(request1)
		!requestMatcher.matches(request2)
		!requestMatcher.matches(request3)
	}

	void 'can match host'() {
		given:
		def request1 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
		def request2 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/grails-fields'.toURI())
		def request3 = new RecordedRequest(method: 'HEAD', uri: 'http://freeside.co/betamax'.toURI())
		def request4 = new RecordedRequest(method: 'GET', uri: 'http://icanhascheezburger.com/'.toURI())

		and:
		def request = new BasicRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
		def requestMatcher = new RequestMatcher(request, ImmutableList.of(host))

		expect:
		requestMatcher.matches(request1)
		requestMatcher.matches(request2)
		requestMatcher.matches(request3)
		!requestMatcher.matches(request4)
	}

	void 'can match path'() {
		given:
		def request1 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
		def request2 = new RecordedRequest(method: 'GET', uri: 'http://robfletcher.github.com/grails-enhanced-scaffolding'.toURI())
		def request3 = new RecordedRequest(method: 'HEAD', uri: 'http://freeside.co/betamax'.toURI())
		def request4 = new RecordedRequest(method: 'GET', uri: 'http://icanhascheezburger.com/betamax'.toURI())

		and:
		def request = new BasicRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI())
		def requestMatcher = new RequestMatcher(request, ImmutableList.of(path))

		expect:
		requestMatcher.matches(request1)
		!requestMatcher.matches(request2)
		requestMatcher.matches(request3)
		requestMatcher.matches(request4)
	}

	void 'can match headers'() {
		given:
		def request1 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI(), headers: [(ACCEPT_ENCODING): 'gzip, deflate'])
		def request2 = new RecordedRequest(method: 'GET', uri: 'http://icanhascheezburger.com/'.toURI(), headers: [(ACCEPT_ENCODING): 'gzip, deflate'])
		def request3 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI(), headers: [(ACCEPT_ENCODING): 'none'])
		def request4 = new RecordedRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI(), headers: [(ACCEPT_ENCODING): 'gzip, deflate', (CACHE_CONTROL): 'no-cache'])

		and:
		def request = new BasicRequest(method: 'GET', uri: 'http://freeside.co/betamax'.toURI(), headers: [(ACCEPT_ENCODING): ['gzip', 'deflate']])
		def requestMatcher = new RequestMatcher(request, ImmutableList.of(headers))

		expect:
		requestMatcher.matches(request1)
		requestMatcher.matches(request2)
		!requestMatcher.matches(request3)
		!requestMatcher.matches(request4)
	}

	void 'can match post body'() {
		given:
		def request1 = new RecordedRequest(method: 'POST', uri: 'http://freeside.co/betamax'.toURI(), body: 'q=1')
		def request2 = new RecordedRequest(method: 'POST', uri: 'http://freeside.co/betamax'.toURI(), body: 'q=2')
		def request3 = new RecordedRequest(method: 'POST', uri: 'http://freeside.co/betamax'.toURI(), body: 'q=1&r=1')

		and:
		def request = new BasicRequest(method: 'POST', uri: 'http://freeside.co/betamax'.toURI(), body: 'q=1')
		def requestMatcher = new RequestMatcher(request, ImmutableList.of(body))

		expect:
		requestMatcher.matches(request1)
		!requestMatcher.matches(request2)
		!requestMatcher.matches(request3)
	}

}
