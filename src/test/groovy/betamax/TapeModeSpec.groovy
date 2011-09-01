package betamax

import betamax.server.HttpProxyServer
import betamax.util.EchoServer
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import static betamax.TapeMode.*
import groovyx.net.http.*
import static java.net.HttpURLConnection.*
import spock.lang.*

class TapeModeSpec extends Specification {

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Shared Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup("stop") HttpProxyServer proxy = HttpProxyServer.instance
	@Shared @AutoCleanup("stop") EchoServer endpoint = new EchoServer()
	RESTClient http

	def setupSpec() {
		tapeRoot.mkdirs()
		proxy.connect(recorder)
		endpoint.start()
	}

	def setup() {
		http = new RESTClient()
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	def cleanup() {
		recorder.ejectTape()
	}

	def "in read-only mode the proxy rejects a request if no recorded interaction exists"() {
		given: "a read-only tape is inserted"
		recorder.insertTape("read only tape", READ_ONLY)

		when: "a request is made that does not match anything recorded on the tape"
		http.get(uri: endpoint.url)

		then: "the proxy rejects the request"
		def e = thrown(HttpResponseException)
		e.statusCode == HTTP_FORBIDDEN
		e.message == "Tape is read-only"
	}

	def "in write-only mode the an interaction can be recorded"() {
		given: "an empty write-only tape is inserted"
		recorder.insertTape("blank write only tape", WRITE_ONLY)
		def tape = recorder.tape

		when: "a request is made"
		http.get(uri: endpoint.url)

		then: "the interaction is recorded"
		tape.size() == old(tape.size()) + 1
	}

	def "in write-only mode the proxy overwrites a recorded interaction"() {
		given: "an existing tape file is inserted in write-only mode"
		def tapeFile = new File(tapeRoot, "write_only_tape.yaml")
		tapeFile.text = """\
!tape
name: write only tape
interactions:
- recorded: 2011-08-26T21:46:52.000Z
  request:
    protocol: HTTP/1.1
    method: GET
    uri: $endpoint.url
    headers: {}
  response:
    protocol: HTTP/1.1
    status: 202
    headers: {}
    body: Previous response made when endpoint was down.
"""
		recorder.insertTape("write only tape", WRITE_ONLY)
		def tape = recorder.tape

		when: "a request is made that matches a request already recorded on the tape"
		http.get(uri: endpoint.url)

		then: "the previously recorded request is overwritten"
		tape.size() == old(tape.size())
		tape.interactions[-1].response.status == HTTP_OK
		tape.interactions[-1].response.body
	}

}
