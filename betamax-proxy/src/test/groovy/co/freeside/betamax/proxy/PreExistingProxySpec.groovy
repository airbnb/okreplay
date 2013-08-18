package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.HelloHandler
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Issue('https://github.com/robfletcher/betamax/issues/54')
class PreExistingProxySpec extends Specification {

	@AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup('stop') SimpleServer proxyServer = new SimpleServer()

	void setupSpec() {
		println 'setupSpec'
		proxyServer.start(HelloHandler)
		System.properties.'http.proxyHost' = InetAddress.localHost.hostAddress
		System.properties.'http.proxyPort' = proxyServer.port.toString()
	}

	void cleanupSpec() {
		println 'cleanupSpec'
		System.clearProperty 'http.proxyHost'
		System.clearProperty 'http.proxyPort'
	}

	@Timeout(10)
	@Betamax(tape = 'existing proxy spec', mode = TapeMode.READ_WRITE)
	void 'pre-existing proxy settings are used for the outbound request from the Betamax proxy'() {
		given:
		HttpURLConnection connection = new URL('http://freeside.co/betamax').openConnection()
		connection.connect()

		expect:
		connection.responseCode == HTTP_OK
		connection.getHeaderField(VIA) == 'Betamax'
		connection.inputStream.text == HELLO_WORLD

		cleanup:
		connection.disconnect()
	}

}
