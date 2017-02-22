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
import okhttp3.Response
import okhttp3.ResponseBody
import software.betamax.encoding.DeflateEncoder
import software.betamax.encoding.GzipEncoder
import software.betamax.tape.yaml.YamlTapeLoader
import spock.lang.*

import static com.google.common.net.HttpHeaders.*
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8
import static java.net.HttpURLConnection.HTTP_OK
import static software.betamax.TapeMode.READ_WRITE

@Issue("https://github.com/robfletcher/betamax/issues/3")
@Unroll
class ContentEncodingSpec extends Specification {

  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def loader = new YamlTapeLoader(tapeRoot)

  /**
   * This is really just testing that the tape doesn"t do anything silly
   * in the presence of a Content-Encoding header. It is the responsibility
   * of the BetamaxResponse implementation to decode the downstream content.
   */
  void "a #encoding encoded response body is stored as plain text in a tape file"() {
    given:
    def request = new Request.Builder()
        .addHeader(ACCEPT_ENCODING, encoding)
        .url("http://freeside.co/betamax")
        .build()

    def responseBuilder = new Response.Builder()
        .code(HTTP_OK)
        .addHeader(CONTENT_TYPE, PLAIN_TEXT_UTF_8.withoutParameters().toString())
        .addHeader(CONTENT_ENCODING, encoding)
    byte[] body
    if (encoding == "gzip") {
      body = new GzipEncoder().encode("O HAI!")
    } else if (encoding == "deflate") {
      body = new DeflateEncoder().encode("O HAI!")
    } else {
      body = "O HAI!".bytes
    }
    def response = responseBuilder
        .body(ResponseBody.create(MediaType.parse("text/plain"), body))
        .build()

    and:
    def tape = loader.newTape("encoded response tape")
    tape.mode = READ_WRITE
    tape.record(request, response)

    when:
    def writer = new StringWriter()
    loader.writeTo(tape, writer)

    then:
    def yaml = writer.toString()
    yaml.contains("body: O HAI!")

    where:
    encoding << ["gzip", "deflate", "none"]
  }

  void "response body is not encoded when played from tape and a #encoding content-encoding header is present"() {
    given:
    def yaml = """\
!tape
name: encoded response tape
interactions:
- recorded: 2011-08-24T20:38:40.000Z
  request:
    method: GET
    uri: http://freeside.co/betamax
    headers: {Accept-Encoding: $encoding}
  response:
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB, Content-Encoding: $encoding}
    body: O HAI!
"""
    def tape = loader.readFrom(new StringReader(yaml))

    and:
    def request = new Request.Builder().url("http://freeside.co/betamax").build()

    when:
    def response = tape.play(request)

    then:
    response.header(CONTENT_ENCODING) == encoding
    response.body().string() == "O HAI!"

    where:
    encoding << ["gzip", "deflate"]
  }

}
