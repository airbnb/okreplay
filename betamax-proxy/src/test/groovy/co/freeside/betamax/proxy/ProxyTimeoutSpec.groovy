package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.SlowHandler
import groovyx.net.http.*
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT

@Issue('https://github.com/robfletcher/betamax/issues/20')
class ProxyTimeoutSpec extends Specification {

	@Rule Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot, proxyTimeout: 100)

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	RESTClient http = new BetamaxRESTClient(endpoint.url)

	void setup() {
		http.client.httpRequestRetryHandler = new DefaultHttpRequestRetryHandler(0, false)
	}

	@Betamax(tape = 'proxy timeout spec', mode = TapeMode.READ_WRITE)
	void 'proxy responds with 504 if target server takes too long to respond'() {
		given:
		endpoint.start(SlowHandler)

		when:
		http.get(path: '/')

		then:
		def e = thrown(HttpResponseException)
		e.statusCode == HTTP_GATEWAY_TIMEOUT
		e.message == "Timed out connecting to $endpoint.url"
	}

}
