package betamax.proxy

import betamax.Recorder
import betamax.util.server.EchoHandler
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import betamax.proxy.jetty.*
import static org.apache.http.HttpHeaders.VIA
import spock.lang.*

@Issue("https://github.com/robfletcher/betamax/issues/16")
class IgnoreHostsSpec extends Specification {

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Shared @AutoCleanup("stop") SimpleServer endpoint = new SimpleServer()
	@AutoCleanup("ejectTape") Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@AutoCleanup("stop") ProxyServer proxy = new ProxyServer()
	RESTClient http

	def setupSpec() {
		endpoint.start(EchoHandler)
	}

	def setup() {
		http = new RESTClient(endpoint.url)
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)

		recorder.insertTape("ignore hosts spec")
	}

	def cleanup() {
		recorder.restoreOriginalProxySettings()
	}

	@Unroll("does not proxy a request to #requestURI when ignoring #ignoreHosts")
	def "does not proxy requests to ignored hosts"() {
		given: "proxy is configured to ignore local connections"
		recorder.ignoreHosts = [endpoint.url.toURI().host]
		proxy.start(recorder)
		recorder.overrideProxySettings()

		when: "a request is made"
		def response = http.get(path: "/")

		then: "the request is not intercepted by the proxy"
		!response.headers[VIA]

		and: "nothing is recorded to the tape"
		recorder.tape.size() == 0

		where:
		ignoreHosts               | requestURI
		endpoint.url.toURI().host | endpoint.url
		"localhost"               | "http://localhost:${endpoint.url.toURI().port}"
		"127.0.0.1"               | "http://localhost:${endpoint.url.toURI().port}"
	}

}
