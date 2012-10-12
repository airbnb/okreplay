package co.freeside.betamax.compatibility

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.HttpURLClient
import groovyx.net.http.RESTClient
import org.apache.http.HttpHost
import org.junit.Rule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY

class HttpBuilderSpec extends Specification {

	@AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	void setupSpec() {
		endpoint.start(EchoHandler)
	}

	@Timeout(10)
	@Betamax(tape = 'http builder spec')
	void 'proxy intercepts HTTPClient connections when using ProxySelectorRoutePlanner'() {
		given:
		def http = new RESTClient(endpoint.url)
		BetamaxRoutePlanner.configure(http.client)

		when:
		def response = http.get(path: '/')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
	}

	@Timeout(10)
	@Betamax(tape = 'http builder spec')
	void 'proxy intercepts HTTPClient connections when explicitly told to'() {
		given:
		def http = new RESTClient(endpoint.url)
		http.client.params.setParameter(DEFAULT_PROXY, new HttpHost('localhost', recorder.proxyPort, 'http'))

		when:
		def response = http.get(path: '/')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
	}

	@Timeout(10)
	@Betamax(tape = 'http builder spec')
	void 'proxy intercepts HttpURLClient connections'() {
		given:
		def http = new HttpURLClient(url: endpoint.url)

		when:
		def response = http.request(path: '/')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
	}

	@Timeout(10)
	@Betamax(tape = 'http builder spec')
	void 'proxy automatically intercepts connections when the underlying client is a SystemDefaultHttpClient'() {
		given:
		def http = new BetamaxRESTClient(endpoint.url)

		when:
		def response = http.get(path: '/')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
	}

}
