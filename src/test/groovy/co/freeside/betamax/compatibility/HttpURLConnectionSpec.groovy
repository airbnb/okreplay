package co.freeside.betamax.compatibility

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import co.freeside.betamax.util.server.HelloHandler
import co.freeside.betamax.util.server.SimpleSecureServer
import org.junit.Rule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK

class HttpURLConnectionSpec extends Specification {

	@AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot, defaultMode: WRITE_ONLY, sslSupport: true)
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	@Shared @AutoCleanup('stop') SimpleServer httpsEndpoint = new SimpleSecureServer(5001)

	void setupSpec() {
		endpoint.start(EchoHandler)
		httpsEndpoint.start(HelloHandler)
	}

	@Timeout(10)
	@Betamax(tape = 'http url connection spec')
	void 'proxy intercepts URL connections'() {
		given:
		HttpURLConnection connection = new URL(endpoint.url).openConnection()
		connection.connect()

		expect:
		connection.responseCode == HTTP_OK
		connection.getHeaderField(VIA) == 'Betamax'

		cleanup:
		connection.disconnect()
	}

	@Betamax(tape = 'http url connection spec', mode = WRITE_ONLY)
	void 'proxy intercepts HTTPS requests'() {
		when:
		HttpURLConnection connection = httpsEndpoint.url.toURL().openConnection()

		then:
		connection.responseCode == SC_OK
		connection.getHeaderField(VIA) == 'Betamax'
		connection.inputStream.text == 'Hello World!'
	}
}
