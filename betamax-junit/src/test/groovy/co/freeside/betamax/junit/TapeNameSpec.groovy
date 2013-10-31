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

package co.freeside.betamax.junit

import co.freeside.betamax.Configuration
import com.google.common.io.Files
import org.junit.Rule
import spock.lang.*

@Issue("https://github.com/robfletcher/betamax/issues/36")
class TapeNameSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    def configuration = Configuration.builder().tapeRoot(tapeRoot).build()
    @Rule public RecorderRule recorder = new RecorderRule(configuration)

    @Betamax(tape = "explicit name")
    void "tape can be named explicitly"() {
        expect:
        recorder.tape.name == "explicit name"
    }

    @Betamax
    void "tape name defaults to test name"() {
        expect:
        recorder.tape.name == "tape name defaults to test name"
    }

}
