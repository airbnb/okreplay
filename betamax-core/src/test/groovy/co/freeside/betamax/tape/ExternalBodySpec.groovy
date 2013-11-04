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

package co.freeside.betamax.tape

import co.freeside.betamax.util.message.*
import spock.lang.*
import static co.freeside.betamax.TapeMode.READ_WRITE
import static co.freeside.betamax.tape.EntityStorage.external
import static com.google.common.net.HttpHeaders.CONTENT_TYPE

@Issue("https://github.com/robfletcher/betamax/issues/59")
class ExternalBodySpec extends Specification {

    @Subject MemoryTape tape = new MemoryTape(name: "external_body_spec", mode: READ_WRITE)

    def request = new BasicRequest("GET", "http://freeside.co/betamax")
    def plainTextResponse = new BasicResponse(status: 200, reason: "OK", body: "O HAI!".bytes)

    void "can write an HTTP interaction to a tape"() {
        given: "the tape is set to record response bodies externally"
        tape.responseBodyStorage = external

        when: "an HTTP interaction is recorded to tape"
        tape.record(request, plainTextResponse)

        then: "the basic request data is stored as normal"
        def interaction = tape.interactions[-1]
        interaction.response.status == plainTextResponse.status
        interaction.response.headers[CONTENT_TYPE] == plainTextResponse.getHeader(CONTENT_TYPE)

        and: "the response body is stored as a file reference"
        interaction.response.body instanceof File
        interaction.response.body.text == "O HAI!"
    }

}
