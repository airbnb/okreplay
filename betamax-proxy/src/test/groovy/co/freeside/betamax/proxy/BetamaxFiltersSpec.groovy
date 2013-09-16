package co.freeside.betamax.proxy

import java.nio.charset.Charset
import co.freeside.betamax.handler.NonWritableTapeException
import co.freeside.betamax.message.*
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.util.message.BasicResponse
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.*
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static io.netty.handler.codec.http.HttpHeaders.Names.VIA
import static io.netty.handler.codec.http.HttpMethod.GET
import static io.netty.handler.codec.http.HttpResponseStatus.OK
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1

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

	void "requestPost adds headers to outgoing request"() {
		when:
		filters.requestPost(request)

		then:
		request.headers().get(VIA) == "Betamax"
	}

	void "responsePre throws an exception if the tape is not writable"() {
		given:
		def response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer("response body", Charset.forName("utf-8")))

		and:
		tape.isWritable() >> false

		when:
		filters.responsePre(response)

		then:
		thrown NonWritableTapeException
	}

	void "responsePre records the exchange to tape"() {
		given:
		def response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer("response body", Charset.forName("utf-8")))

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
				bodyAsBinary.bytes == readContent(response)
			}
		}

		and:
		response.headers().get(X_BETAMAX) == "REC"
	}

	private static byte[] readContent(FullHttpMessage message) {
		def stream = new ByteArrayOutputStream()
		message.content().getBytes(0, stream, message.content().readableBytes())
		stream.toByteArray()
	}
}
