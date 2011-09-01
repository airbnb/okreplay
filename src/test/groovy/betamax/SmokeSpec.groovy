package betamax

import betamax.server.HttpProxyServer
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.junit.Rule
import static java.net.HttpURLConnection.HTTP_OK
import spock.lang.*

class SmokeSpec extends Specification {

	@Rule Recorder recorder = new Recorder()

	@Shared RESTClient http = new RESTClient()

	def setupSpec() {
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	def cleanupSpec() {
		HttpProxyServer.instance.stop()
	}

	@Betamax(tape = "smoke spec")
	@Unroll("#type response data")
	def "various types of response data"() {
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
	}

}
