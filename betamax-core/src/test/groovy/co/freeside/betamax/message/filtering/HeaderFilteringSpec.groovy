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

package co.freeside.betamax.message.filtering

import co.freeside.betamax.util.message.*
import spock.lang.*
import static co.freeside.betamax.message.filtering.HeaderFilteringMessage.NO_PASS_HEADERS
import static com.google.common.net.HttpHeaders.CONTENT_TYPE
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8

@Unroll
class HeaderFilteringSpec extends Specification {

    void "ServletRequestAdapter filters out #header headers"() {

        given: "a servlet request"
        def request = new BasicRequest()
        request.addHeader(CONTENT_TYPE, PLAIN_TEXT_UTF_8.withoutParameters().toString())
        request.addHeader(header, "value")

        when: "we wrap it"
        def filteringRequest = new HeaderFilteringRequest(request)

        then: "filtered headers are removed"
        filteringRequest.headers.keySet() == [CONTENT_TYPE] as Set
        filteringRequest.getHeader(header) == null

        and: "unfiltered headers are still available"
        filteringRequest.getHeader(CONTENT_TYPE) == request.getHeader(CONTENT_TYPE)

        where:
        header << NO_PASS_HEADERS

    }

    void "HttpResponseAdapter filters out #header headers"() {

        given: "an HttpClient response"
        def response = new BasicResponse()
        response.addHeader(CONTENT_TYPE, PLAIN_TEXT_UTF_8.toString())
        response.addHeader(header, "value")

        when: "we wrap it"
        def filteringResponse = new HeaderFilteringResponse(response)

        then: "filtered headers are removed"
        filteringResponse.headers.keySet() == [CONTENT_TYPE] as Set
        filteringResponse.getHeader(header) == null

        and: "unfiltered headers are still available"
        filteringResponse.getHeader(CONTENT_TYPE) == response.getHeader(CONTENT_TYPE)

        where:
        header << NO_PASS_HEADERS

    }

}
