package co.freeside.betamax.recorder

import co.freeside.betamax.Recorder
import co.freeside.betamax.proxy.jetty.ProxyServer
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static co.freeside.betamax.TapeMode.READ_ONLY
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static java.net.HttpURLConnection.HTTP_FORBIDDEN
import static java.net.HttpURLConnection.HTTP_OK

class TapeModeSpec extends Specification {

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Shared Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup("stop") ProxyServer proxy = new ProxyServer()
	@Shared @AutoCleanup("stop") SimpleServer endpoint = new SimpleServer()
	RESTClient http

	def setupSpec() {
		tapeRoot.mkdirs()
		proxy.start(recorder)
		recorder.overrideProxySettings()
		endpoint.start(EchoHandler)
	}

	def setup() {
		http = new RESTClient()
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	def cleanup() {
		recorder.ejectTape()
	}

	def cleanupSpec() {
		recorder.restoreOriginalProxySettings()
	}

	def "in read-only mode the proxy rejects a request if no recorded interaction exists"() {
		given: "a read-only tape is inserted"
		recorder.insertTape("read only tape", [mode: READ_ONLY])

		when: "a request is made that does not match anything recorded on the tape"
		http.get(uri: endpoint.url)

		then: "the proxy rejects the request"
		def e = thrown(HttpResponseException)
		e.statusCode == HTTP_FORBIDDEN
		e.message == "Tape is read-only"
	}

	def "in write-only mode the an interaction can be recorded"() {
		given: "an empty write-only tape is inserted"
		recorder.insertTape("blank write only tape", [mode: WRITE_ONLY])
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
    method: GET
    uri: $endpoint.url
    headers: {}
  response:
    status: 202
    headers: {}
    body: Previous response made when endpoint was down.
"""
		recorder.insertTape("write only tape", [mode: WRITE_ONLY])
		def tape = recorder.tape

		when: "a request is made that matches a request already recorded on the tape"
		http.get(uri: endpoint.url)

		then: "the previously recorded request is overwritten"
		tape.size() == old(tape.size())
		tape.interactions[-1].response.status == HTTP_OK
		tape.interactions[-1].response.body
	}

}
