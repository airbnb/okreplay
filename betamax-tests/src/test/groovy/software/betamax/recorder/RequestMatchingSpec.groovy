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

package software.betamax.recorder

import com.google.common.io.Files
import software.betamax.ComposedMatchRule
import software.betamax.MatchRules
import software.betamax.message.tape.RecordedRequest
import software.betamax.tape.yaml.YamlTapeLoader
import spock.lang.*

import static com.google.common.net.HttpHeaders.ACCEPT
import static com.google.common.net.HttpHeaders.CONTENT_TYPE
import static com.google.common.net.MediaType.JSON_UTF_8
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8

@Issue("https://github.com/robfletcher/betamax/issues/9")
class RequestMatchingSpec extends Specification {

  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def loader = new YamlTapeLoader(tapeRoot)

  @Unroll('#method request for #uri returns "#responseText"')
  void "default match is method and uri"() {
    given:
    def yaml = """\
!tape
name: method and url tape
interactions:
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    url: http://xkcd.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: GET method response from xkcd.com
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: POST
    url: http://xkcd.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: POST method response from xkcd.com
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    url: http://qwantz.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: GET method response from qwantz.com
"""

    and:
    def tape = loader.readFrom(new StringReader(yaml))

    when:
    def response = tape.play(request)

    then:
    response.body().string() == responseText

    where:
    method | uri
    "GET"  | "http://xkcd.com/"
    "POST" | "http://xkcd.com/"
    "GET"  | "http://qwantz.com/"

    responseText = "$method method response from ${uri.toURI().host}"
    request = new RecordedRequest.Builder()
        .url(uri)
        .method(method, null)
        .build()
  }

  void "can match based on host"() {
    given:
    def yaml = """\
!tape
name: host match tape
interactions:
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    url: http://xkcd.com/936/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: GET method response from xkcd.com
"""

    and:
    def tape = loader.readFrom(new StringReader(yaml))
    tape.matchRule = MatchRules.host

    when:
    def request = new RecordedRequest.Builder()
        .url("http://xkcd.com/875")
        .build()
    def response = tape.play(request)

    then:
    response.body().string() == "GET method response from xkcd.com"
  }

  @Unroll('request with Accept: #acceptHeader returns "#responseText"')
  void "can match based on headers"() {
    given:
    def yaml = """\
!tape
name: headers match tape
interactions:
- recorded: 2013-10-01T13:27:37.000Z
  request:
    method: GET
    url: http://httpbin.org/get
    headers:
      Accept: application/json
  response:
    status: 200
    headers:
      Content-Type: application/json
    body: |-
      { "message": "JSON data" }
- recorded: 2013-10-01T13:34:33.000Z
  request:
    method: GET
    url: http://httpbin.org/get
    headers:
      Accept: text/plain
  response:
    status: 200
    headers:
      Content-Type: text/plain
    body: Plain text data
"""

    and:
    def tape = loader.readFrom(new StringReader(yaml))
    tape.matchRule = ComposedMatchRule.of(MatchRules.method, MatchRules.uri, MatchRules.accept)

    when:
    def request = new RecordedRequest.Builder()
        .addHeader(ACCEPT, acceptHeader)
        .url("http://httpbin.org/get")
        .build()
    def response = tape.play(request)

    then:
    response.header(CONTENT_TYPE) == acceptHeader
    response.body().string() == responseText

    where:
    acceptHeader                                    | responseText
    JSON_UTF_8.withoutParameters().toString()       | '{ "message": "JSON data" }'
    PLAIN_TEXT_UTF_8.withoutParameters().toString() | "Plain text data"
  }
}
