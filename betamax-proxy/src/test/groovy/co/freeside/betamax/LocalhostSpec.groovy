package co.freeside.betamax

import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.*
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Issue('https://github.com/robfletcher/betamax/issues/62')
@Issue('http://bugs.sun.com/view_bug.do?bug_id=6737819')
@Unroll
class LocalhostSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot)

	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	@Shared RESTClient http = new BetamaxRESTClient()

	void setupSpec() {
		endpoint.start(EchoHandler)
	}

	@IgnoreIf({ println javaVersion; javaVersion >= 1.6 && javaVersion < 1.7 })
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
