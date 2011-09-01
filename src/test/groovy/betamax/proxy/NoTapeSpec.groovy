package betamax.proxy

import betamax.server.HttpProxyServer
import betamax.util.EchoServer
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import groovyx.net.http.*
import static java.net.HttpURLConnection.HTTP_FORBIDDEN
import spock.lang.*
import betamax.Recorder

@Issue("https://github.com/robfletcher/betamax/issues/18")
class NoTapeSpec extends Specification {

	@Shared Recorder recorder = new Recorder()
	@Shared @AutoCleanup("stop") HttpProxyServer proxy = new HttpProxyServer()
	@Shared @AutoCleanup("stop") EchoServer endpoint = new EchoServer()
	RESTClient http

	def setupSpec() {
		proxy.start(recorder)
		endpoint.start()
	}

	def setup() {
		http = new RESTClient(endpoint.url)
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	def "an error is returned if the proxy intercepts a request when no tape is inserted"() {
		when:
		http.get(path: "/")

		then:
		def e = thrown(HttpResponseException)
		e.statusCode == HTTP_FORBIDDEN
		e.message == "No tape"
	}
}
