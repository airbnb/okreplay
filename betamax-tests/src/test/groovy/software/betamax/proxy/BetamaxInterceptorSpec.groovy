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

package software.betamax.proxy

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import software.betamax.message.tape.RecordedRequest
import software.betamax.message.tape.RecordedResponse
import software.betamax.tape.Tape
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static software.betamax.Headers.X_BETAMAX

@Unroll
class BetamaxInterceptorSpec extends Specification {

  @Subject
  BetamaxInterceptor filters = new BetamaxInterceptor()
  Request request = new Request.Builder().url("http://freeside.co/betamax").build()
  Tape tape = Mock(Tape)

  void "clientToProxyRequest returns null if no match is found on tape"() {
    given:
    tape.isReadable() >> true
    tape.seek(_) >> false

    expect:
    filters.clientToProxyRequest(request) == null
  }

  void "clientToProxyRequest returns null if tape is not readable"() {
    given:
    tape.isReadable() >> false

    when:
    def response = filters.clientToProxyRequest(request)

    then:
    response == null

    and:
    0 * tape.seek(_)
  }

  void "clientToProxyRequest returns a recorded response if found on tape"() {
    given:
    def recordedResponse = new RecordedResponse.Builder()
        .code(200)
        .body(ResponseBody.create(MediaType.parse("text/plain"), "message body"))
        .build()

    and:
    tape.isReadable() >> true
    tape.seek(_) >> true
    tape.play(_) >> recordedResponse

    expect:
    Response response = filters.clientToProxyRequest(request)
    response != null
    response.code() == 200
    readContent(response) == recordedResponse.body().bytes()

    and:
    response.headers().get(X_BETAMAX) == "PLAY"
  }

  void "serverToProxyResponse records the exchange to tape"() {
    given:
    def response = new RecordedResponse.Builder()
        .code(200)
        .body(ResponseBody.create(MediaType.parse("text/plain"), "response body"))
        .build()

    and:
    tape.isWritable() >> true

    when:
    filters.serverToProxyResponse(response)

    then:
    1 * tape.record(_, _) >> { Request recordedRequest, Response recordedResponse ->
      with(recordedRequest) {
        method() == request.method().toString()
        uri.toString() == request.uri
      }

      with(recordedResponse) {
        code() == response.code()
        body().bytes() == readContent(response)
      }
    }
  }

  void "proxyToClientResponse adds the X-Betamax header"() {
    given:
    def response = new RecordedResponse.Builder()
        .code(200)
        .build()

    when:
    filters.proxyToClientResponse(response)

    then:
    response.headers().get(X_BETAMAX) == "REC"
  }

  private static byte[] readContent(RecordedRequest response) {
    return response.body as byte[]
  }
}
