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
import co.freeside.betamax.tape.Tape
import co.freeside.betamax.util.message.*
import spock.lang.Specification

class TapeReaderSpec extends Specification {

    def recorder = Stub(Recorder)
    def handler = new TapeReader(recorder)
    def nextHandler = Mock(HttpHandler)
    def request = new BasicRequest()
    def response = new BasicResponse()

    void setup() {
        handler.add(nextHandler)
    }

    void "chains if there is no matching tape entry"() {
        given:
        def tape = Stub(Tape) {
            seek(request) >> false
            isWritable() >> true
        }
        recorder.tape >> tape

        when:
        handler.handle(request)

        then:
        1 * nextHandler.handle(request)
    }

    void "chains if there is a matching tape entry if the tape is not readable"() {
        given:
        def tape = Mock(Tape) {
            isReadable() >> false
            isWritable() >> true
        }
        recorder.tape >> tape

        when:
        handler.handle(request)

        then:
        0 * tape.play(_)
        1 * nextHandler.handle(request)
    }

    void "succeeds if there is a matching tape entry"() {
        given:
        def tape = Mock(Tape) {
            isReadable() >> true
            seek(request) >> true
        }
        recorder.tape >> tape

        when:
        def result = handler.handle(request)

        then:
        result.is(response)

        and:
        1 * tape.play(request) >> response

        and:
        0 * nextHandler._
    }

    void "throws an exception if there is no tape"() {
        given:
        recorder.tape >> null

        when:
        handler.handle(request)

        then:
        thrown NoTapeException

        and:
        0 * nextHandler._
    }

    void "throws an exception if there is no matching entry and the tape is not writable"() {
        given:
        def tape = Stub(Tape) {
            isReadable() >> true
            isWritable() >> false
            seek(request) >> false
        }
        recorder.tape >> tape

        when:
        handler.handle(request)

        then:
        thrown NonWritableTapeException

        and:
        0 * nextHandler._
    }

}
