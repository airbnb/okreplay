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

import co.freeside.betamax.message.*
import spock.lang.Specification

class ChainedHttpHandlerSpec extends Specification {

    def handler = new ChainedHttpHandler() {
        @Override
        Response handle(Request request) {
            throw new UnsupportedOperationException()
        }
    }

    def request = [:] as Request
    def response = [:] as Response

    void "throws an exception if chain is called on the last handler in the chain"() {
        when:
        handler.chain(request)

        then:
        thrown IllegalStateException
    }

    void "chain passes to the next handler if there is one"() {
        given:
        def nextHandler = Mock(HttpHandler)
        handler.add(nextHandler)

        when:
        def result = handler.chain(request)

        then:
        1 * nextHandler.handle(request) >> response

        and:
        result.is(response)
    }
}
