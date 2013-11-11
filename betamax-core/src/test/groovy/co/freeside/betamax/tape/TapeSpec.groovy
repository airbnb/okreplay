/*
 * Copyright 2011 the original author or authors.
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

import co.freeside.betamax.message.*
import co.freeside.betamax.tape.yaml.YamlTapeLoader
import co.freeside.betamax.util.message.*
import com.google.common.io.Files
import spock.lang.*
import static co.freeside.betamax.TapeMode.*
import static com.google.common.net.HttpHeaders.*
import static com.google.common.net.MediaType.FORM_DATA

@Stepwise
class TapeSpec extends Specification {

    // TODO: only need this because there's no convenient way to construct a tape
    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def loader = new YamlTapeLoader(tapeRoot)
    @Shared Tape tape = loader.loadTape('tape_spec')

    Request getRequest = new BasicRequest('GET', 'http://icanhascheezburger.com/')
    Response plainTextResponse = new BasicResponse(status: 200, reason: 'OK', body: 'O HAI!'.bytes)

    void setup() {
        plainTextResponse.addHeader(CONTENT_TYPE, 'text/plain;charset=UTF-8')
        plainTextResponse.addHeader(CONTENT_LANGUAGE, 'en-GB')
        plainTextResponse.addHeader(CONTENT_ENCODING, 'gzip')
    }

    void cleanup() {
        tape.mode = READ_WRITE
    }

    void 'reading from an empty tape throws an exception'() {
        when: 'an empty tape is played'
        tape.play(getRequest)

        then: 'an exception is thrown'
        thrown IllegalStateException
    }

    void 'can write an HTTP interaction to a tape'() {
        when: 'an HTTP interaction is recorded to tape'
        tape.record(getRequest, plainTextResponse)

        then: 'the size of the tape increases'
        tape.size() == old(tape.size()) + 1
        def interaction = tape.interactions[-1]

        and: 'the request data is correctly stored'
        interaction.request.method == getRequest.method
        interaction.request.uri == getRequest.uri

        and: 'the response data is correctly stored'
        interaction.response.status == plainTextResponse.status
        interaction.response.body == 'O HAI!'
        interaction.response.headers[CONTENT_TYPE] == plainTextResponse.getHeader(CONTENT_TYPE)
        interaction.response.headers[CONTENT_LANGUAGE] == plainTextResponse.getHeader(CONTENT_LANGUAGE)
        interaction.response.headers[CONTENT_ENCODING] == plainTextResponse.getHeader(CONTENT_ENCODING)
    }

    void 'can overwrite a recorded interaction'() {
        when: 'a recording is made'
        tape.record(getRequest, plainTextResponse)

        then: 'the tape size does not increase'
        tape.size() == old(tape.size())

        and: 'the previous recording was overwritten'
        tape.interactions[-1].recorded > old(tape.interactions[-1].recorded)
    }

    void 'seek does not match a request for a different URI'() {
        given:
        def request = new BasicRequest('GET', 'http://qwantz.com/')

        expect:
        !tape.seek(request)
    }

    void 'can seek for a previously recorded interaction'() {
        expect:
        tape.seek(getRequest)
    }

    void 'can read a stored interaction'() {
        when: 'the tape is played'
        def response = tape.play(getRequest)

        then: 'the recorded response data is copied onto the response'
        response.status == plainTextResponse.status
        response.bodyAsText.input.text == 'O HAI!'
        response.headers == plainTextResponse.headers
    }

    void 'can record post requests with a body'() {
        given: 'a request with some content'
        def request = new BasicRequest('POST', 'http://github.com/')
        request.body = 'q=1'.getBytes('UTF-8')
        request.addHeader(CONTENT_TYPE, FORM_DATA.toString())

        when: 'the request and its response are recorded'
        tape.record(request, plainTextResponse)

        then: 'the request body is stored on the tape'
        def interaction = tape.interactions[-1]
        interaction.request.body == request.bodyAsText.input.text
    }

    void 'a write-only tape cannot be read from'() {
        given: 'the tape is put into write-only mode'
        tape.mode = WRITE_ONLY

        when: 'the tape is played'
        tape.play(getRequest)

        then: 'an exception is thrown'
        def e = thrown(IllegalStateException)
        e.message == 'the tape is not readable'
    }

    void 'a read-only tape cannot be written to'() {
        given: 'the tape is put into read-only mode'
        tape.mode = READ_ONLY

        when: 'the tape is recorded to'
        tape.record(getRequest, plainTextResponse)

        then: 'an exception is thrown'
        def e = thrown(IllegalStateException)
        e.message == 'the tape is not writable'
    }

}
