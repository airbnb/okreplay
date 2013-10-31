/*
 * Copyright 2013 the original author or authors.
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

package co.freeside.betamax

import co.freeside.betamax.internal.RecorderListener
import co.freeside.betamax.tape.Tape
import spock.lang.*

class RecorderSpec extends Specification {

    def listener = Mock(RecorderListener)
    def configuration = Spy(Configuration, constructorArgs: [Configuration.builder()]) {
        registerListeners(_) >> { it[0] << listener }
    }
    @Subject def recorder = new Recorder(configuration)

    void "throws an exception if started when already running"() {
        given:
        recorder.start("a tape")

        when:
        recorder.start("another tape")

        then:
        thrown IllegalStateException
    }

    void "throws an exception if stopped when not started"() {
        when:
        recorder.stop()

        then:
        thrown IllegalStateException
    }

    void "calls listeners on start"() {
        when:
        recorder.start("a tape")

        then:
        1 * listener.onRecorderStart(_ as Tape)
    }

    void "calls listeners on stop"() {
        given:
        recorder.start("a tape")

        when:
        recorder.stop()

        then:
        1 * listener.onRecorderStop()
    }

}
