package betamax

import java.util.concurrent.TimeUnit
import org.apache.http.HttpHost
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import groovyx.net.http.*
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY
import spock.lang.*
import org.apache.http.nio.reactor.IOReactor

class ProxySpec extends Specification {

	@Shared IOReactor server

	def setupSpec() {
		System.properties."http.proxyHost" = "localhost"
		System.properties."http.proxyPort" = "5555"
		System.properties."http.nonProxyHosts" = "localhost"

        server = HttpProxyServer.start()
	}

	def cleanupSpec() {
		server.shutdown()
	}

	@Timeout(10)
	def "proxy intercepts URL connections"() {
        given:
        def connection = new URL("http://google.com/").openConnection()

		expect:
        connection.getHeaderField("X-Betamax-Proxy") == "true"

        cleanup:
        connection.disconnect()
	}

	@Timeout(10)
	def "proxy intercepts HTTPClient connections when using ProxySelectorRoutePlanner"() {
		given:
		def http = new RESTClient("http://google.com/")
		def routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
		http.client.routePlanner = routePlanner

		when:
		def response = http.get(path: "/")

		then:
		response.getFirstHeader("X-Betamax-Proxy")?.value == "true"
	}

	@Timeout(10)
	def "proxy intercepts HTTPClient connections when explicitly told to"() {
		given:
		def http = new RESTClient("http://google.com/")
		http.client.params.setParameter(DEFAULT_PROXY, new HttpHost("localhost", 5555, "http"))

		when:
		def response = http.get(path: "/")

		then:
        response.getFirstHeader("X-Betamax-Proxy")?.value == "true"
	}

	@Timeout(10)
	def "proxy intercepts HttpURLClient connections"() {
		given:
		def http = new HttpURLClient(url: "http://google.com/")

		when:
		def response = http.request(path: "/")

		then:
        response.getFirstHeader("X-Betamax-Proxy")?.value == "true"
	}

}
