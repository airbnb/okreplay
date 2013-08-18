package co.freeside.betamax.tape

import java.util.concurrent.CountDownLatch
import co.freeside.betamax.*
import co.freeside.betamax.handler.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.server.HelloHandler
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static java.util.concurrent.TimeUnit.SECONDS

class MultiThreadedTapeWritingSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	HttpHandler handler = new DefaultHandlerChain(recorder)

	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	void setupSpec() {
		endpoint.start(HelloHandler)
	}

	@Betamax(tape = 'multi_threaded_tape_writing_spec', mode = TapeMode.READ_WRITE)
	void 'the tape can cope with concurrent reading and writing'() {
		when: 'requests are fired concurrently'
		def finished = new CountDownLatch(threads)
		def responses = [:]
		threads.times { i ->
			Thread.start {
				try {
					def request = new BasicRequest('GET', "$endpoint.url$i")
					responses[i] = handler.handle(request).bodyAsText.text
				} catch (IOException e) {
					responses[i] = 'FAIL!'
				}
				finished.countDown()
			}
		}

		then: 'all threads complete'
		finished.await(1, SECONDS)

		and: 'the correct response is returned to each request'
		responses.every {
			it.value == HELLO_WORLD
		}

		where:
		threads = 10
	}
}
