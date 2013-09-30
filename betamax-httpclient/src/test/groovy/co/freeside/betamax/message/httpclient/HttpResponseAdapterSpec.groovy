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

package co.freeside.betamax.message.httpclient

import org.apache.http.entity.*
import org.apache.http.message.BasicHttpResponse
import spock.lang.*
import static co.freeside.betamax.message.AbstractMessage.*
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1

@Unroll
class HttpResponseAdapterSpec extends Specification {

	void 'a content type header of "#contentTypeHeader" is interpreted as #expectedContentType'() {
		given:
		def response = new BasicHttpResponse(HTTP_1_1, 200, 'OK')
		if (contentTypeHeader) {
			response.setHeader(CONTENT_TYPE, contentTypeHeader)
		}

		and:
		def responseAdapter = new HttpResponseAdapter(response)

		expect:
		responseAdapter.contentType == expectedContentType

		where:
		contentTypeHeader        | expectedContentType
		'image/png'              | 'image/png'
		'text/xml;charset=utf-8' | 'text/xml'
		null                     | DEFAULT_CONTENT_TYPE
	}

	void 'interprets #headerName header as #expectedValue'() {
		given:
		def response = new BasicHttpResponse(HTTP_1_1, 200, 'OK')
		headerValues.each {
			response.addHeader(headerName, it)
		}

		and:
		def responseAdapter = new HttpResponseAdapter(response)

		expect:
		responseAdapter.headers[headerName] == expectedValue
		responseAdapter.getHeader(headerName) == expectedValue

		where:
		headerName    | headerValues                 | expectedValue
		CONTENT_TYPE  | ['text/html']                | 'text/html'
		LAST_MODIFIED | ['12 Sep 2012 22:44:42 GMT'] | '12 Sep 2012 22:44:42 GMT'
		VIA           | ['Proxy 1', 'Proxy 2']       | 'Proxy 1, Proxy 2'
	}

	void 'identifies if a response body is present'() {
		given:
		def response = new BasicHttpResponse(HTTP_1_1, 200, 'OK')
		response.entity = new StringEntity('O HAI')

		and:
		def responseAdapter = new HttpResponseAdapter(response)

		expect:
		responseAdapter.hasBody()
	}

	void 'interprets response body using #charset when Content-Type is declared as #contentTypeHeader'() {
		given:
		def body = 'Price: \u00a399.99'
		def response = new BasicHttpResponse(HTTP_1_1, 200, 'OK')
		response.setHeader(CONTENT_TYPE, contentTypeHeader)
		response.entity = new ByteArrayEntity(body.getBytes(charset))

		and:
		def responseAdapter = new HttpResponseAdapter(response)

		expect:
		responseAdapter.bodyAsText.text == body

		where:
		contentTypeHeader             | charset
		null                          | DEFAULT_CHARSET
		'text/plain'                  | DEFAULT_CHARSET
		'text/plain;charset=utf-8'    | 'UTF-8'
		'text/plain;charset=utf-16LE' | 'UTF-16LE'
	}

	void 'response body is re-readable'() {
		given: 'a response with a non-repeatable entity'
		def body = 'O HAI'.bytes
		def entity = new BasicHttpEntity()
		entity.content = new ByteArrayInputStream(body)

		def response = new BasicHttpResponse(HTTP_1_1, 200, 'OK')
		response.setHeader(CONTENT_TYPE, 'text/plain')
		response.entity = entity

		and:
		def responseAdapter = new HttpResponseAdapter(response)

		expect: 'the response body can be read'
		responseAdapter.bodyAsBinary.bytes == body

		and: 'read again'
		responseAdapter.bodyAsBinary.bytes == body
	}

	void 'cannot get the body of a response that does not have one'() {
		given:
		def response = new BasicHttpResponse(HTTP_1_1, 200, 'OK')

		and:
		def responseAdapter = new HttpResponseAdapter(response)

		when:
		responseAdapter.bodyAsBinary

		then:
		thrown IllegalStateException
	}

	void 'can add extra headers'() {
		given:
		def response = new BasicHttpResponse(HTTP_1_1, 200, 'OK')
		response.addHeader('X-What-Ever', 'Pff')

		and:
		def responseAdapter = new HttpResponseAdapter(response)

		when:
		responseAdapter.addHeader('X-What-Ever', 'Meh')

		then:
		responseAdapter.getHeader('X-What-Ever') == 'Pff, Meh'
	}

}
