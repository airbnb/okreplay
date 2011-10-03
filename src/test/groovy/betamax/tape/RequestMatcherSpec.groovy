package betamax.tape

import betamax.util.message.BasicRequest
import static betamax.MatchRule.*
import static org.apache.http.HttpHeaders.*
import spock.lang.*

@Issue("https://github.com/robfletcher/betamax/issues/9")
class RequestMatcherSpec extends Specification {

	def "by default matches method and url"() {
		given:
		def request1 = new RecordedRequest(method: "GET", uri: "http://robfletcher.github.com/betamax".toURI())
		def request2 = new RecordedRequest(method: "HEAD", uri: "http://robfletcher.github.com/betamax".toURI())
		def request3 = new RecordedRequest(method: "GET", uri: "http://robfletcher.github.com/betamax?q=1".toURI())

		and:
		def request = new BasicRequest(method: "GET", uri: "http://robfletcher.github.com/betamax".toURI())
		def requestMatcher = new RequestMatcher(request)

		expect:
		requestMatcher.matches(request1)
		!requestMatcher.matches(request2)
		!requestMatcher.matches(request3)
	}

	def "can match host"() {
		given:
		def request1 = new RecordedRequest(method: "GET", uri: "http://robfletcher.github.com/betamax".toURI())
		def request2 = new RecordedRequest(method: "GET", uri: "http://robfletcher.github.com/grails-enhanced-scaffolding".toURI())
		def request3 = new RecordedRequest(method: "HEAD", uri: "http://robfletcher.github.com/betamax".toURI())
		def request4 = new RecordedRequest(method: "GET", uri: "http://icanhascheezburger.com/".toURI())

		and:
		def request = new BasicRequest(method: "GET", uri: "http://robfletcher.github.com/betamax".toURI())
		def requestMatcher = new RequestMatcher(request, host)

		expect:
		requestMatcher.matches(request1)
		requestMatcher.matches(request2)
		requestMatcher.matches(request3)
		!requestMatcher.matches(request4)
	}

	def "can match path"() {
		given:
		def request1 = new RecordedRequest(method: "GET", uri: "http://robfletcher.github.com/betamax".toURI())
		def request2 = new RecordedRequest(method: "GET", uri: "http://robfletcher.github.com/grails-enhanced-scaffolding".toURI())
		def request3 = new RecordedRequest(method: "HEAD", uri: "http://robfletcher.github.com/betamax".toURI())
		def request4 = new RecordedRequest(method: "GET", uri: "http://icanhascheezburger.com/betamax".toURI())

		and:
		def request = new BasicRequest(method: "GET", uri: "http://robfletcher.github.com/betamax".toURI())
		def requestMatcher = new RequestMatcher(request, path)

		expect:
		requestMatcher.matches(request1)
		!requestMatcher.matches(request2)
		requestMatcher.matches(request3)
		requestMatcher.matches(request4)
	}

	def "can match headers"() {
		given:
		def request1 = new RecordedRequest(method: "GET", uri: "http://robfletcher.github.com/betamax".toURI(), headers: [(ACCEPT_ENCODING): "gzip, deflate"])
		def request2 = new RecordedRequest(method: "GET", uri: "http://icanhascheezburger.com/".toURI(), headers: [(ACCEPT_ENCODING): "gzip, deflate"])
		def request3 = new RecordedRequest(method: "GET", uri: "http://robfletcher.github.com/betamax".toURI(), headers: [(ACCEPT_ENCODING): "none"])
		def request4 = new RecordedRequest(method: "GET", uri: "http://robfletcher.github.com/betamax".toURI(), headers: [(ACCEPT_ENCODING): "gzip, deflate", (CACHE_CONTROL): "no-cache"])

		and:
		def request = new BasicRequest(method: "GET", uri: "http://robfletcher.github.com/betamax".toURI(), headers: [(ACCEPT_ENCODING): ["gzip", "deflate"]])
		def requestMatcher = new RequestMatcher(request, headers)

		expect:
		requestMatcher.matches(request1)
		requestMatcher.matches(request2)
		!requestMatcher.matches(request3)
		!requestMatcher.matches(request4)
	}

	def "can match post body"() {
		given:
		def request1 = new RecordedRequest(method: "POST", uri: "http://robfletcher.github.com/betamax".toURI(), body: "q=1")
		def request2 = new RecordedRequest(method: "POST", uri: "http://robfletcher.github.com/betamax".toURI(), body: "q=2")
		def request3 = new RecordedRequest(method: "POST", uri: "http://robfletcher.github.com/betamax".toURI(), body: "q=1&r=1")

		and:
		def request = new BasicRequest(method: "POST", uri: "http://robfletcher.github.com/betamax".toURI(), body: "q=1")
		def requestMatcher = new RequestMatcher(request, body)

		expect:
		requestMatcher.matches(request1)
		!requestMatcher.matches(request2)
		!requestMatcher.matches(request3)
	}

}
