package co.freeside.betamax.recorder

import co.freeside.betamax.*
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory
import org.apache.http.conn.ssl.SSLSocketFactory
import spock.lang.*
import static ProxyRecorder.DEFAULT_PROXY_TIMEOUT
import static co.freeside.betamax.Recorder.DEFAULT_TAPE_ROOT
import static co.freeside.betamax.TapeMode.*

class RecorderConfigurationSpec extends Specification {

	@Shared String tmpdir = System.properties.'java.io.tmpdir'

	void 'recorder gets default configuration if not overridden and no properties file exists'() {
		given:
		def recorder = new ProxyRecorder()

		expect:
		recorder.tapeRoot == new File(DEFAULT_TAPE_ROOT)
		recorder.proxyPort == 5555
		recorder.defaultMode == READ_WRITE
		recorder.proxyTimeout == DEFAULT_PROXY_TIMEOUT
		recorder.ignoreHosts == []
		!recorder.ignoreLocalhost
		!recorder.sslSupport
		recorder.sslSocketFactory instanceof DummySSLSocketFactory
	}

	void 'recorder configuration is overridden by map arguments'() {
		given:
		def recorder = new ProxyRecorder(
				tapeRoot: new File(tmpdir, 'tapes'),
				proxyPort: 1337,
				defaultMode: READ_ONLY,
				proxyTimeout: 30000,
				ignoreHosts: ['localhost'],
				ignoreLocalhost: true,
				sslSupport: true,
				sslSocketFactory: new SSLSocketFactory(null)
		)

		expect:
		recorder.tapeRoot == new File(tmpdir, 'tapes')
		recorder.proxyPort == 1337
		recorder.defaultMode == READ_ONLY
		recorder.proxyTimeout == 30000
		recorder.ignoreHosts == ['localhost']
		recorder.ignoreLocalhost
		recorder.sslSupport
		recorder.sslSocketFactory instanceof SSLSocketFactory
	}

	void 'recorder picks up configuration from properties'() {
		given:
		def properties = new Properties()
		properties.setProperty('betamax.tapeRoot', "$tmpdir/tapes")
		properties.setProperty('betamax.proxyPort', '1337')
		properties.setProperty('betamax.defaultMode', 'READ_ONLY')
		properties.setProperty('betamax.proxyTimeout', '30000')
		properties.setProperty('betamax.ignoreHosts', 'localhost,127.0.0.1')
		properties.setProperty('betamax.ignoreLocalhost', 'true')
		properties.setProperty('betamax.sslSupport', 'true')

		and:
		def recorder = new ProxyRecorder(properties)

		expect:
		recorder.tapeRoot == new File(tmpdir, 'tapes')
		recorder.proxyPort == 1337
		recorder.defaultMode == READ_ONLY
		recorder.proxyTimeout == 30000
		recorder.ignoreHosts == ['localhost', '127.0.0.1']
		recorder.ignoreLocalhost
		recorder.sslSupport
	}

	void 'recorder picks up configuration from properties file'() {
		given:
		def propertiesFile = new File(tmpdir, 'betamax.properties')
		def properties = new Properties()
		properties.setProperty('betamax.tapeRoot', "$tmpdir/tapes")
		properties.setProperty('betamax.proxyPort', '1337')
		properties.setProperty('betamax.defaultMode', 'READ_ONLY')
		properties.setProperty('betamax.proxyTimeout', '30000')
		properties.setProperty('betamax.ignoreHosts', 'localhost,127.0.0.1')
		properties.setProperty('betamax.ignoreLocalhost', 'true')
		properties.setProperty('betamax.sslSupport', 'true')
		propertiesFile.withWriter { writer ->
			properties.store(writer, null)
		}

		and:
		Recorder.classLoader.addURL(new File(tmpdir).toURL())

		and:
		def recorder = new ProxyRecorder()

		expect:
		recorder.tapeRoot == new File(tmpdir, 'tapes')
		recorder.proxyPort == 1337
		recorder.defaultMode == READ_ONLY
		recorder.proxyTimeout == 30000
		recorder.ignoreHosts == ['localhost', '127.0.0.1']
		recorder.ignoreLocalhost
		recorder.sslSupport

		cleanup:
		propertiesFile.delete()
	}

	void 'constructor arguments take precendence over a properties file'() {
		given:
		def propertiesFile = new File(tmpdir, 'betamax.properties')
		def properties = new Properties()
		properties.setProperty('betamax.tapeRoot', "$tmpdir/tapes")
		properties.setProperty('betamax.proxyPort', '1337')
		properties.setProperty('betamax.defaultMode', 'READ_ONLY')
		properties.setProperty('betamax.proxyTimeout', '30000')
		properties.setProperty('betamax.ignoreHosts', 'localhost,127.0.0.1')
		properties.setProperty('betamax.ignoreLocalhost', 'true')
		properties.setProperty('betamax.sslSupport', 'true')
		propertiesFile.withWriter { writer ->
			properties.store(writer, null)
		}

		and:
		Recorder.classLoader.addURL(new File(tmpdir).toURL())

		and:
		def recorder = new ProxyRecorder(
				tapeRoot: new File('test/fixtures/tapes'),
				proxyPort: 1234,
				defaultMode: WRITE_ONLY,
				proxyTimeout: 10000,
				ignoreHosts: ['github.com'],
				ignoreLocalhost: false,
				sslSupport: false
		)

		expect:
		recorder.tapeRoot == new File('test/fixtures/tapes')
		recorder.proxyPort == 1234
		recorder.defaultMode == WRITE_ONLY
		recorder.proxyTimeout == 10000
		recorder.ignoreHosts == ['github.com']
		!recorder.ignoreLocalhost
		!recorder.sslSupport

		cleanup:
		propertiesFile.delete()
	}
}
