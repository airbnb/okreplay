package co.freeside.betamax

import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Issue('https://github.com/robfletcher/betamax/issues/62')
@Issue('http://bugs.sun.com/view_bug.do?bug_id=6737819')
@Unroll
class LocalhostSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = new File(System.properties.'java.io.tmpdir', 'tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)

	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	@Shared RESTClient http = new RESTClient()

	void setupSpec() {
		BetamaxRoutePlanner.configure(http.client)
		endpoint.start(EchoHandler)
	}

	@Betamax(tape = 'localhost', mode = WRITE_ONLY)
	void 'can proxy requests to local endpoint at #uri'() {
		when:
		HttpResponseDecorator response = http.get(uri: uri)

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'

		where:
		uri << [endpoint.url, "http://localhost:$endpoint.port/", "http://127.0.0.1:$endpoint.port/"]
	}

}
