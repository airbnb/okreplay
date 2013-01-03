package co.freeside.betamax.proxy

import java.security.KeyStore
import co.freeside.betamax.*
import co.freeside.betamax.httpclient.BetamaxHttpsSupport
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory
import co.freeside.betamax.util.server.*
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.client.SystemDefaultHttpClient
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK

@Issue('https://github.com/robfletcher/betamax/issues/72')
class CustomSecureSocketFactorySpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Shared @AutoCleanup('stop') SimpleServer httpsEndpoint = new SimpleSecureServer(5001)
	@Shared SSLSocketFactory sslSocketFactory

	@Rule @AutoCleanup('ejectTape') ProxyRecorder recorder = new ProxyRecorder(tapeRoot: tapeRoot, sslSupport: true, sslSocketFactory: sslSocketFactory)

	@Shared URI httpsUri

	HttpClient http

	void setupSpec() {
		httpsEndpoint.start(HelloHandler)

		httpsUri = httpsEndpoint.url.toURI()

		def trustStore = KeyStore.getInstance(KeyStore.defaultType)
		trustStore.load(null, null)

		sslSocketFactory = Spy(DummySSLSocketFactory, constructorArgs: [trustStore])
	}

	void setup() {
		http = new SystemDefaultHttpClient()
		BetamaxHttpsSupport.configure(http)
	}

	@Betamax(tape = 'custom secure socket factory spec', mode = TapeMode.WRITE_ONLY)
	void 'proxy can use a custom SSL socket factory'() {

		when: 'an HTTPS request is made'
		def request = new HttpGet(httpsEndpoint.url)
		def response = http.execute(request)

		and: 'it is intercepted by the proxy'
		assert response.statusLine.statusCode == SC_OK
		assert response.getFirstHeader(VIA)?.value == 'Betamax'

		and: 'the response body is readable'
		assert response.entity.content.text == HELLO_WORLD

		then: 'the custom ssl socket factory was used'
		(1.._) * sslSocketFactory._

		where:
		url << [httpsEndpoint.url]
		scheme = url.toURI().scheme

	}

}
