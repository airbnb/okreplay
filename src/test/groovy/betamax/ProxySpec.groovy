package betamax

import betamax.server.HttpProxyServer
import org.apache.http.HttpHost
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import groovyx.net.http.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY
import spock.lang.*

class ProxySpec extends Specification {

	@Shared HttpProxyServer server = new HttpProxyServer()
	final url = "http://grails.org/"

	def setupSpec() {
		System.properties."http.proxyHost" = "localhost"
		System.properties."http.proxyPort" = server.port.toString()
		System.properties."http.nonProxyHosts" = "localhost"

        server.start()
	}

	def cleanupSpec() {
		server.stop()
	}

	@Timeout(10)
	def "proxy intercepts URL connections"() {
        given:
        def connection = new URL(url).openConnection()

		expect:
		connection.responseCode == HTTP_OK
        connection.getHeaderField("X-Betamax") == "REC"

        cleanup:
        connection.disconnect()
	}

	@Timeout(10)
	def "proxy intercepts HTTPClient connections when using ProxySelectorRoutePlanner"() {
		given:
		def http = new RESTClient(url)
		def routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
		http.client.routePlanner = routePlanner

		when:
		def response = http.get(path: "/")

		then:
		response.status == HTTP_OK
		response.getFirstHeader("X-Betamax")?.value == "REC"
	}

	@Timeout(10)
	def "proxy intercepts HTTPClient connections when explicitly told to"() {
		given:
		def http = new RESTClient(url)
		http.client.params.setParameter(DEFAULT_PROXY, new HttpHost("localhost", server.port, "http"))

		when:
		def response = http.get(path: "/")

		then:
		response.status == HTTP_OK
        response.getFirstHeader("X-Betamax")?.value == "REC"
	}

	@Timeout(10)
	def "proxy intercepts HttpURLClient connections"() {
		given:
		def http = new HttpURLClient(url: url)

		when:
		def response = http.request(path: "/")

		then:
		response.status == HTTP_OK
		response.getFirstHeader("X-Betamax")?.value == "REC"
	}

	@Unroll({"proxy handles $method requests"})
	def "proxy handles all request methods"() {
		given:
		def http = new RESTClient(url)
		def routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
		http.client.routePlanner = routePlanner

		when:
		def response = http."$method"(path: "/")

		then:
		response.status == HTTP_OK
        response.getFirstHeader("X-Betamax")?.value == "REC"

		cleanup:
		http.shutdown()

		where:
		method << ["get", "post", "put", "head", "delete", "options"]
	}

}
