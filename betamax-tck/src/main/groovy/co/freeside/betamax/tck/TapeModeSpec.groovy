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

package co.freeside.betamax.tck

import co.freeside.betamax.*
import co.freeside.betamax.handler.NonWritableTapeException
import co.freeside.betamax.message.Response
import co.freeside.betamax.tape.MemoryTape
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import spock.lang.*
import static co.freeside.betamax.TapeMode.*
import static java.net.HttpURLConnection.HTTP_OK

@Unroll
abstract class TapeModeSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared Recorder recorder = new Recorder(configuration)

    @Shared @AutoCleanup("stop") def endpoint = new SimpleServer(HelloHandler)

    protected abstract Configuration getConfiguration()

    protected abstract void makeRequest()

    void setupSpec() {
        endpoint.start()
    }

    void cleanup() {
        recorder.stop()
    }

    void "in #mode mode the proxy rejects a request if no recorded interaction exists"() {
        given: "a read-only tape is inserted"
        recorder.start("read only tape", mode)

        when: "a request is made that does not match anything recorded on the tape"
        makeRequest()

        then: "the proxy rejects the request"
        thrown NonWritableTapeException

        where:
        mode << [READ_ONLY, READ_SEQUENTIAL]
    }

    void "in #mode mode a new interaction is recorded"() {
        given: "an empty write-only tape is inserted"
        new File(tapeRoot, "blank_tape_${mode}.yaml").delete()
        recorder.start("blank tape $mode", mode)
        def tape = recorder.tape

        when: "a request is made"
        makeRequest()

        then: "the interaction is recorded"
        tape.size() == old(tape.size()) + 1

        where:
        mode << [READ_WRITE, WRITE_ONLY, WRITE_SEQUENTIAL]
    }

    void "in write-only mode the proxy overwrites an existing matching interaction"() {
        given: "an existing tape file is inserted in write-only mode"
        def tapeFile = new File(tapeRoot, "write_only_tape.yaml")
        tapeFile.text = """\
!tape
name: write only tape
interactions:
- recorded: 2011-08-26T21:46:52.000Z
  request:
    method: GET
    uri: $endpoint.url
    headers: {}
  response:
    status: 202
    headers: {}
    body: Previous response made when endpoint was down.
"""
        recorder.start("write only tape", WRITE_ONLY)
        def tape = recorder.tape as MemoryTape

        when: "a request is made that matches a request already recorded on the tape"
        makeRequest()

        then: "the previously recorded request is overwritten"
        tape.size() == old(tape.size())
        tape.interactions[-1].response.status == HTTP_OK
        tape.interactions[-1].response.body
    }

    @Issue("https://github.com/robfletcher/betamax/issues/7")
    @Issue("https://github.com/robfletcher/betamax/pull/70")
    void "in write-sequential mode the proxy records additional interactions"() {
        given: "an existing tape file is inserted in write-sequential mode"
        def tapeFile = new File(tapeRoot, "write_sequential_tape.yaml")
        tapeFile.text = """\
!tape
name: write sequential tape
interactions:
- recorded: 2011-08-26T21:46:52.000Z
  request:
    method: GET
    uri: $endpoint.url
    headers: {}
  response:
    status: 202
    headers: {}
    body: Previous response made when endpoint was down.
"""
        recorder.start("write sequential tape", WRITE_SEQUENTIAL)
        def tape = recorder.tape as MemoryTape

        when: "a request is made that matches a request already recorded on the tape"
        makeRequest()

        then: "the previously recorded request is overwritten"
        tape.size() == old(tape.size()) + 1
        tape.interactions[-1].response.status == HTTP_OK
        tape.interactions[-1].response.body
    }

}
