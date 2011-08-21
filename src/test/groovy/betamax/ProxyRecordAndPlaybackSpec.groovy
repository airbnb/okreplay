package betamax

import betamax.server.HttpProxyServer
import betamax.util.EchoServer
import groovy.json.JsonSlurper
import groovyx.net.http.HttpURLClient
import spock.lang.*

@Stepwise
class ProxyRecordAndPlaybackSpec extends Specification {

	@Shared Recorder recorder = Recorder.instance
	@Shared HttpProxyServer proxy = new HttpProxyServer()
	EchoServer endpoint = new EchoServer()

	def setupSpec() {
		System.properties."http.proxyHost" = "localhost"
		System.properties."http.proxyPort" = proxy.port.toString()

		Recorder.instance.tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
		Recorder.instance.insertTape("proxy_record_and_playback_spec")

		proxy.start()
	}

	def cleanupSpec() {
		proxy.stop()
		assert recorder.tapeRoot.deleteDir()
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
		Recorder.instance.tape.interactions.size() == 1

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
		Recorder.instance.tape.interactions.size() == 1
	}

	def "when the proxy is stopped the tape is written to a file"() {
		when:
		proxy.stop()

		then:
		def file = new File(recorder.tapeRoot, "${recorder.tape.name}.json")
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
		def file = new File(recorder.tapeRoot, "existing_tape.json")
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
		recorder.insertTape("existing_tape")

		then:
		recorder.tape.name == "existing_tape"
		recorder.tape.interactions.size() == 1
	}

}
