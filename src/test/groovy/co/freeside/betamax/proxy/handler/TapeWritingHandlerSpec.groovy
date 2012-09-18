package co.freeside.betamax.proxy.handler

import co.freeside.betamax.Recorder
import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.message.BasicResponse
import spock.lang.Specification

import static java.net.HttpURLConnection.HTTP_FORBIDDEN

class TapeWritingHandlerSpec extends Specification {

	Recorder recorder = Mock(Recorder)
	TapeWritingHandler handler = new TapeWritingHandler(recorder)
	Request request = new BasicRequest()
	Response response = new BasicResponse()

	void 'writes chained response to tape before returning it'() {
		given:
		def nextHandler = Mock(HttpHandler)
		nextHandler.handle(_) >> response
		handler << nextHandler

		and:
		def tape = Mock(Tape)
		recorder.getTape() >> tape
		tape.isWritable() >> true

		when:
		def result = handler.handle(request)

		then:
		result.is(response)

		and:
		1 * tape.record(request, response)
	}

	void 'throws an exception if there is no tape inserted'() {
		given:
		recorder.getTape() >> null

		when:
		handler.handle(request)

		then:
		def e = thrown(ProxyException)
		e.httpStatus == HTTP_FORBIDDEN
	}

	void 'throws an exception if the tape is not writable'() {
		given:
		def tape = Mock(Tape)
		recorder.getTape() >> tape
		tape.isWritable() >> false

		when:
		handler.handle(request)

		then:
		def e = thrown(ProxyException)
		e.httpStatus == HTTP_FORBIDDEN
	}

}
