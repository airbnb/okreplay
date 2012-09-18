package co.freeside.betamax.proxy

import co.freeside.betamax.Recorder
import co.freeside.betamax.proxy.jetty.ProxyServer
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.yaml.snakeyaml.Yaml
import spock.lang.*

import static java.net.HttpURLConnection.HTTP_OK

@Stepwise
class ProxyRecordAndPlaybackSpec extends Specification {

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Shared @AutoCleanup("ejectTape") Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup("stop") ProxyServer proxy = new ProxyServer()
	@AutoCleanup("stop") SimpleServer endpoint = new SimpleServer()
	RESTClient http

	def setupSpec() {
		recorder.insertTape("proxy record and playback spec")
		proxy.start(recorder)
		recorder.overrideProxySettings()
	}

	def setup() {
		http = new RESTClient(endpoint.url)
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	def cleanupSpec() {
		recorder.restoreOriginalProxySettings()
	}

	@Timeout(10)
	def "proxy makes a real HTTP request the first time it gets a request for a URI"() {
		given:
		endpoint.start(EchoHandler)

		when:
		http.get(path: "/")

		then:
		recorder.tape.size() == 1
	}

	@Timeout(10)
	def "subsequent requests for the same URI are played back from tape"() {
		when:
		http.get(path: "/")

		then:
		recorder.tape.size() == 1
	}

	@Timeout(10)
	def "subsequent requests with a different HTTP method are recorded separately"() {
		given:
		endpoint.start(EchoHandler)

		when:
		http.head(path: "/")

		then:
		recorder.tape.size() == old(recorder.tape.size()) + 1
		recorder.tape.interactions[-1].request.method == "HEAD"
	}

	def "when the tape is ejected the data is written to a file"() {
		given:
		proxy.stop()

		when:
		recorder.ejectTape()

		then:
		def file = new File(recorder.tapeRoot, "proxy_record_and_playback_spec.yaml")
		file.isFile()

		and:
		def yaml = file.withReader { reader ->
			new Yaml().loadAs(reader, Map)
		}
		yaml.name == "proxy record and playback spec"
		yaml.size() == 2
	}

	def "can load an existing tape from a file"() {
		given:
		def file = new File(recorder.tapeRoot, "existing_tape.yaml")
		file.parentFile.mkdirs()
		file.text = """\
!tape
name: existing_tape
interactions:
- recorded: 2011-08-19T11:45:33.000Z
  request:
    method: GET
    uri: http://icanhascheezburger.com/
    headers: {Accept-Language: 'en-GB,en', If-None-Match: b00b135}
  response:
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB}
    body: O HAI!
"""

		when:
		recorder.insertTape("existing_tape")
		proxy.start(recorder)

		then:
		recorder.tape.name == "existing_tape"
		recorder.tape.size() == 1
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
