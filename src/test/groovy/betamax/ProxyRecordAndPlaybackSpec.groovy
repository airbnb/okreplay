package betamax

import betamax.server.HttpProxyServer
import betamax.util.EchoServer
import groovyx.net.http.HttpURLClient
import spock.lang.*

@Stepwise
class ProxyRecordAndPlaybackSpec extends Specification {

	@Shared HttpProxyServer proxy = new HttpProxyServer()
	EchoServer endpoint = new EchoServer()
	String url

	def setupSpec() {
		System.properties."http.proxyHost" = "localhost"
		System.properties."http.proxyPort" = proxy.port.toString()

		Betamax.instance.insertTape("test")

		proxy.start()
	}

    def setup() {
        url = endpoint.start()
    }

    def cleanup() {
        endpoint.stop()
    }

	def cleanupSpec() {
		proxy.stop()
	}

	def "proxy makes processes a real HTTP request the first time it gets a request for a URI"() {
		given:
		def http = new HttpURLClient(url: url)

		when:
		http.request(path: "/")

		then:
		Betamax.instance.tape.interactions.size() == 1
	}

	def "subsequent requests for the same URI are played back from tape"() {
		given:
		def http = new HttpURLClient(url: url)

		when:
		http.request(path: "/")

		then:
		Betamax.instance.tape.interactions.size() == 1
	}

}
