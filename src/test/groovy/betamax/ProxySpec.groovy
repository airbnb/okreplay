package betamax

import groovyx.net.http.RESTClient
import java.util.concurrent.TimeUnit
import org.apache.http.HttpHost
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY
import spock.lang.*

class ProxySpec extends Specification {

	@Shared def server = new ProxyServer()

	def setupSpec() {
		System.properties."http.proxyHost" = "localhost"
		System.properties."http.proxyPort" = "5555"
		System.properties."http.nonProxyHosts" = "localhost"

		new Thread(server).start()
		server.waitUntilRunning(10, TimeUnit.SECONDS)
	}

	def cleanupSpec() {
		server.stop()
	}

	@Timeout(10)
	def "proxy intercepts URL connections"() {
		when:
		def response = new URL("http://google.com/").text

		then:
		response =~ /^Hello from the proxy!/
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
		response.data.text =~ /^Hello from the proxy!/
	}

	@Timeout(10)
	def "proxy intercepts HTTPClient connections when explicitly told to"() {
		given:
		def http = new RESTClient("http://google.com/")
		http.client.params.setParameter(DEFAULT_PROXY, new HttpHost("localhost", 5555, "http"))

		when:
		def response = http.get(path: "/")

		then:
		response.data.text =~ /^Hello from the proxy!/
	}

}
