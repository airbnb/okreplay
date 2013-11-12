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

import spock.lang.*
import static co.freeside.betamax.TapeMode.*

@Issue("https://github.com/robfletcher/betamax/issues/106")
@Unroll
class RecorderTapeModeSpec extends Specification {

    void "tape mode is #expectedMode if default mode is #defaultMode and start is called with #modeParam"() {
        given:
        def configuration = Configuration.builder().defaultMode(defaultMode).build()
        def recorder = new Recorder(configuration)

        when:
        recorder.start("recorder tape mode spec", modeParam)

        then:
        recorder.tape.mode == expectedMode

        where:
        defaultMode | modeParam  | expectedMode
        READ_ONLY   | READ_WRITE | READ_WRITE
        READ_ONLY   | NULL       | READ_ONLY
    }

}
