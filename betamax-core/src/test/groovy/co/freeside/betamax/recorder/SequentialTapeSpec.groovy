package co.freeside.betamax.recorder

import co.freeside.betamax.*
import co.freeside.betamax.handler.*
import co.freeside.betamax.util.message.BasicRequest
import groovy.json.JsonSlurper
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.READ_SEQUENTIAL
import static org.apache.http.HttpHeaders.CONTENT_TYPE
import static org.apache.http.HttpStatus.*
import static org.apache.http.entity.ContentType.APPLICATION_JSON

@Issue('https://github.com/robfletcher/betamax/issues/7')
@Issue('https://github.com/robfletcher/betamax/pull/70')
class SequentialTapeSpec extends Specification {

	@Rule Recorder recorder = new Recorder()
	HttpHandler handler = new DefaultHandlerChain(recorder)

	@Betamax(tape = 'sequential tape', mode = READ_SEQUENTIAL)
	void 'read sequential tapes play back recordings in correct sequence'() {
		when: 'multiple requests are made to the same endpoint'
		def responses = []
		n.times {
			responses << handler.handle(request)
		}

		then: 'both successfully connect'
		responses.every {
			it.status == SC_OK
		}

		and: 'each has different content'
		responses.bodyAsText.text == (1..n).collect {
			"count: $it"
		}

		where:
		n = 2
		request = new BasicRequest('GET', 'http://freeside.co/betamax')
	}

	@Betamax(tape = 'sequential tape', mode = READ_SEQUENTIAL)
	void 'read sequential tapes return an error if more than the expected number of requests are made'() {
		given: 'all recorded requests have already been played'
		n.times {
			handler.handle(request)
		}

		when: 'multiple requests are made to the same endpoint'
		handler.handle(request)

		then: 'an exception is thrown'
		thrown NonWritableTapeException

		where:
		n = 2
		request = new BasicRequest('GET', 'http://freeside.co/betamax')
	}

	@Betamax(tape = 'rest conversation tape', mode = READ_SEQUENTIAL)
	void 'can read sequential responses from tapes with other content'() {
		given:
		def getRequest = new BasicRequest('GET', url)
		def postRequest = new BasicRequest('POST', url)
		postRequest.addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
		postRequest.body = '{"name":"foo"}'.bytes

		when: 'multiple requests are made to the same endpoint'
		def responses = []
		responses << handler.handle(getRequest)
		responses << handler.handle(postRequest)
		responses << handler.handle(getRequest)

		then: 'all successfully connect'
		responses.status == [SC_NOT_FOUND, SC_CREATED, SC_OK]

		and:
		new JsonSlurper().parseText(responses[2].bodyAsText.text).name == 'foo'

		where:
		url = 'http://freeside.co/thing/1'
	}

	@Betamax(tape = 'rest conversation tape', mode = READ_SEQUENTIAL)
	void 'out of sequence requests cause an error'() {
		given: 'one request is made'
		handler.handle(request)

		when: 'another request is made out of the expected sequence'
		handler.handle(request)

		then: 'all successfully connect'
		thrown NonWritableTapeException // todo: this is so not the right error here

		where:
		request = new BasicRequest('GET', 'http://freeside.co/thing/1')
	}

}
