package co.freeside.betamax.compatibility

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.ProxyHost
import org.apache.commons.httpclient.methods.GetMethod
import org.junit.Rule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

class HttpClient3Spec extends Specification {

	@AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	void setupSpec() {
		endpoint.start(EchoHandler)
	}

	@Timeout(10)
	@Betamax(tape = 'http client 3 spec')
	void 'proxy intercepts HTTPClient 3.x connections'() {
		given:
		def client = new HttpClient()
		client.hostConfiguration.proxyHost = new ProxyHost('localhost', 5555)

		and:
		def request = new GetMethod(endpoint.url)

		when:
		def status = client.executeMethod(request)

		then:
		status == HTTP_OK
		request.getResponseHeader(VIA)?.value == 'Betamax'
	}

}
