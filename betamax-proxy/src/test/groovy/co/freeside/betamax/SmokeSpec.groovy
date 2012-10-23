package co.freeside.betamax

import co.freeside.betamax.httpclient.BetamaxHttpsSupport
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import groovyx.net.http.*
import org.junit.Rule
import spock.lang.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Unroll
class SmokeSpec extends Specification {

	@Rule Recorder recorder = new Recorder(sslSupport: true)

	@Shared RESTClient http = new BetamaxRESTClient()

	@Betamax(tape = 'smoke spec')
	void '#type response data'() {
		when:
		HttpResponseDecorator response = http.get(uri: uri)

		then:
		response.status == HTTP_OK

		where:
		type   | uri
		'html' | 'http://grails.org/'
		'json' | 'http://api.twitter.com/1/statuses/public_timeline.json?count=3&include_entities=true'
		'xml'  | 'http://feeds.feedburner.com/wondermark'
		'png'  | 'http://media.xircles.codehaus.org/_projects/groovy/_logos/small.png'
		'css'  | 'http://d297h9he240fqh.cloudfront.net/cache-1633a825c/assets/views_one.css'
	}

	@Betamax(tape = 'smoke spec')
	void 'https proxying'() {
		setup:
		BetamaxHttpsSupport.configure(http.client)

		when:
		HttpResponseDecorator response = http.get(uri: uri)

		then:
		response.status == HTTP_OK

		where:
		uri = 'https://github.com/robfletcher/betamax/'
	}

	@Issue('https://github.com/robfletcher/betamax/issues/52')
	@Betamax(tape = 'ocsp')
	void 'OCSP messages'() {
		when:
		HttpResponseDecorator response = http.post(uri: 'http://ocsp.ocspservice.com/public/ocsp')

		then:
		response.status == HTTP_OK
		response.data.bytes.length == 2529
	}

	@Issue(['https://github.com/robfletcher/betamax/issues/61', 'http://jira.codehaus.org/browse/JETTY-1533'])
	@Ignore('Jetty issue is fixed but not yet in a release once it is it will solve this problem')
	@Betamax(tape = 'smoke spec')
	void 'can cope with URLs that do not end in a slash'() {
		given:
		def uri = 'http://freeside.co'

		when:
		HttpResponseDecorator response = http.get(uri: uri)

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA) == 'Betamax'
	}
}
