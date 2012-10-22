package co.freeside.betamax.handler

import co.freeside.betamax.Recorder
import co.freeside.betamax.handler.*
import co.freeside.betamax.message.*
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.util.message.*
import spock.lang.Specification

class TapeWriterSpec extends Specification {

	Recorder recorder = Mock(Recorder)
	TapeWriter handler = new TapeWriter(recorder)
	Request request = new BasicRequest()
	Response response = new BasicResponse()

	void 'writes chained response to tape before returning it'() {
		given:
		def nextHandler = Mock(HttpHandler)
		nextHandler.handle(_) >> response
		handler << nextHandler

		and:
		def tape = Mock(Tape)
		recorder.tape >> tape
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
		recorder.tape >> null

		when:
		handler.handle(request)

		then:
		thrown NoTapeException
	}

	void 'throws an exception if the tape is not writable'() {
		given:
		def tape = Mock(Tape)
		recorder.tape >> tape
		tape.isWritable() >> false

		when:
		handler.handle(request)

		then:
		thrown NonWritableTapeException
	}

}
