package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.httpclient.BetamaxHttpsSupport
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.*
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.SystemDefaultHttpClient
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK

@Issue('https://github.com/robfletcher/betamax/issues/34')
@Unroll
class HttpsSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule @AutoCleanup('ejectTape') ProxyRecorder recorder = new ProxyRecorder(tapeRoot: tapeRoot, sslSupport: true)
	@Shared @AutoCleanup('stop') SimpleServer httpsEndpoint = new SimpleSecureServer(5001)
	@Shared @AutoCleanup('stop') SimpleServer httpEndpoint = new SimpleServer()

	@Shared URI httpUri
	@Shared URI httpsUri

	HttpClient http

	void setupSpec() {
		httpEndpoint.start(HelloHandler)
		httpsEndpoint.start(HelloHandler)

		httpUri = httpEndpoint.url.toURI()
		httpsUri = httpsEndpoint.url.toURI()
	}

	void setup() {
		http = new SystemDefaultHttpClient()
		BetamaxHttpsSupport.configure(http)
	}

	@Betamax(tape = 'https spec')
	void 'proxy is selected for #scheme URIs'() {
		given:
		def proxySelector = ProxySelector.default

		expect:
		def proxy = proxySelector.select(uri).first()
		proxy.type() == Proxy.Type.HTTP

		and:
		def proxyURI = "${scheme}://${proxy.address()}".toURI()
		InetAddress.getByName(proxyURI.host) == InetAddress.getByName(recorder.proxyHost)
		proxyURI.port == recorder.proxyPort

		where:
		uri << [httpUri, httpsUri]
		scheme = uri.scheme
	}

	@Betamax(tape = 'https spec', mode = TapeMode.WRITE_ONLY)
	void 'proxy can intercept #scheme requests'() {
		when: 'a request is made'
		def request = new HttpGet(url)
		def response = http.execute(request)

		then: 'it is intercepted by the proxy'
		response.statusLine.statusCode == SC_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'

		and: 'the response body is readable'
		response.entity.content.text == HELLO_WORLD

		where:
		url << [httpEndpoint.url, httpsEndpoint.url]
		scheme = url.toURI().scheme
	}

}


