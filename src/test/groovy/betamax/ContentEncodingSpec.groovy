package betamax

import betamax.server.HttpProxyServer
import betamax.util.EchoServer
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import static org.apache.http.HttpHeaders.CONTENT_ENCODING
import spock.lang.*

@Issue("https://github.com/robfletcher/betamax/issues/3")
class ContentEncodingSpec extends Specification {

	@Shared File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Shared Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared HttpProxyServer proxy = new HttpProxyServer()
	@AutoCleanup("stop") EchoServer endpoint = new EchoServer()
	RESTClient http

	def setupSpec() {
		recorder.insertTape("content_encoding_spec")
		proxy.start(recorder)
	}

	def setup() {
		endpoint.start()

		http = new RESTClient(endpoint.url)
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	def cleanupSpec() {
		proxy.stop()
		recorder.ejectTape()
		assert tapeRoot.deleteDir()
	}

	def "by default a gzipped response is stored gzipped"() {
		given:
		http.get(path: "/")

		expect:
		recorder.tape.interactions[0].response.getFirstHeader(CONTENT_ENCODING).value == "gzip"
	}

}
