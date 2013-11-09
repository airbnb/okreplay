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

package co.freeside.betamax.handler

import co.freeside.betamax.util.message.BasicRequest
import org.apache.http.*
import org.apache.http.client.HttpClient
import org.apache.http.message.BasicHttpResponse
import spock.lang.*
import static com.google.common.base.Charsets.ISO_8859_1
import static com.google.common.net.HttpHeaders.ACCEPT_LANGUAGE
import static com.google.common.net.HttpHeaders.CONTENT_TYPE
import static com.google.common.net.HttpHeaders.IF_NONE_MATCH
import static com.google.common.net.MediaType.FORM_DATA
import static java.net.HttpURLConnection.HTTP_OK

@Unroll
class TargetConnectorSpec extends Specification {

    HttpClient httpClient = Mock(HttpClient)
    TargetConnector handler = new TargetConnector(httpClient)

    BasicRequest request = new BasicRequest("GET", "http://freeside.co/")
    HttpResponse okResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HTTP_OK, "OK")

    void "proceeds request to original target and returns response"() {
        when:
        def response = handler.handle(request)

        then:
        1 * httpClient.execute(_, _) >> { httpHost, outboundRequest ->
            assert outboundRequest.requestLine.method == request.method
            assert outboundRequest.requestLine.uri == request.uri.toString()
            okResponse
        }

        and:
        response.status == 200
    }

    void "uses #method for the outbound request method"() {
        given:
        request.method = method

        when:
        handler.handle(request)

        then:
        1 * httpClient.execute(_, _) >> { httpHost, outboundRequest ->
            assert outboundRequest.requestLine.method == request.method.toString()
            okResponse
        }

        where:
        method << ["GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"]
    }

    void "sends headers with outbound request"() {
        given:
        request.addHeader(ACCEPT_LANGUAGE, "en_GB, en")
        request.addHeader(IF_NONE_MATCH, "abc123")

        when:
        handler.handle(request)

        then:
        1 * httpClient.execute(_, _) >> { httpHost, outboundRequest ->
            assert request.headers.every { name, value ->
                outboundRequest.getFirstHeader(name)?.value == value
            }
            okResponse
        }
    }

    void "sends body with outbound request"() {
        given:
        request.method = "POST"
        request.addHeader(CONTENT_TYPE, FORM_DATA.toString())
        request.body = "price=\u003199.99".getBytes(ISO_8859_1)

        when:
        handler.handle(request)

        then:
        1 * httpClient.execute(_, _) >> { httpHost, outboundRequest ->
            def entity = outboundRequest.entity
            assert entity.content.text == request.bodyAsText.input.text
            okResponse
        }
    }

    void "throws an exception with HTTP status #httpStatus if outbound request #description"() {
        given:
        httpClient.execute(_, _) >> {
            throw originalExceptionClass.newInstance()
        }

        when:
        handler.handle(request)

        then:
        thrown expectedExceptionClass

        where:
        originalExceptionClass | expectedExceptionClass | description
        SocketTimeoutException | TargetTimeoutException | "times out"
        IOException            | TargetErrorException   | "fails"
    }

}
