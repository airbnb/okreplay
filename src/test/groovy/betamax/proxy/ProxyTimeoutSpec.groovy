package betamax.proxy

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.junit.Rule
import betamax.*

import groovyx.net.http.*
import static java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT
import spock.lang.*
import betamax.util.server.SimpleServer
import betamax.util.server.SlowHandler

@Issue("https://github.com/robfletcher/betamax/issues/20")
class ProxyTimeoutSpec extends Specification {

	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot, proxyTimeout: 100)

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@AutoCleanup("stop") SimpleServer endpoint = new SimpleServer()
	RESTClient http

	def setup() {
		http = new RESTClient(endpoint.url)
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
		http.client.httpRequestRetryHandler = new DefaultHttpRequestRetryHandler(0, false)
	}

	@Betamax(tape = "proxy timeout spec")
	def "proxy responds with 504 if target server takes too long to respond"() {
		given:
		endpoint.start(SlowHandler)

		when:
		http.head(path: "/")

		then:
		def e = thrown(HttpResponseException)
		e.statusCode == HTTP_GATEWAY_TIMEOUT
		e.message == "Target server took too long to respond"
	}

}
