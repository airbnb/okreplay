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

	@Rule Recorder recorder = new ProxyRecorder(sslSupport: true)

	@Shared RESTClient http = new BetamaxRESTClient()

	@Betamax(tape = 'smoke spec')
	void '#type response data'() {
		when:
		HttpResponseDecorator response = http.get(uri: uri)

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA).value == 'Betamax'

		where:
		type   | uri
		'txt'  | 'http://httpbin.org/robots.txt'
		'html' | 'http://httpbin.org/html'
		'json' | 'http://httpbin.org/get'
//		'xml'  | 'http://blog.freeside.co/rss'
//		'png'  | 'https://si0.twimg.com/profile_images/1665962331/220px-Betamax_Logo_normal.png'
		'css'  | 'http://freeside.co/betamax/stylesheets/betamax.css'
	}

	@Betamax(tape = 'smoke spec')
	void 'gzipped response data'() {
		when:
		HttpResponseDecorator response = http.get(uri: uri)

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA).value == 'Betamax'

		where:
		uri = 'http://httpbin.org/gzip'
	}

	@Betamax(tape = 'smoke spec')
	void 'redirects are followed'() {
		when:
		HttpResponseDecorator response = http.get(uri: uri)

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA).value == 'Betamax'

		where:
		uri = 'http://httpbin.org/redirect/1'
	}

	@Ignore("until HTTPS support implemented")
	@Betamax(tape = 'smoke spec')
	void 'https proxying'() {
		setup:
		BetamaxHttpsSupport.configure(http.client)

		when:
		HttpResponseDecorator response = http.get(uri: uri)

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA).value == 'Betamax'

		where:
		uri = 'https://httpbin.org/get'
	}

	@Ignore("site is no longer there")
	@Issue('https://github.com/robfletcher/betamax/issues/52')
	@Betamax(tape = 'ocsp')
	void 'OCSP messages'() {
		when:
		HttpResponseDecorator response = http.post(uri: 'http://ocsp.ocspservice.com/public/ocsp')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA).value == 'Betamax'
		response.data.bytes.length == 2529
	}

	@Issue(['https://github.com/robfletcher/betamax/issues/61', 'http://jira.codehaus.org/browse/JETTY-1533'])
	@Betamax(tape = 'smoke spec')
	void 'can cope with URLs that do not end in a slash'() {
		when:
		HttpResponseDecorator response = http.get(uri: uri)

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA).value == 'Betamax'

		where:
		uri = 'http://httpbin.org'
	}
}
