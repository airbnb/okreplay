package co.freeside.betamax.compatibility

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import org.junit.Rule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

class HttpURLConnectionSpec extends Specification {

	@AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	void setupSpec() {
		endpoint.start(EchoHandler)
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

}
