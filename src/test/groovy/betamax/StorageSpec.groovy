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
class StorageSpec extends Specification {

    @Shared Tape tape = new Tape()
    HttpRequest getRequest = new HttpGet("http://icanhascheezburger.com/")
	HttpResponse emptyResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK")
    HttpResponse plainTextResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK")

    def setup() {
        plainTextResponse.addHeader(CONTENT_TYPE, "text/plain")
        plainTextResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
        plainTextResponse.addHeader(CONTENT_ENCODING, "gzip")
        plainTextResponse.entity = new BasicHttpEntity()
        plainTextResponse.entity.content = new ByteArrayInputStream("O HAI!".bytes)
        plainTextResponse.entity.contentLength = 6L
    }

	def "reading from an empty tape does nothing"() {
		expect:
		!tape.read(getRequest, emptyResponse)
	}

    def "can write an HTTP interaction to a tape"() {
        when:
        tape.write(getRequest, plainTextResponse)

        then:
        tape.programmes.size() == 1

        and:
        def programme = tape.programmes.iterator().next()
        programme.request.method == "GET"
        programme.request.uri == "http://icanhascheezburger.com/"
		programme.response.protocol == "HTTP/1.1"
        programme.response.status == 200
        programme.response.body == "O HAI!"
        programme.response.headers[CONTENT_TYPE] == "text/plain"
        programme.response.headers[CONTENT_LANGUAGE] == "en-GB"
        programme.response.headers[CONTENT_ENCODING] == "gzip"
    }

    def "can read a stored HTTP interaction"() {
        expect:
        tape.read(getRequest, emptyResponse)

        and:
		emptyResponse.statusLine.protocolVersion.toString() == "HTTP/1.1"
        emptyResponse.statusLine.statusCode == 200
		emptyResponse.entity.content.text == "O HAI!"
		emptyResponse.getHeaders(CONTENT_TYPE).value == ["text/plain"]
		emptyResponse.getHeaders(CONTENT_LANGUAGE).value == ["en-GB"]
		emptyResponse.getHeaders(CONTENT_ENCODING).value == ["gzip"]
    }

	def "read does not match a request for a different URI"() {
		given:
		def request = new HttpGet("http://qwantz.com/")

		expect:
		!tape.read(request, emptyResponse)
	}

}
