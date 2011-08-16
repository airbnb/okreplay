package betamax

import betamax.server.HttpProxyServer
import org.apache.http.HttpHost
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import groovyx.net.http.*
import static groovyx.net.http.ContentType.URLENC
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY
import spock.lang.*
import betamax.util.EchoServer

class ProxySpec extends Specification {

	@Shared HttpProxyServer proxy = new HttpProxyServer()
    EchoServer endpoint = new EchoServer()
	String url

	def setupSpec() {
		System.properties."http.proxyHost" = "localhost"
		System.properties."http.proxyPort" = proxy.port.toString()

		proxy.start()
	}

    def setup() {
        url = endpoint.start()
    }

	def cleanupSpec() {
		proxy.stop()
	}

	@Timeout(10)
	def "proxy intercepts URL connections"() {
		given:
		HttpURLConnection connection = new URL(url).openConnection()
		connection.connect()

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
		http.client.params.setParameter(DEFAULT_PROXY, new HttpHost("localhost", proxy.port, "http"))

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

	@Timeout(10)
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

	@Timeout(10)
	def "proxy forwards query string"() {
		given:
		def http = new RESTClient(url)
		def routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
		http.client.routePlanner = routePlanner

		when:
		def response = http.get(path: "/", query: [q: "1"])

		then:
		response.status == HTTP_OK
		response.data.text.contains("GET /?q=1 HTTP/1.1")

		cleanup:
		http.shutdown()
	}

	@Timeout(10)
	def "proxy forwards post data"() {
		given:
		def http = new RESTClient(url)
		def routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
		http.client.routePlanner = routePlanner

		when:
		def response = http.post(path: "/", body: [q: "1"], requestContentType: URLENC)

		then:
		response.status == HTTP_OK
		response.data.text.endsWith("\nq=1")

		cleanup:
		http.shutdown()
	}

}
