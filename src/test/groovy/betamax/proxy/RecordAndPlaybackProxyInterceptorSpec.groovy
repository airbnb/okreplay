package betamax.proxy

import org.apache.http.client.methods.HttpGet
import org.apache.http.message.BasicHttpResponse
import spock.lang.Specification
import betamax.*
import static java.net.HttpURLConnection.HTTP_FORBIDDEN
import org.apache.http.*
import static org.apache.http.HttpVersion.HTTP_1_1

class RecordAndPlaybackProxyInterceptorSpec extends Specification {

	Recorder recorder = Mock(Recorder)
	VetoingProxyInterceptor interceptor = new RecordAndPlaybackProxyInterceptor(recorder)
	HttpRequest request = new HttpGet("http://robfletcher.github.com/betamax")
	HttpResponse response = new BasicHttpResponse(HTTP_1_1, 200, "OK")

	def "does not veto a request when no matching recording is found on tape"() {
		given:
		def tape = Mock(Tape)
		tape.isReadable() >> true
		tape.isWritable() >> true
		tape.seek(_) >> false
		recorder.getTape() >> tape

		when:
		boolean veto = interceptor.interceptRequest(request, response)

		then:
		!veto

		and:
		0 * tape.play(_)
	}
	
	def "vetos a request when a matching recording is found on tape"() {
		given:
		def tape = Mock(Tape)
		tape.isReadable() >> true
		tape.seek(_) >> true
		recorder.getTape() >> tape

		when:
		boolean veto = interceptor.interceptRequest(request, response)

		then:
		veto

		and:
		1 * tape.play(response)
	}

	def "vetos a request and sets failing response code if no tape is inserted"() {
		given:
		recorder.getTape() >> null

		when:
		boolean veto = interceptor.interceptRequest(request, response)

		then:
		veto

		and:
		response.statusLine.statusCode == HTTP_FORBIDDEN
	}

	def "does not veto a request if the tape is not readable"() {
		given:
		def tape = Mock(Tape)
		tape.isReadable() >> false
		tape.isWritable() >> true
		recorder.getTape() >> tape

		when:
		boolean veto = interceptor.interceptRequest(request, response)

		then:
		!veto

		and: "the tape is positioned on an existing matching recording so it gets overwritten in WRITE_ONLY mode"
		1 * tape.seek(_) >> true
	}

	def "vetos a request and sets failing response code if the tape is not writable"() {
		given:
		def tape = Mock(Tape)
		tape.isReadable() >> true
		tape.seek(request) >> false
		tape.isWritable() >> false
		recorder.getTape() >> tape

		when:
		boolean veto = interceptor.interceptRequest(request, response)

		then:
		veto

		and:
		response.statusLine.statusCode == HTTP_FORBIDDEN
	}

	def "records response when intercepted"() {
		given:
		def tape = Mock(Tape)
		recorder.getTape() >> tape

		when:
		interceptor.interceptResponse(request, response)

		then:
		1 * tape.record(request, response)
	}

}
