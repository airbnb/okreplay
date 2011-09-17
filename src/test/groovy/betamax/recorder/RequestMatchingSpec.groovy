package betamax.recorder

import betamax.Recorder
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import static betamax.MatchRule.host
import static org.apache.http.HttpHeaders.VIA
import spock.lang.*

@Issue("https://github.com/robfletcher/betamax/issues/9")
class RequestMatchingSpec extends Specification {

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Shared Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared RESTClient http = new RESTClient()

	def setupSpec() {
		tapeRoot.mkdirs()

		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	@Unroll("#method request for #uri returns '#responseText'")
	def "default match is method and uri"() {
		given:
		new File(tapeRoot, "method_and_uri_tape.yaml").text = """\
!tape
name: method and uri tape
interactions:
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    uri: http://xkcd.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: get method response from xkcd.com
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: POST
    uri: http://xkcd.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: post method response from xkcd.com
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    uri: http://qwantz.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: get method response from qwantz.com
"""
		when:
		def response = recorder.withTape("method and uri tape") {
			http."$method"(uri: uri)
		}

		then:
		response.data.text == responseText

		where:
		method | uri                  | responseText
		"get"  | "http://xkcd.com/"   | "get method response from xkcd.com"
		"post" | "http://xkcd.com/"   | "post method response from xkcd.com"
		"get"  | "http://qwantz.com/" | "get method response from qwantz.com"
	}

	def "can match based on host"() {
		given:
		new File(tapeRoot, "host_match_tape.yaml").text = """\
!tape
name: host match tape
interactions:
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    uri: http://xkcd.com/936/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: get method response from xkcd.com
"""
		when:
		def response = recorder.withTape("host match tape", [match: [host]]) {
			http.get(uri: "http://xkcd.com/875/")
		}

		then:
		response.getFirstHeader(VIA)?.value == "Betamax"
		response.data.text == "get method response from xkcd.com"
	}
}
