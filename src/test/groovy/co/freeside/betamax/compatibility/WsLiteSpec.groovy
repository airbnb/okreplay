package co.freeside.betamax.compatibility

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import org.junit.Rule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import wslite.rest.RESTClient
import static co.freeside.betamax.proxy.jetty.BetamaxProxy.X_BETAMAX
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

class WsLiteSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	RESTClient http = new RESTClient(endpoint.url)

	@Betamax(tape = 'wslite spec')
	void 'can record a connection made with WsLite'() {
		given:
		endpoint.start(EchoHandler)
		def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('localhost', recorder.proxyPort))

		when:
		def response = http.get(path: '/', proxy: proxy)

		then:
		response.statusCode == HTTP_OK
		response.headers[VIA] == 'Betamax'
		response.headers[X_BETAMAX] == 'REC'
	}

}
