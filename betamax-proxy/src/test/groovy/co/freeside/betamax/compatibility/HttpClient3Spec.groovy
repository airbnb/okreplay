package co.freeside.betamax.compatibility

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import org.apache.commons.httpclient.*
import org.apache.commons.httpclient.methods.GetMethod
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

class HttpClient3Spec extends Specification {

	@AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule ProxyRecorder recorder = new ProxyRecorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	void setupSpec() {
		endpoint.start(EchoHandler)
	}

	@Timeout(10)
	@Betamax(tape = 'http client 3 spec', mode = TapeMode.READ_WRITE)
	void 'proxy intercepts HTTPClient 3.x connections'() {
		given:
		def client = new HttpClient()
		client.hostConfiguration.proxyHost = new ProxyHost(recorder.proxyHost, recorder.proxyPort)

		and:
		def request = new GetMethod(endpoint.url)

		when:
		def status = client.executeMethod(request)

		then:
		status == HTTP_OK
		request.getResponseHeader(VIA)?.value == 'Betamax'
	}

}
