package betamax

import betamax.server.HttpProxyServer
import betamax.util.EchoServer
import groovy.json.JsonSlurper
import groovyx.net.http.HttpURLClient
import static java.net.HttpURLConnection.HTTP_OK
import spock.lang.*
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner

@Stepwise
class ProxyRecordAndPlaybackSpec extends Specification {

	@Shared Recorder recorder = Recorder.instance
	@Shared HttpProxyServer proxy = new HttpProxyServer()
	@AutoCleanup("stop") EchoServer endpoint = new EchoServer()
	RESTClient http

	def setupSpec() {
		recorder.tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
		recorder.insertTape("proxy_record_and_playback_spec")

		proxy.start(recorder)
	}

	def setup() {
		http = new RESTClient(endpoint.url)
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	def cleanupSpec() {
		proxy.stop()
        recorder.ejectTape()
		assert recorder.tapeRoot.deleteDir()
	}

	@Timeout(10)
	def "proxy makes processes a real HTTP request the first time it gets a request for a URI"() {
		given:
		endpoint.start()

		when:
		http.get(path: "/")

		then:
		recorder.tape.interactions.size() == 1
	}

	@Timeout(10)
	def "subsequent requests for the same URI are played back from tape"() {
		when:
		http.get(path: "/")

		then:
		recorder.tape.interactions.size() == 1
	}

	@Timeout(10)
	def "subsequent requests with a different HTTP method are recorded separately"() {
		given:
		endpoint.start()

		when:
		http.head(path: "/")

		then:
		recorder.tape.interactions.size() == old(recorder.tape.interactions.size()) + 1
		recorder.tape.interactions[-1].request.requestLine.method == "HEAD"
	}

	def "when the tape is ejected the data is written to a file"() {
		given:
		proxy.stop()

		when:
		def tape = recorder.ejectTape()

		then:
		def file = new File(recorder.tapeRoot, "${tape.name}.json")
		file.isFile()

		and:
		def json = file.withReader {
			reader -> new JsonSlurper().parse(reader)
		}
		json.tape.name == "proxy_record_and_playback_spec"
		json.tape.interactions.size() == 2
	}

	def "can load an existing tape from a file"() {
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
		proxy.start(recorder)

		then:
		recorder.tape.name == "existing_tape"
		recorder.tape.interactions.size() == 1
	}

	@Timeout(10)
	def "can play back a loaded tape"() {
		when:
		def response = http.get(uri: "http://icanhascheezburger.com/")

		then:
		response.statusLine.statusCode == HTTP_OK
		response.data.text == "O HAI!"
	}

}
