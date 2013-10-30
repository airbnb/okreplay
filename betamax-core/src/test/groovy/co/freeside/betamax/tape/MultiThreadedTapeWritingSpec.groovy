/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.tape

import java.util.concurrent.CountDownLatch
import co.freeside.betamax.*
import co.freeside.betamax.handler.DefaultHandlerChain
import co.freeside.betamax.junit.Betamax
import co.freeside.betamax.junit.RecorderRule
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static java.util.concurrent.TimeUnit.SECONDS

class MultiThreadedTapeWritingSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') def tapeRoot = Files.createTempDir()
    def configuration = Configuration.builder().tapeRoot(tapeRoot).build()
	@Rule RecorderRule recorder = new RecorderRule(configuration)

	def handler = new DefaultHandlerChain(recorder)

	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer(HelloHandler)

	void setupSpec() {
		endpoint.start()
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
					responses[i] = handler.handle(request).bodyAsText.input.text
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
