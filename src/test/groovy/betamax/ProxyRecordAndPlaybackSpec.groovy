package betamax

import betamax.server.HttpProxyServer
import betamax.util.EchoServer
import groovyx.net.http.HttpURLClient
import spock.lang.*

@Stepwise
class ProxyRecordAndPlaybackSpec extends Specification {

	@Shared HttpProxyServer proxy = new HttpProxyServer()
	EchoServer endpoint = new EchoServer()

	def setupSpec() {
		System.properties."http.proxyHost" = "localhost"
		System.properties."http.proxyPort" = proxy.port.toString()

		Betamax.instance.tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
		Betamax.instance.insertTape("proxy_record_and_playback_spec")

		proxy.start()
	}

	def cleanupSpec() {
		proxy.stop()
	}

	@Timeout(10)
	def "proxy makes processes a real HTTP request the first time it gets a request for a URI"() {
		given:
		endpoint.start()

		and:
		def http = new HttpURLClient(url: endpoint.url)

		when:
		http.request(path: "/")

		then:
		Betamax.instance.tape.interactions.size() == 1

		cleanup:
		endpoint.awaitStop()
	}

	@Timeout(10)
	def "subsequent requests for the same URI are played back from tape"() {
		given:
		def http = new HttpURLClient(url: endpoint.url)

		when:
		http.request(path: "/")

		then:
		Betamax.instance.tape.interactions.size() == 1
	}

	def "when the proxy is stopped the tape is written to a file"() {
		when:
		proxy.stop()

		then:
		Betamax.instance.tape.file.isFile()
	}

}
