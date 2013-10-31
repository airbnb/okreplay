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

package co.freeside.betamax

import co.freeside.betamax.util.message.BasicRequest
import spock.lang.*
import static co.freeside.betamax.MatchRules.*

@Unroll
class ComposedMatchRuleSpec extends Specification {

    void "composed rule matches if all contained rules match"() {
        given:
        def rule = ComposedMatchRule.of(* rules)

        and:
        def request1 = new BasicRequest(method1, uri1)
        def request2 = new BasicRequest(method2, uri2)

        expect:
        rule.isMatch(request1, request2) == shouldMatch

        where:
        rules         | method1 | uri1                  | method2 | uri2                         | shouldMatch
        [method, uri] | "GET"   | "http://freeside.co/" | "GET"   | "http://freeside.co/"        | true
        [method, uri] | "GET"   | "http://freeside.co/" | "GET"   | "http://freeside.co/betamax" | false
        [method]      | "GET"   | "http://freeside.co/" | "GET"   | "http://freeside.co/betamax" | true
        [method, uri] | "GET"   | "http://freeside.co/" | "POST"  | "http://freeside.co/"        | false
    }

}
