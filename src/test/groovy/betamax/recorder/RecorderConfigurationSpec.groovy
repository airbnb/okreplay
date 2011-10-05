package betamax.recorder

import betamax.Recorder
import static betamax.Recorder.DEFAULT_PROXY_TIMEOUT
import static betamax.TapeMode.*
import spock.lang.*

class RecorderConfigurationSpec extends Specification {

	@Shared String tmpdir = System.properties."java.io.tmpdir"

	def "recorder gets default configuration if not overridden and no properties file exists"() {
		given:
		def recorder = new Recorder()

		expect:
		recorder.tapeRoot == new File("src/test/resources/betamax/tapes")
		recorder.proxyPort == 5555
		recorder.defaultMode == READ_WRITE
		recorder.proxyTimeout == DEFAULT_PROXY_TIMEOUT
		recorder.ignoreHosts == []
	}

	def "recorder configuration is overridden by map arguments"() {
		given:
		def recorder = new Recorder(tapeRoot: new File(tmpdir, "tapes"), proxyPort: 1337, defaultMode: READ_ONLY, proxyTimeout: 30000, ignoreHosts: ["localhost"])

		expect:
		recorder.tapeRoot == new File(tmpdir, "tapes")
		recorder.proxyPort == 1337
		recorder.defaultMode == READ_ONLY
		recorder.proxyTimeout == 30000
		recorder.ignoreHosts == ["localhost"]
	}

	def "recorder picks up configuration from properties"() {
		given:
		def properties = new Properties()
		properties.setProperty("betamax.tapeRoot", "$tmpdir/tapes")
		properties.setProperty("betamax.proxyPort", "1337")
		properties.setProperty("betamax.defaultMode", "READ_ONLY")
		properties.setProperty("betamax.proxyTimeout", "30000")
		properties.setProperty("betamax.ignoreHosts", "localhost,127.0.0.1")

		and:
		def recorder = new Recorder(properties)

		expect:
		recorder.tapeRoot == new File(tmpdir, "tapes")
		recorder.proxyPort == 1337
		recorder.defaultMode == READ_ONLY
		recorder.proxyTimeout == 30000
		recorder.ignoreHosts == ["localhost", "127.0.0.1"]
	}

	def "recorder picks up configuration from properties file"() {
		given:
		def propertiesFile = new File(tmpdir, "betamax.properties")
		def properties = new Properties()
		properties.setProperty("betamax.tapeRoot", "$tmpdir/tapes")
		properties.setProperty("betamax.proxyPort", "1337")
		properties.setProperty("betamax.defaultMode", "READ_ONLY")
		properties.setProperty("betamax.proxyTimeout", "30000")
		properties.setProperty("betamax.ignoreHosts", "localhost,127.0.0.1")
		propertiesFile.withWriter { writer ->
			properties.store(writer, null)
		}

		and:
		Recorder.classLoader.addURL(new File(tmpdir).toURL())

		and:
		def recorder = new Recorder()

		expect:
		recorder.tapeRoot == new File(tmpdir, "tapes")
		recorder.proxyPort == 1337
		recorder.defaultMode == READ_ONLY
		recorder.proxyTimeout == 30000
		recorder.ignoreHosts == ["localhost", "127.0.0.1"]

		cleanup:
		propertiesFile.delete()
	}

	def "recorder picks up configuration from groovy config script"() {
		given:
		def configFile = new File(tmpdir, "BetamaxConfig.groovy")
		configFile.withWriter { writer ->
			writer << """\
				betamax {
					tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
					proxyPort = 1337
					defaultMode = betamax.TapeMode.READ_ONLY
					proxyTimeout = 30000
					ignoreHosts = ["localhost", "127.0.0.1"]
				}
			"""
		}

		and:
		Recorder.classLoader.addURL(new File(tmpdir).toURL())

		and:
		def recorder = new Recorder()

		expect:
		recorder.tapeRoot == new File(tmpdir, "tapes")
		recorder.proxyPort == 1337
		recorder.defaultMode == READ_ONLY
		recorder.proxyTimeout == 30000
		recorder.ignoreHosts == ["localhost", "127.0.0.1"]

		cleanup:
		configFile.delete()
	}

	def "constructor arguments take precendence over a properties file"() {
		given:
		def propertiesFile = new File(tmpdir, "betamax.properties")
		def properties = new Properties()
		properties.setProperty("betamax.tapeRoot", "$tmpdir/tapes")
		properties.setProperty("betamax.proxyPort", "1337")
		properties.setProperty("betamax.defaultMode", "READ_ONLY")
		properties.setProperty("betamax.proxyTimeout", "30000")
		properties.setProperty("betamax.ignoreHosts", "localhost,127.0.0.1")
		propertiesFile.withWriter { writer ->
			properties.store(writer, null)
		}

		and:
		Recorder.classLoader.addURL(new File(tmpdir).toURL())

		and:
		def recorder = new Recorder(tapeRoot: new File("test/fixtures/tapes"), proxyPort: 1234, defaultMode: WRITE_ONLY, proxyTimeout: 10000, ignoreHosts: ["github.com"])

		expect:
		recorder.tapeRoot == new File("test/fixtures/tapes")
		recorder.proxyPort == 1234
		recorder.defaultMode == WRITE_ONLY
		recorder.proxyTimeout == 10000
		recorder.ignoreHosts == ["github.com"]

		cleanup:
		propertiesFile.delete()
	}

}
