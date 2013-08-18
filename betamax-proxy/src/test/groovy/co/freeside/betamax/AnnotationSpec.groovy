package co.freeside.betamax

import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.RESTClient
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Stepwise
class AnnotationSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot)
	@AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	RESTClient http

	void setup() {
		http = new BetamaxRESTClient(endpoint.url)
	}

	void 'no tape is inserted if there is no annotation on the feature'() {
		expect:
		recorder.tape == null
	}

	@Betamax(tape = 'annotation_spec')
	void 'annotation on feature causes tape to be inserted'() {
		expect:
		recorder.tape.name == 'annotation_spec'
	}

	void 'tape is ejected after annotated feature completes'() {
		expect:
		recorder.tape == null
	}

	@Betamax(tape = 'annotation_spec', mode = TapeMode.READ_WRITE)
	void 'annotated feature can record'() {
		given:
		endpoint.start(EchoHandler)

		when:
		def response = http.get(path: '/')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
		response.getFirstHeader(X_BETAMAX)?.value == 'REC'
	}

	@Betamax(tape = 'annotation_spec')
	void 'annotated feature can play back'() {
		when:
		def response = http.get(path: '/')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
		response.getFirstHeader(X_BETAMAX)?.value == 'PLAY'
	}

	void 'can make unproxied request after using annotation'() {
		given:
		endpoint.start(EchoHandler)

		when:
		def response = http.get(path: '/')

		then:
		response.status == HTTP_OK
		response.getFirstHeader(VIA) == null
	}

}
