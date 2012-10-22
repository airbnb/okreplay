package co.freeside.betamax.recorder

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.IncrementingHandler
import groovyx.net.http.RESTClient
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_SEQUENTIAL
import static co.freeside.betamax.proxy.jetty.BetamaxProxy.X_BETAMAX
import static co.freeside.betamax.util.FileUtils.newTempDir
import static org.apache.http.HttpStatus.SC_OK

@Issue('https://github.com/robfletcher/betamax/issues/7')
@Issue('https://github.com/robfletcher/betamax/pull/70')
class SequentialTapeWritingSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	RESTClient http = new BetamaxRESTClient(endpoint.url)

	void setupSpec() {
		endpoint.start(IncrementingHandler)
	}

	@Betamax(tape = 'sequential tape', mode = WRITE_SEQUENTIAL)
	void 'write sequential tapes record multiple matching responses'() {
		when: 'multiple requests are made to the same endpoint'
		def responses = []
		n.times {
			responses << http.get(path: '/')
		}

		then: 'both successfully connect'
		responses.every {
			it.status == SC_OK
		}

		and: 'they both get recorded'
		responses.every {
			it.getFirstHeader(X_BETAMAX).value == 'REC'
		}

		and: 'each has different content'
		responses.data.text == (1..n).collect {
			"count: $it"
		}

		and: 'multiple recordings are added to the tape'
		recorder.tape.size() == old(recorder.tape.size()) + n

		where:
		n = 2
	}

}
