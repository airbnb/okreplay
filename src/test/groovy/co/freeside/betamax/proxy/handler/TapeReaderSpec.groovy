package co.freeside.betamax.proxy.handler

import co.freeside.betamax.Recorder
import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.message.BasicResponse
import spock.lang.Specification

import static java.net.HttpURLConnection.HTTP_FORBIDDEN

class TapeReaderSpec extends Specification {

	Recorder recorder = Mock(Recorder)
	TapeReader handler = new TapeReader(recorder)
	HttpHandler nextHandler = Mock(HttpHandler)
	Request request = new BasicRequest()
	Response response = new BasicResponse()

	void setup() {
		handler << nextHandler
	}

	void 'chains if there is no matching tape entry'() {
		given:
		def tape = Mock(Tape)
		tape.seek(request) >> false

		and:
		recorder.tape >> tape

		when:
		handler.handle(request)

		then:
		1 * nextHandler.handle(request)
	}

	void 'chains if there is a matching tape entry if the tape is not readable'() {
		given:
		def tape = Mock(Tape)
		tape.isReadable() >> false
		recorder.tape >> tape

		when:
		handler.handle(request)

		then:
		0 * tape.play(_)
		1 * nextHandler.handle(request)
	}

	void 'succeeds if there is a matching tape entry'() {
		given:
		def tape = Mock(Tape)
		tape.isReadable() >> true
		tape.seek(request) >> true
		recorder.tape >> tape

		when:
		def result = handler.handle(request)

		then:
		result.is(response)

		and:
		1 * tape.play(request) >> response

		and:
		0 * nextHandler._
	}

	void 'throws an exception if there is no tape'() {
		given:
		recorder.tape >> null

		when:
		handler.handle(request)

		then:
		def e = thrown(ProxyException)
		e.httpStatus == HTTP_FORBIDDEN

		and:
		0 * nextHandler._
	}

}
