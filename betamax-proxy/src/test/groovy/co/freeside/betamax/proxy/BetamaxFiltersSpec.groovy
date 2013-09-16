package co.freeside.betamax.proxy

import co.freeside.betamax.tape.Tape
import co.freeside.betamax.util.message.BasicResponse
import io.netty.handler.codec.http.*
import spock.lang.*
import static io.netty.handler.codec.http.HttpMethod.GET
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1

class BetamaxFiltersSpec extends Specification {

	@Subject
	BetamaxFilters filters
	HttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "http://freeside.co/betamax")
	Tape tape = Stub(Tape)

	void setup() {
		filters = new BetamaxFilters(request, tape)
	}

	void "requestPre returns null if no match is found on tape"() {
		expect:
		filters.requestPre(request) is null
	}

	void "requestPre returns a recorded response if found on tape"() {
		given:
		def recordedResponse = new BasicResponse(200, "OK")
		recordedResponse.body = "message body".bytes

		and:
		tape.seek(_) >> true
		tape.play(_) >> recordedResponse

		expect:
		FullHttpResponse response = filters.requestPre(request)
		response != null
		response.status.code() == 200
		response.status.reasonPhrase() == "OK"
		readContent(response) == recordedResponse.body
	}

	private static byte[] readContent(FullHttpMessage message) {
		def stream = new ByteArrayOutputStream()
		message.content().getBytes(0, stream, message.content().readableBytes())
		stream.toByteArray()
	}
}
