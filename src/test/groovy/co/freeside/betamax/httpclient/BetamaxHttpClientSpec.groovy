package co.freeside.betamax.httpclient

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.HelloHandler
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

class BetamaxHttpClientSpec extends Specification {

	@AutoCleanup('deleteDir') File tapeRoot = new File(System.properties.'java.io.tmpdir', 'tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot, proxy: Mock(HttpInterceptor))
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	void setupSpec() {
		endpoint.start(HelloHandler)

	}

	@Betamax(tape = 'betamax http client')
	void 'can use Betamax without starting the proxy'() {
		given:
		def http = new BetamaxHttpClient(new DefaultHttpClient(), recorder)
		def request = new HttpGet(endpoint.url)

		when:
		def response = http.execute(request)

		then:
		response.statusLine.statusCode == HTTP_OK
		response.entity.content.text == HELLO_WORLD

		and:
		response.getFirstHeader(VIA).value == 'BetamaxHttpClient'
		response.getFirstHeader('X-Betamax').value == 'REC'
	}

}
