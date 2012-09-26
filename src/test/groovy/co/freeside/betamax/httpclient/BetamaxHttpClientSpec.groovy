package co.freeside.betamax.httpclient

import co.freeside.betamax.*
import co.freeside.betamax.handler.HandlerException
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.*
import org.apache.http.client.methods.*
import org.apache.http.entity.StringEntity
import org.eclipse.jetty.server.Handler
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED

class BetamaxHttpClientSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = new File(System.properties.'java.io.tmpdir', 'tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot, proxy: Mock(HttpInterceptor))
	@AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	def http = new BetamaxHttpClient(recorder)

	@Betamax(tape = 'betamax http client')
	void 'can use Betamax without starting the proxy'() {
		given:
		endpoint.start(HelloHandler)

		and:
		def request = new HttpGet(endpoint.url)

		when:
		def response = http.execute(request)

		then:
		response.statusLine.statusCode == HTTP_OK
		response.entity.content.text == HELLO_WORLD

		and:
		response.getFirstHeader(VIA).value == 'Betamax'
		response.getFirstHeader('X-Betamax').value == 'REC'
	}

	@Betamax(tape = 'betamax http client')
	void 'can play back from tape'() {
		given:
		def handler = Mock(Handler)
		endpoint.start(handler)

		and:
		def request = new HttpGet(endpoint.url)

		when:
		def response = http.execute(request)

		then:
		response.statusLine.statusCode == HTTP_OK
		response.entity.content.text == HELLO_WORLD

		and:
		response.getFirstHeader(VIA).value == 'Betamax'
		response.getFirstHeader('X-Betamax').value == 'PLAY'

		and:
		0 * handler.handle(*_)
	}

	@Betamax(tape = 'betamax http client')
	void 'can send a request with a body'() {
		given:
		endpoint.start(EchoHandler)

		and:
		def request = new HttpPost(endpoint.url)
		request.entity = new StringEntity('message=O HAI', APPLICATION_FORM_URLENCODED)

		when:
		def response = http.execute(request)

		then:
		response.statusLine.statusCode == HTTP_OK
		response.entity.content.text.endsWith 'message=O HAI'

		and:
		response.getFirstHeader(VIA).value == 'Betamax'
	}

	void 'fails in non-annotated spec'() {
		given:
		def handler = Mock(Handler)
		endpoint.start(handler)

		when:
		http.execute(new HttpGet(endpoint.url))

		then:
		def e = thrown(HandlerException)
		e.message == 'No tape'

		and:
		0 * handler.handle(*_)
	}

	// TODO: support ignore hosts config

}
