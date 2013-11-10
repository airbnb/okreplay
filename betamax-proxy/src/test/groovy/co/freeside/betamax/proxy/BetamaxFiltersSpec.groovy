/*
 * Copyright 2013 the original author or authors.
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

import java.nio.charset.Charset
import co.freeside.betamax.encoding.*
import co.freeside.betamax.message.*
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.util.message.BasicResponse
import com.google.common.io.ByteStreams
import io.netty.buffer.*
import io.netty.handler.codec.http.*
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static io.netty.buffer.Unpooled.copiedBuffer
import static io.netty.handler.codec.http.HttpHeaders.Names.*
import static io.netty.handler.codec.http.HttpMethod.GET
import static io.netty.handler.codec.http.HttpResponseStatus.OK
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1

@Unroll
class BetamaxFiltersSpec extends Specification {

	@Subject
	BetamaxFilters filters
	HttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "http://freeside.co/betamax")
	Tape tape = Mock(Tape)

	void setup() {
		filters = new BetamaxFilters(request, tape)
	}

	void "requestPre returns null if no match is found on tape"() {
		given:
		tape.isReadable() >> true
		tape.seek(_) >> false

		expect:
		filters.requestPre(request) == null
	}

	void "requestPre returns null if tape is not readable"() {
		given:
		tape.isReadable() >> false

		when:
		def response = filters.requestPre(request)

		then:
		response == null

		and:
		0 * tape.seek(_)
	}

	void "requestPre returns a recorded response if found on tape"() {
		given:
		def recordedResponse = new BasicResponse(200, "OK")
		recordedResponse.body = "message body".bytes

		and:
		tape.isReadable() >> true
		tape.seek(_) >> true
		tape.play(_) >> recordedResponse

		expect:
		FullHttpResponse response = filters.requestPre(request)
		response != null
		response.status.code() == 200
		response.status.reasonPhrase() == "OK"
		readContent(response) == recordedResponse.body

		and:
		response.headers().get(X_BETAMAX) == "PLAY"
	}

	void "requestPre encodes the response if the original response was encoded with #encoding"() {
		given:
		def recordedResponse = new BasicResponse(200, "OK")
		recordedResponse.addHeader CONTENT_ENCODING, encoding
		recordedResponse.body = responseBody.bytes

		and:
		tape.isReadable() >> true
		tape.seek(_) >> true
		tape.play(_) >> recordedResponse

		expect:
		FullHttpResponse response = filters.requestPre(request)
		response != null
		response.status.code() == 200
		response.status.reasonPhrase() == "OK"
		readContent(response) == encodedBody

		and:
		response.headers().get(X_BETAMAX) == "PLAY"

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
		"none"    | new NoOpEncoder()

		responseBody = "message body"
		encodedBody = encoder.encode(responseBody)
	}

	void "requestPost adds headers to outgoing request"() {
		when:
		filters.requestPost(request)

		then:
		request.headers().get(VIA) == "Betamax"
	}

	void "responsePre records the exchange to tape"() {
		given:
		def response = new DefaultFullHttpResponse(HTTP_1_1, OK, copiedBuffer("response body", Charset.forName("utf-8")))

		and:
		tape.isWritable() >> true

		when:
		filters.responsePre(response)

		then:
		1 * tape.record(_, _) >> { Request recordedRequest, Response recordedResponse ->
			with(recordedRequest) {
				method == request.method.toString()
				uri.toString() == request.uri
			}

			with(recordedResponse) {
				status == response.status.code()
				bodyAsBinary.input.bytes == readContent(response)
			}
		}
    }

    void "responsePost adds the X-Betamax header"() {
        given:
        def response = new DefaultFullHttpResponse(HTTP_1_1, OK)

        when:
        filters.responsePost(response)

        then:
		response.headers().get(X_BETAMAX) == "REC"
	}

	private static byte[] readContent(FullHttpMessage message) {
		ByteBuf buffer = message.content()
		def stream = new ByteArrayOutputStream()
		new ByteBufInputStream(buffer).withStream {
			ByteStreams.copy(it, stream)
		}
		stream.toByteArray()
	}
}
