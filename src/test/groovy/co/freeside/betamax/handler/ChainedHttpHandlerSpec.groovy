package co.freeside.betamax.handler

import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response
import spock.lang.Specification

class ChainedHttpHandlerSpec extends Specification {

	ChainedHttpHandler handler = new ChainedHttpHandler() {
		@Override
		Response handle(Request request) {
			throw new UnsupportedOperationException()
		}
	}

	Request request = [:] as Request
	Response response = [:] as Response

	void 'throws an exception if chain is called on the last handler in the chain'() {
		when:
		handler.chain(request)

		then:
		thrown IllegalStateException
	}

	void 'chain passes to the next handler if there is one'() {
		given:
		def nextHandler = Mock(HttpHandler)
		handler << nextHandler

		when:
		def result =  handler.chain(request)

		then:
		1 * nextHandler.handle(request) >> response

		and:
		result.is(response)
	}
}
