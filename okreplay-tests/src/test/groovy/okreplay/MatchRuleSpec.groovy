package okreplay

import okhttp3.MediaType
import okhttp3.RequestBody
import okreplay.RecordedRequest
import spock.lang.Issue
import spock.lang.Specification

import static com.google.common.net.HttpHeaders.AUTHORIZATION
import static com.google.common.net.HttpHeaders.CACHE_CONTROL
import static okreplay.MatchRules.*

@Issue('https://github.com/robfletcher/betamax/issues/9')
class MatchRuleSpec extends Specification {
  void 'can match method and url'() {
    given:
    def request1 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .build()
    def request2 = new RecordedRequest.Builder()
        .method('HEAD', null)
        .url('http://freeside.co/betamax')
        .build()
    def request3 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax?q=1')
        .build()

    and:
    def request = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .build()
    def rule = ComposedMatchRule.of(method, uri)

    expect:
    rule.isMatch(request, request1)
    !rule.isMatch(request, request2)
    !rule.isMatch(request, request3)
  }

  void 'can match host'() {
    given:
    def request1 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .build()
    def request2 = new RecordedRequest.Builder()
        .url('http://freeside.co/grails-fields')
        .build()
    def request3 = new RecordedRequest.Builder()
        .method('HEAD', null)
        .url('http://freeside.co/betamax')
        .build()
    def request4 = new RecordedRequest.Builder()
        .url('http://icanhascheezburger.com/')
        .build()

    and:
    def request = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .build()
    def rule = host

    expect:
    rule.isMatch(request, request1)
    rule.isMatch(request, request2)
    rule.isMatch(request, request3)
    !rule.isMatch(request, request4)
  }

  void 'can match path'() {
    given:
    def request1 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .build()
    def request2 = new RecordedRequest.Builder()
        .url('http://robfletcher.github.com/grails-enhanced-scaffolding')
        .build()
    def request3 = new RecordedRequest.Builder()
        .method('HEAD', null)
        .url('http://freeside.co/betamax')
        .build()
    def request4 = new RecordedRequest.Builder()
        .url('http://icanhascheezburger.com/betamax')
        .build()

    and:
    def request = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .build()
    def rule = path

    expect:
    rule.isMatch(request, request1)
    !rule.isMatch(request, request2)
    rule.isMatch(request, request3)
    rule.isMatch(request, request4)
  }

  void 'can match authorization'() {
    given:
    def request1 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .addHeader(AUTHORIZATION, 'Basic 1234')
        .build()
    def request2 = new RecordedRequest.Builder()
        .url('http://icanhascheezburger.com/')
        .addHeader(AUTHORIZATION, 'Basic 1234')
        .addHeader(CACHE_CONTROL, 'no-cache')
        .build()
    def request3 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .addHeader(AUTHORIZATION, 'Basic 4321')
        .build()
    def request4 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .addHeader(AUTHORIZATION, 'Basic 4321')
        .addHeader(CACHE_CONTROL, 'no-cache')
        .build()

    and:
    def request = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .addHeader(AUTHORIZATION, 'Basic 1234')
        .build()
    def rule = authorization

    expect:
    rule.isMatch(request, request1)
    rule.isMatch(request, request2)
    !rule.isMatch(request, request3)
    !rule.isMatch(request, request4)
  }

  void 'can match post body'() {
    def mediaType = MediaType.parse("text/plain")
    given:
    def request1 = new RecordedRequest.Builder()
        .method('POST', RequestBody.create(mediaType, 'q=1'))
        .url('http://freeside.co/betamax')
        .build()
    def request2 = new RecordedRequest.Builder()
        .method('POST', RequestBody.create(mediaType, 'q=2'))
        .url('http://freeside.co/betamax')
        .build()
    def request3 = new RecordedRequest.Builder()
        .method('POST', RequestBody.create(mediaType, 'q=1&r=1'))
        .url('http://freeside.co/betamax')
        .build()

    and:
    def request = new RecordedRequest.Builder()
        .method('POST', RequestBody.create(mediaType, 'q=1'))
        .url('http://freeside.co/betamax')
        .build()
    def rule = body

    expect:
    rule.isMatch(request, request1)
    !rule.isMatch(request, request2)
    !rule.isMatch(request, request3)
  }

  void 'can match query parameters in different orders'() {
    given:
    def request1 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax?p=2&q=1')
        .build()
    def request2 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax?p=2&q=2')
        .build()
    def request3 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .build()
    def request4 = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax')
        .build()

    and:
    def request = new RecordedRequest.Builder()
        .url('http://freeside.co/betamax?q=1&p=2')
        .build()
    def rule = ComposedMatchRule.of(method, queryParams)

    expect:
    rule.isMatch(request, request1)
    !rule.isMatch(request, request2)
    rule.isMatch(request3, request4)
    !rule.isMatch(request, request3)
  }
}
