package co.freeside.betamax

import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.junit.Rule
import spock.lang.*

import static java.net.HttpURLConnection.HTTP_OK
import java.security.KeyStore
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.scheme.Scheme
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory

class SmokeSpec extends Specification {

	@Rule Recorder recorder = new Recorder(sslSupport: true)

	@Shared RESTClient http = new RESTClient()

	void setupSpec() {
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	@Betamax(tape = "smoke spec")
	@Unroll("#type response data")
	void "various types of response data"() {
		when:
		def response = http.get(uri: uri)

		then:
		response.status == HTTP_OK

		where:
		type   | uri
		"html" | "http://grails.org/"
		"json" | "http://api.twitter.com/1/statuses/public_timeline.json?count=3&include_entities=true"
		"xml"  | "http://feeds.feedburner.com/wondermark"
		"png"  | "http://media.xircles.codehaus.org/_projects/groovy/_logos/small.png"
		"css"  | "http://d297h9he240fqh.cloudfront.net/cache-1633a825c/assets/views_one.css"
	}

	@Betamax(tape = 'smoke spec')
	void 'https proxying'() {
		setup:
		http.client.connectionManager.schemeRegistry.register(new Scheme('https', DummySSLSocketFactory.instance, 443))

		when:
		def response = http.get(uri: uri)

		then:
		response.status == HTTP_OK

		where:
		uri = 'https://github.com/robfletcher/betamax/'
	}

}
