package co.freeside.betamax.tape

import co.freeside.betamax.message.Request
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.message.BasicResponse
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

import static java.util.concurrent.TimeUnit.SECONDS

@Issue('https://github.com/robfletcher/betamax/issues/57')
class MultiThreadedTapeAccessSpec extends Specification {

	@Shared Tape tape = new MemoryTape(name: 'multi_threaded_tape_access_spec')

	void 'the correct response is replayed to each thread'() {
		given: 'a number of requests'
		List<Request> requests = (0..<threads).collect { i ->
			def request = new BasicRequest('GET', "http://example.com/$i")
			request.addHeader('X-Thread', i.toString())
			request
		}

		and: 'some existing responses on tape'
		requests.eachWithIndex { request, i ->
			def response = new BasicResponse(status: 200, reason: 'OK', body: i.toString())
			tape.record(request, response)
		}

		when: 'requests are replayed concurrently'
		def finished = new CountDownLatch(threads)
		def responses = [:]
		requests.eachWithIndex { request, i ->
			Thread.start {
				def response = tape.play(request)
				responses[requests[i].getHeader('X-Thread')] = response.bodyAsText.text
				finished.countDown()
			}
		}

		then: 'all threads complete'
		finished.await(1, SECONDS)

		and: 'the correct response is returned to each request'
		responses.every { key, value ->
			key == value
		}

		where:
		threads = 10
	}

}
