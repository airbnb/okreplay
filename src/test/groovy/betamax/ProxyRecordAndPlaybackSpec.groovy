package betamax

import betamax.server.HttpProxyServer
import betamax.util.EchoServer
import groovyx.net.http.HttpURLClient
import spock.lang.*
import groovy.json.JsonSlurper

@Stepwise
class ProxyRecordAndPlaybackSpec extends Specification {

	@Shared Betamax betamax = Betamax.instance
	@Shared HttpProxyServer proxy = new HttpProxyServer()
	EchoServer endpoint = new EchoServer()

	def setupSpec() {
		System.properties."http.proxyHost" = "localhost"
		System.properties."http.proxyPort" = proxy.port.toString()

		Betamax.instance.tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
		Betamax.instance.insertTape("proxy_record_and_playback_spec")

		proxy.start()
	}

	def cleanupSpec() {
		proxy.stop()
		assert betamax.tapeRoot.deleteDir()
	}

	@Timeout(10)
	def "proxy makes processes a real HTTP request the first time it gets a request for a URI"() {
		given:
		endpoint.start()

		and:
		def http = new HttpURLClient(url: endpoint.url)

		when:
		http.request(path: "/")

		then:
		Betamax.instance.tape.interactions.size() == 1

		cleanup:
		endpoint.awaitStop()
	}

	@Timeout(10)
	def "subsequent requests for the same URI are played back from tape"() {
		given:
		def http = new HttpURLClient(url: endpoint.url)

		when:
		http.request(path: "/")

		then:
		Betamax.instance.tape.interactions.size() == 1
	}

	def "when the proxy is stopped the tape is written to a file"() {
		when:
		proxy.stop()

		then:
		def file = new File(betamax.tapeRoot, "${betamax.tape.name}.json")
		file.isFile()

		and:
		def json = file.withReader {
			reader -> new JsonSlurper().parse(reader)
		}
		json.tape.name == "proxy_record_and_playback_spec"
		json.tape.interactions.size() == 1
	}

	def "can load an existing tape from a file and play it back"() {
		given:
		def file = new File(betamax.tapeRoot, "existing_tape.json")
		file.parentFile.mkdirs()
		file.withWriter { writer ->
			writer << """\
{
	"tape": {
		"name": "existing_tape",
		"interactions": [
			{
				"recorded": "2011-08-19 12:45:33 +0100",
				"request": {
					"protocol": "HTTP/1.1",
					"method": "GET",
					"uri": "http://icanhascheezburger.com/"
				},
				"response": {
					"protocol": "HTTP/1.1",
					"status": 200,
					"body": "O HAI!"
				}
			}
		]
	}
}"""
		}

		when:
		betamax.insertTape("existing_tape")

		then:
		betamax.tape.name == "existing_tape"
		betamax.tape.interactions.size() == 1
	}

}
