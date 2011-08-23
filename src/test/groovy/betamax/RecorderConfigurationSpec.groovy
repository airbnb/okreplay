package betamax

import spock.lang.*

class RecorderConfigurationSpec extends Specification {

	@Shared String tmpdir = System.properties."java.io.tmpdir"

	def "recorder gets default configuration if not overridden and no properties file exists"() {
		given:
		def recorder = new Recorder()

		expect:
		recorder.tapeRoot == new File("src/test/resources/betamax/tapes")
		recorder.proxyPort == 5555
	}

	def "recorder configuration is overridden by map arguments"() {
		given:
		def recorder = new Recorder(tapeRoot: new File(tmpdir, "tapes"), proxyPort: 1337)

		expect:
		recorder.tapeRoot == new File(tmpdir, "tapes")
		recorder.proxyPort == 1337
	}

	def "recorder picks up configuration from properties"() {
		given:
		def properties = new Properties()
		properties.setProperty("betamax.tapeRoot", "$tmpdir/tapes")
		properties.setProperty("betamax.proxyPort", "1337")

		and:
		def recorder = new Recorder(properties)

		expect:
		recorder.tapeRoot == new File(tmpdir, "tapes")
		recorder.proxyPort == 1337
	}

	def "recorder picks up configuration from properties file"() {
		given:
		def propertiesFile = new File(tmpdir, "betamax.properties")
		def properties = new Properties()
		properties.setProperty("betamax.tapeRoot", "$tmpdir/tapes")
		properties.setProperty("betamax.proxyPort", "1337")
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

		cleanup:
		configFile.delete()
	}

	def "constructor arguments take precendence over a properties file"() {
		given:
		def propertiesFile = new File(tmpdir, "betamax.properties")
		def properties = new Properties()
		properties.setProperty("betamax.tapeRoot", "$tmpdir/tapes")
		properties.setProperty("betamax.proxyPort", "1337")
		propertiesFile.withWriter { writer ->
			properties.store(writer, null)
		}

		and:
		Recorder.classLoader.addURL(new File(tmpdir).toURL())

		and:
		def recorder = new Recorder(tapeRoot: new File("test/fixtures/tapes"), proxyPort: 1234)

		expect:
		recorder.tapeRoot == new File("test/fixtures/tapes")
		recorder.proxyPort == 1234

		cleanup:
		propertiesFile.delete()
	}

}
