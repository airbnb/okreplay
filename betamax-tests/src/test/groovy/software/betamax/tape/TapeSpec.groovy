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

package software.betamax.tape

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import software.betamax.encoding.GzipEncoder
import software.betamax.tape.yaml.YamlTapeLoader
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static com.google.common.net.HttpHeaders.*
import static com.google.common.net.MediaType.FORM_DATA
import static software.betamax.TapeMode.*

@Stepwise
class TapeSpec extends Specification {

  // TODO: only need this because there's no convenient way to construct a tape
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def loader = new YamlTapeLoader(tapeRoot)
  @Shared Tape tape = loader.loadTape('tape_spec')

  Request getRequest = new Request.Builder()
      .url('http://icanhascheezburger.com/')
      .build()
  Response plainTextResponse = new Response.Builder()
      .code(200)
      .addHeader(CONTENT_LANGUAGE, 'en-GB')
      .addHeader(CONTENT_ENCODING, 'gzip')
      .body(ResponseBody.create(MediaType.parse('text/plain;charset=UTF-8'),
      new GzipEncoder().encode('O HAI!')))
      .build()

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
    interaction.request.method == getRequest.method()
    interaction.request.uri == getRequest.uri

    and: 'the response data is correctly stored'
    interaction.response.status == plainTextResponse.status
    interaction.response.body == 'O HAI!'
    interaction.response.headers[CONTENT_TYPE] == plainTextResponse.header(CONTENT_TYPE)
    interaction.response.headers[CONTENT_LANGUAGE] == plainTextResponse.header(CONTENT_LANGUAGE)
    interaction.response.headers[CONTENT_ENCODING] == plainTextResponse.header(CONTENT_ENCODING)
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
    def request = new Request.Builder().url('http://qwantz.com/').build()

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
    response.code() == plainTextResponse.code()
    response.body().string() == 'O HAI!'
    response.headers() == plainTextResponse.headers()
  }

  void 'can record post requests with a body'() {
    given: 'a request with some content'
    def request = new Request.Builder()
        .url('http://github.com/')
        .method("POST", RequestBody.create(MediaType.parse(FORM_DATA.toString()), 'q=1'))
        .build()

    when: 'the request and its response are recorded'
    tape.record(request, plainTextResponse)

    then: 'the request body is stored on the tape'
    def interaction = tape.interactions[-1]
    interaction.request.bodyAsText == request.bodyAsText
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
