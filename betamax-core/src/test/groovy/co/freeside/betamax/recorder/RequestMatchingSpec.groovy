package co.freeside.betamax.recorder

import co.freeside.betamax.Headers
import co.freeside.betamax.Recorder
import co.freeside.betamax.handler.*
import co.freeside.betamax.util.message.BasicRequest
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.MatchRule.host
import static co.freeside.betamax.util.FileUtils.newTempDir
import static org.apache.http.HttpHeaders.VIA

@Issue('https://github.com/robfletcher/betamax/issues/9')
class RequestMatchingSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@AutoCleanup('stop') Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	HttpHandler handler = new DefaultHandlerChain(recorder)

	void setupSpec() {
		tapeRoot.mkdirs()
	}

	@Unroll('#method request for #uri returns "#responseText"')
	void 'default match is method and uri'() {
		given:
		new File(tapeRoot, 'method_and_uri_tape.yaml').text = '''\
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
    body: GET method response from xkcd.com
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: POST
    uri: http://xkcd.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: POST method response from xkcd.com
- recorded: 2011-08-23T20:24:33.000Z
  request:
    method: GET
    uri: http://qwantz.com/
  response:
    status: 200
    headers: {Content-Type: text/plain}
    body: GET method response from qwantz.com
'''

		and:
		recorder.start('method and uri tape')

		when:
		def response = handler.handle(request)

		then:
		response.bodyAsText.text == responseText

		where:
		method | uri
		'GET'  | 'http://xkcd.com/'
		'POST' | 'http://xkcd.com/'
		'GET'  | 'http://qwantz.com/'

		responseText = "$method method response from ${uri.toURI().host}"
		request = new BasicRequest(method, uri)
	}

	void 'can match based on host'() {
		given:
		new File(tapeRoot, 'host_match_tape.yaml').text = '''\
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
    body: GET method response from xkcd.com
'''

		and:
		recorder.start('host match tape', [match: [host]])

		when:
		def request = new BasicRequest('GET', 'http://xkcd.com/875')
		def response = handler.handle(request)

		then:
		response.headers[VIA] == 'Betamax'
		response.headers[X_BETAMAX] == 'PLAY'
		response.bodyAsText.text == 'GET method response from xkcd.com'
	}
}
