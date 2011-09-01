package betamax

import betamax.server.HttpProxyServer
import betamax.util.EchoServer
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.yaml.snakeyaml.Yaml
import static java.net.HttpURLConnection.HTTP_OK
import spock.lang.*

@Stepwise
class ProxyRecordAndPlaybackSpec extends Specification {

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Shared @AutoCleanup("ejectTape") Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup("stop") HttpProxyServer proxy = new HttpProxyServer()
	@AutoCleanup("stop") EchoServer endpoint = new EchoServer()
	RESTClient http

	def setupSpec() {
		recorder.insertTape("proxy_record_and_playback_spec")

		proxy.start(recorder)
	}

	def setup() {
		http = new RESTClient(endpoint.url)
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
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
		yaml.name == "proxy_record_and_playback_spec"
		yaml.interactions.size() == 2
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
    protocol: HTTP/1.1
    method: GET
    uri: http://icanhascheezburger.com/
    headers: {Accept-Language: 'en-GB,en', If-None-Match: b00b135}
  response:
    protocol: HTTP/1.1
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB}
    body: O HAI!
"""

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
