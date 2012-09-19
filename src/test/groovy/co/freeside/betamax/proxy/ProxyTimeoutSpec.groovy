package co.freeside.betamax.proxy

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.SlowHandler
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.junit.Rule
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import static java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT

@Issue("https://github.com/robfletcher/betamax/issues/20")
class ProxyTimeoutSpec extends Specification {

	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot, proxyTimeout: 100)

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@AutoCleanup("stop") SimpleServer endpoint = new SimpleServer()
	RESTClient http

	def setup() {
		http = new RESTClient(endpoint.url)
		BetamaxRoutePlanner.configure(http.client)
		http.client.httpRequestRetryHandler = new DefaultHttpRequestRetryHandler(0, false)
	}

	@Betamax(tape = "proxy timeout spec")
	def "proxy responds with 504 if target server takes too long to respond"() {
		given:
		endpoint.start(SlowHandler)

		when:
		http.get(path: "/")

		then:
		def e = thrown(HttpResponseException)
		e.statusCode == HTTP_GATEWAY_TIMEOUT
		e.message == "Timed out connecting to $endpoint.url"
	}

}
