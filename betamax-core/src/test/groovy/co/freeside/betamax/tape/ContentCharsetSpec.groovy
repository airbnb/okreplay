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

import co.freeside.betamax.tape.yaml.YamlTapeLoader
import co.freeside.betamax.util.message.*
import com.google.common.io.Files
import spock.lang.*
import static co.freeside.betamax.TapeMode.READ_WRITE
import static com.google.common.base.Charsets.*
import static com.google.common.net.HttpHeaders.*
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8
import static java.net.HttpURLConnection.HTTP_OK

@Issue("https://github.com/robfletcher/betamax/issues/21")
@Unroll
class ContentCharsetSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def loader = new YamlTapeLoader(tapeRoot)

    void "a response with a #charset body is recorded correctly"() {
        given:
        def request = new BasicRequest()

        def response = new BasicResponse(HTTP_OK, "OK")
        response.addHeader(CONTENT_TYPE, PLAIN_TEXT_UTF_8.withCharset(charset).toString())
        response.addHeader(CONTENT_ENCODING, "none")
        response.body = "\u00a3".getBytes(charset)

        and:
        def tape = loader.newTape("charsets")
        tape.mode = READ_WRITE
        tape.record(request, response)

        when:
        def writer = new StringWriter()
        loader.writeTo(tape, writer)

        then:
        def yaml = writer.toString()
        yaml.contains("body: \u00a3")

        where:
        charset << [UTF_8, ISO_8859_1]
    }

    void "a response with a #charset body is played back correctly"() {
        given:
        def yaml = """\
!tape
name: charsets
interactions:
- recorded: 2011-08-27T23:25:45.000Z
  request:
    method: GET
    uri: http://freeside.co/betamax
  response:
    status: 200
    headers:
      Content-Type: text/plain;charset=$charset
      Content-Encoding: none
    body: \u00a3
"""
        def tape = loader.readFrom(new StringReader(yaml))

        and:
        def request = new BasicRequest("GET", "http://freeside.co/betamax")

        when:
        def response = tape.play(request)

        then:
        def expected = "\u00a3".getBytes(charset)
        response.bodyAsBinary.input.bytes == expected

        where:
        charset << [UTF_8, ISO_8859_1]
    }

}
