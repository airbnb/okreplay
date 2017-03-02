package walkman

import okhttp3.MediaType
import okhttp3.RequestBody
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
