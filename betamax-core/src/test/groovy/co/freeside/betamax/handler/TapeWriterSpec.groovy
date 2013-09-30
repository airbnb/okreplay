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

package co.freeside.betamax.handler

import co.freeside.betamax.Recorder
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
