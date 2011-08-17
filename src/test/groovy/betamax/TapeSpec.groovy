package betamax

import betamax.storage.Tape
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.message.BasicHttpResponse
import spock.lang.Specification
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import spock.lang.Stepwise
import spock.lang.Shared

@Stepwise
class TapeSpec extends Specification {

	private static final ProtocolVersion HTTP_1_1 = new ProtocolVersion("HTTP", 1, 1)
	
	@Shared Tape tape = new Tape()
    HttpRequest getRequest = new HttpGet("http://icanhascheezburger.com/")
    HttpResponse plainTextResponse = new BasicHttpResponse(HTTP_1_1, 200, "OK")

    def setup() {
        plainTextResponse.addHeader(CONTENT_TYPE, "text/plain")
        plainTextResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
        plainTextResponse.addHeader(CONTENT_ENCODING, "gzip")
        plainTextResponse.entity = new BasicHttpEntity()
        plainTextResponse.entity.content = new ByteArrayInputStream("O HAI!".bytes)
        plainTextResponse.entity.contentLength = 6L
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
		def interaction = tape.interactions.iterator().next()

		and:
		interaction.request.requestLine.method == getRequest.requestLine.method
        interaction.request.requestLine.uri == getRequest.requestLine.uri
		interaction.request.requestLine.protocolVersion == getRequest.requestLine.protocolVersion

		and:
		interaction.response.statusLine.protocolVersion == plainTextResponse.statusLine.protocolVersion
        interaction.response.statusLine.statusCode == plainTextResponse.statusLine.statusCode
        interaction.response.entity.content.text == "O HAI!"
        interaction.response.getHeaders(CONTENT_TYPE).value == plainTextResponse.getHeaders(CONTENT_TYPE).value
        interaction.response.getHeaders(CONTENT_LANGUAGE).value == plainTextResponse.getHeaders(CONTENT_LANGUAGE).value
        interaction.response.getHeaders(CONTENT_ENCODING).value == plainTextResponse.getHeaders(CONTENT_ENCODING).value

		and:
		!interaction.request.is(getRequest)
		!interaction.response.is(plainTextResponse)
    }

    def "can read a stored HTTP interaction"() {
		given:
		def response = new BasicHttpResponse(HTTP_1_1, 200, "OK")

        expect:
        tape.play(getRequest, response)

        and:
		response.statusLine.protocolVersion == plainTextResponse.statusLine.protocolVersion
        response.statusLine.statusCode == plainTextResponse.statusLine.statusCode
		response.entity.content.text == "O HAI!"
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

}
