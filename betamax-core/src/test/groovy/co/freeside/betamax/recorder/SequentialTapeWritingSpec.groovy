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

import co.freeside.betamax.tape.MemoryTape
import co.freeside.betamax.tape.yaml.YamlTapeLoader
import co.freeside.betamax.util.message.*
import com.google.common.io.Files
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_SEQUENTIAL
import static java.net.HttpURLConnection.HTTP_OK

@Issue("https://github.com/robfletcher/betamax/issues/7")
@Issue("https://github.com/robfletcher/betamax/pull/70")
class SequentialTapeWritingSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def tapeLoader = new YamlTapeLoader(tapeRoot)
    MemoryTape tape

    void setup() {
        tape = tapeLoader.loadTape("sequential tape")
        tape.mode = WRITE_SEQUENTIAL
    }

    void "write sequential tapes record multiple matching responses"() {
        when: "multiple responses are captured from the same endpoint"
        (1..n).each {
            def response = new BasicResponse(HTTP_OK, "OK")
            response.body = "count: $it".bytes
            tape.record(request, response)
        }

        then: "multiple recordings are added to the tape"
        tape.size() == old(tape.size()) + n

        and: "each has different content"
        with(tape.interactions) {
            response.bodyAsText.input.text == (1..n).collect {
                "count: $it"
            }
        }

        where:
        n = 2
        request = new BasicRequest("GET", "http://freeside.co/betamax")
    }

}
