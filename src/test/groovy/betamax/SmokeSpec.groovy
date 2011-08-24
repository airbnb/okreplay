package betamax

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.junit.Rule
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.ACCEPT_ENCODING
import spock.lang.*

class SmokeSpec extends Specification {

	@Rule Recorder recorder = new Recorder()

	@Shared DefaultHttpClient http = new DefaultHttpClient()

	def setupSpec() {
		http.routePlanner = new ProxySelectorRoutePlanner(http.connectionManager.schemeRegistry, ProxySelector.default)
	}

	@Betamax(tape = "smoke spec")
	def "json response data"() {
		given:
		def request = new HttpGet(uri)
		request.setHeader(ACCEPT_ENCODING, "none")

		when:
		def response = http.execute(request)

		then:
		response.statusLine.statusCode == HTTP_OK

		where:
		uri << ["http://api.twitter.com/1/statuses/public_timeline.json?count=3&include_entities=true"]
	}
	
}
