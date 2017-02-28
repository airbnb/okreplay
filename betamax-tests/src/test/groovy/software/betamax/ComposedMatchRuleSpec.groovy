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

package software.betamax

import okhttp3.MediaType
import okhttp3.RequestBody
import software.betamax.message.tape.RecordedRequest
import spock.lang.Specification
import spock.lang.Unroll

import static MatchRules.method
import static MatchRules.uri

@Unroll
class ComposedMatchRuleSpec extends Specification {
  void "composed rule matches if all contained rules match"() {
    given:
    def rule = ComposedMatchRule.of(*rules)

    and:
    def request1 = new RecordedRequest.Builder()
        .method(method1, null)
        .url(uri1).build()
    def body = RequestBody.create(MediaType.parse("text/plain"), "foo")
    def request2 = new RecordedRequest.Builder()
        .method(method2, method2 == 'GET' ? null : body)
        .url(uri2)
        .build()

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
