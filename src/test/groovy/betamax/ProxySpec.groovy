package betamax

import org.apache.http.HttpHost
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.apache.log4j.Logger
import groovyx.net.http.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY
import static org.apache.log4j.Level.DEBUG
import spock.lang.*
import org.apache.http.nio.reactor.IOReactor

class ProxySpec extends Specification {

//	@Shared HttpProxyServer server = new HttpProxyServer()
	@Shared IOReactor reactor

	def setupSpec() {
		Logger.getLogger("betamax").level = DEBUG

		System.properties."http.proxyHost" = "localhost"
		System.properties."http.proxyPort" = "5555"
		System.properties."http.nonProxyHosts" = "localhost"

        reactor = HttpProxyServer.start()
	}

	def cleanupSpec() {
		reactor.shutdown()
	}

	@Timeout(10)
	def "proxy intercepts URL connections"() {
        given:
        HttpURLConnection connection = new URL("http://grails.org/").openConnection()

		expect:
		connection.responseCode == HTTP_OK
        connection.getHeaderField("X-Betamax") == "REC"

        cleanup:
        connection.disconnect()
	}

	@Timeout(10)
	def "proxy intercepts HTTPClient connections when using ProxySelectorRoutePlanner"() {
		given:
		def http = new RESTClient("http://grails.org/")
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
		def http = new RESTClient("http://grails.org/")
		http.client.params.setParameter(DEFAULT_PROXY, new HttpHost("localhost", 5555, "http"))

		when:
		def response = http.get(path: "/")

		then:
		response.status == HTTP_OK
        response.getFirstHeader("X-Betamax")?.value == "REC"
	}

	@Timeout(10)
	def "proxy intercepts HttpURLClient connections"() {
		given:
		def http = new HttpURLClient(url: "http://grails.org/")

		when:
		def response = http.request(path: "/")

		then:
		response.status == HTTP_OK
		response.getFirstHeader("X-Betamax")?.value == "REC"
	}

}
