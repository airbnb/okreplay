package betamax

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

	@Betamax(tape = "smoke spec")
	def "json response data"() {
		when:
		def response = http.get(uri: uri)

		then:
		response.status == HTTP_OK

		where:
		uri << ["http://api.twitter.com/1/statuses/public_timeline.json?count=3&include_entities=true"]
	}
	
}
