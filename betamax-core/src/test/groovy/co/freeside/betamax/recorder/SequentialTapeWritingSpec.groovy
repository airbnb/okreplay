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

package co.freeside.betamax.recorder

import co.freeside.betamax.*
import co.freeside.betamax.handler.*
import co.freeside.betamax.junit.Betamax
import co.freeside.betamax.junit.RecorderRule
import co.freeside.betamax.message.Response
import co.freeside.betamax.util.server.SimpleServer
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.server.IncrementingHandler
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.TapeMode.WRITE_SEQUENTIAL
import static co.freeside.betamax.util.FileUtils.newTempDir
import static org.apache.http.HttpStatus.SC_OK

@Issue('https://github.com/robfletcher/betamax/issues/7')
@Issue('https://github.com/robfletcher/betamax/pull/70')
class SequentialTapeWritingSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') def tapeRoot = newTempDir('tapes')
	def recorder = new Recorder(tapeRoot: tapeRoot)
    @Rule RecorderRule recorderRule = new RecorderRule(recorder)
    @Shared @AutoCleanup('stop') def endpoint = new SimpleServer(IncrementingHandler)
	def handler = new DefaultHandlerChain(recorder)

	void setupSpec() {
		endpoint.start()
	}

	@Betamax(tape = 'sequential tape', mode = WRITE_SEQUENTIAL)
	void 'write sequential tapes record multiple matching responses'() {
		when: 'multiple requests are made to the same endpoint'
        List<Response> responses = []
		n.times {
			responses << handler.handle(request)
		}

		then: 'both successfully connect'
		responses.every {
			it.status == SC_OK
		}

		and: 'they both get recorded'
		responses.every {
			it.headers[X_BETAMAX] == 'REC'
		}

		and: 'each has different content'
		responses.bodyAsText.input.text == (1..n).collect {
			"count: $it"
		}

		and: 'multiple recordings are added to the tape'
		recorder.tape.size() == old(recorder.tape.size()) + n

		where:
		n = 2
		request = new BasicRequest('GET', endpoint.url)
	}

}
