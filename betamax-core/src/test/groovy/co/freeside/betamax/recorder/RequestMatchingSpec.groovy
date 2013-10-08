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

package co.freeside.betamax.recorder

import co.freeside.betamax.MatchRule
import co.freeside.betamax.Recorder
import co.freeside.betamax.handler.*
import co.freeside.betamax.util.message.BasicRequest
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.util.FileUtils.newTempDir
import static org.apache.http.HttpHeaders.*

@Issue('https://github.com/robfletcher/betamax/issues/9')
class RequestMatchingSpec extends Specification {

    @Shared
    @AutoCleanup('deleteDir')
    File tapeRoot = newTempDir('tapes')
    @AutoCleanup('stop')
    Recorder recorder = new Recorder(tapeRoot: tapeRoot)
    HttpHandler handler = new DefaultHandlerChain(recorder)

    void setupSpec() {
        tapeRoot.mkdirs()
    }

    @Unroll('#method request for #uri returns "#responseText"')
    void 'default match is method and uri'() {
        given:
        new File(tapeRoot, 'method_and_uri_tape.yaml').text = '''\
!tape
name: method and uri tape
interactions:
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    uri: http://xkcd.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: GET method response from xkcd.com
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: POST
    uri: http://xkcd.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: POST method response from xkcd.com
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    uri: http://qwantz.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: GET method response from qwantz.com
'''

        and:
        recorder.start('method and uri tape')

        when:
        def response = handler.handle(request)

        then:
        response.bodyAsText.input.text == responseText

        where:
        method | uri
        'GET'  | 'http://xkcd.com/'
        'POST' | 'http://xkcd.com/'
        'GET'  | 'http://qwantz.com/'

        responseText = "$method method response from ${uri.toURI().host}"
        request = new BasicRequest(method, uri)
    }

    void 'can match based on host'() {
        given:
        new File(tapeRoot, 'host_match_tape.yaml').text = '''\
!tape
name: host match tape
interactions:
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    uri: http://xkcd.com/936/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: GET method response from xkcd.com
'''

        and:
        recorder.start('host match tape', [match: [MatchRule.host]])

        when:
        def request = new BasicRequest('GET', 'http://xkcd.com/875')
        def response = handler.handle(request)

        then:
        response.headers[VIA] == 'Betamax'
        response.headers[X_BETAMAX] == 'PLAY'
        response.bodyAsText.input.text == 'GET method response from xkcd.com'
    }

    @Unroll('request with Accept: #acceptHeader returns "#responseText"')
    void 'can match based on headers'() {
        given:
        new File(tapeRoot, 'headers_match_tape.yaml').text = '''\
!tape
name: headers match tape
interactions:
- recorded: 2013-10-01T13:27:37.000Z
  request:
    method: GET
    uri: http://httpbin.org/get
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
    uri: http://httpbin.org/get
    headers:
      Accept: text/plain
  response:
    status: 200
    headers:
      Content-Type: text/plain
    body: Plain text data
'''

        and:
        recorder.start('headers match tape', [match: [MatchRule.method, MatchRule.uri, MatchRule.headers]])

        when:
        def request = new BasicRequest('GET', 'http://httpbin.org/get')
        request.addHeader(ACCEPT, acceptHeader)
        def response = handler.handle(request)

        then:
        response.headers[VIA] == 'Betamax'
        response.headers[X_BETAMAX] == 'PLAY'
        response.headers[CONTENT_TYPE] == acceptHeader
        response.bodyAsText.input.text == responseText

        where:
        acceptHeader       | responseText
        "application/json" | '{ "message": "JSON data" }'
        "text/plain"       | "Plain text data"
    }
}
