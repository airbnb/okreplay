package betamax

import betamax.storage.Tape
import org.apache.http.message.BasicHttpResponse
import static groovyx.net.http.ContentType.URLENC
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1
import org.apache.http.client.methods.*
import org.apache.http.entity.*
import spock.lang.*
import betamax.encoding.GzipEncoder

@Stepwise
class TapeSpec extends Specification {

	@Shared File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Shared Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared Tape tape = new Tape(name: "tape_spec")
	HttpRequest getRequest = new HttpGet("http://icanhascheezburger.com/")
	HttpResponse plainTextResponse = new BasicHttpResponse(HTTP_1_1, 200, "OK")

	def setup() {
		plainTextResponse.addHeader(CONTENT_TYPE, "text/plain")
		plainTextResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
		plainTextResponse.addHeader(CONTENT_ENCODING, "gzip")
		plainTextResponse.entity = new BasicHttpEntity()
        def bytes = new GzipEncoder().encode("O HAI!")
        plainTextResponse.entity.content = new ByteArrayInputStream(bytes)
		plainTextResponse.entity.contentLength = bytes.length
	}

    def cleanupSpec() {
        assert tapeRoot.deleteDir()
    }

	def "reading from an empty tape does nothing"() {
		given:
		def response = new BasicHttpResponse(HTTP_1_1, 200, "OK")

		expect:
		!tape.play(getRequest, response)
	}

	def "can write an HTTP interaction to a tape"() {
		when:
		tape.record(getRequest, plainTextResponse)

		then:
		tape.interactions.size() == 1
		def interaction = tape.interactions[-1]

		and:
		interaction.request.method == getRequest.requestLine.method
		interaction.request.uri == getRequest.requestLine.uri
		interaction.request.protocol == getRequest.requestLine.protocolVersion.toString()

		and:
		interaction.response.protocol == plainTextResponse.statusLine.protocolVersion.toString()
		interaction.response.status == plainTextResponse.statusLine.statusCode
		interaction.response.body == "O HAI!"
		interaction.response.headers[CONTENT_TYPE] == plainTextResponse.getFirstHeader(CONTENT_TYPE).value
		interaction.response.headers[CONTENT_LANGUAGE] == plainTextResponse.getFirstHeader(CONTENT_LANGUAGE).value
		interaction.response.headers[CONTENT_ENCODING] == plainTextResponse.getFirstHeader(CONTENT_ENCODING).value
	}

	def "can read a stored HTTP interaction"() {
		given:
		def response = new BasicHttpResponse(HTTP_1_1, 200, "OK")

		expect:
		tape.play(getRequest, response)

		and:
		response.statusLine.protocolVersion == plainTextResponse.statusLine.protocolVersion
		response.statusLine.statusCode == plainTextResponse.statusLine.statusCode
		new GzipEncoder().decode(response.entity.content) == "O HAI!"
		response.getHeaders(CONTENT_TYPE).value == plainTextResponse.getHeaders(CONTENT_TYPE).value
		response.getHeaders(CONTENT_LANGUAGE).value == plainTextResponse.getHeaders(CONTENT_LANGUAGE).value
		response.getHeaders(CONTENT_ENCODING).value == plainTextResponse.getHeaders(CONTENT_ENCODING).value
	}

	def "read does not match a request for a different URI"() {
		given:
		def request = new HttpGet("http://qwantz.com/")
		def response = new BasicHttpResponse(HTTP_1_1, 200, "OK")

		expect:
		!tape.play(request, response)
	}

	def "can record post requests with a body"() {
		given:
		def request = new HttpPost("http://github.com/")
		request.entity = new StringEntity("q=1", URLENC.toString(), "UTF-8")

		when:
		tape.record(request, plainTextResponse)

		then:
		def interaction = tape.interactions[-1]
		interaction.request.body == request.entity.content.text
	}
    
}
