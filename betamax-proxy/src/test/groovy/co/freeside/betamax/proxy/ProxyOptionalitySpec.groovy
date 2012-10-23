package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.HelloHandler
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.SystemDefaultHttpClient
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Issue('https://github.com/robfletcher/betamax/issues/40')
class ProxyOptionalitySpec extends Specification {

	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	@Rule Recorder recorder = new BetamaxProxyRecorder(useProxy: false)

	void setupSpec() {
		endpoint.start(HelloHandler)
	}

	@Betamax(tape = 'proxy optionality')
	void 'the Betamax proxy does not start if not used by the recorder'() {
		given:
		def http = new SystemDefaultHttpClient()

		and:
		def request = new HttpGet(endpoint.url)

		when:
		def response = http.execute(request)

		then:
		response.statusLine.statusCode == HTTP_OK
		response.entity.content.text == HELLO_WORLD

		and:
		!response.getFirstHeader(VIA)
		!response.getFirstHeader('X-Betamax')
	}

}
