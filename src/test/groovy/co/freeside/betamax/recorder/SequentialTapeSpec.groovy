package co.freeside.betamax.recorder

import co.freeside.betamax.*
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import groovyx.net.http.RESTClient
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.*
import static org.apache.http.HttpStatus.*

@Issue('https://github.com/robfletcher/betamax/issues/7')
@Issue('https://github.com/robfletcher/betamax/pull/70')
class SequentialTapeSpec extends Specification {

	@Rule Recorder recorder = new Recorder()
	RESTClient http = new BetamaxRESTClient('http://freeside.co/')

	void setup() {
		http.handler.failure = { response -> response }
	}

	@Betamax(tape = 'sequential tape', mode = READ_SEQUENTIAL)
	void 'read sequential tapes play back recordings in correct sequence'() {
		when: 'multiple requests are made to the same endpoint'
		def responses = []
		n.times {
			responses << http.get(path: '/betamax')
		}

		then: 'both successfully connect'
		responses.every {
			it.status == SC_OK
		}

		and: 'each has different content'
		responses.data.text == (1..n).collect {
			"count: $it"
		}

		where:
		n = 2
	}

	@Betamax(tape = 'sequential tape', mode = READ_SEQUENTIAL)
	void 'read sequential tapes return an error if more than the expected number of requests are made'() {
		when: 'multiple requests are made to the same endpoint'
		def responses = []
		(n + 1).times {
			responses << http.get(path: '/betamax')
		}

		then: 'the first requests successfully connect'
		responses[0..<n].every {
			it.status == SC_OK
		}

		and: 'the last request fails'
		responses[-1].status == SC_FORBIDDEN

		where:
		n = 2
	}

	@Betamax(tape = 'rest conversation tape', mode = READ_SEQUENTIAL)
	void 'can read sequential responses from tapes with other content'() {
		when: 'multiple requests are made to the same endpoint'
		def responses = []
		responses << http.get(path: '/thing/1')
		responses << http.post(path: '/thing/1', contentType: 'application/json', body: '{"name":"foo"}')
		responses << http.get(path: '/thing/1')

		then: 'all successfully connect'
		responses.status == [SC_NOT_FOUND, SC_CREATED, SC_OK]

		and:
		responses[2].data.name == 'foo'
	}

	@Betamax(tape = 'rest conversation tape', mode = READ_SEQUENTIAL)
	void 'out of sequence requests cause an error'() {
		when: 'multiple requests are made to the same endpoint'
		def responses = []
		2.times {
			responses << http.get(path: '/thing/1')
		}

		then: 'all successfully connect'
		responses.status == [SC_NOT_FOUND, SC_FORBIDDEN]
	}

}
