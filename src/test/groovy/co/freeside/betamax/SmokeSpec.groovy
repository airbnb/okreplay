package co.freeside.betamax

import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.apache.http.conn.scheme.Scheme
import org.junit.Rule
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static java.net.HttpURLConnection.HTTP_OK

@Unroll
class SmokeSpec extends Specification {

	@Rule Recorder recorder = new Recorder(sslSupport: true)

	@Shared RESTClient http = new RESTClient()

	void setupSpec() {
		BetamaxRoutePlanner.configure(http.client)
	}

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
		http.client.connectionManager.schemeRegistry.register(new Scheme('https', DummySSLSocketFactory.instance, 443))

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

}
