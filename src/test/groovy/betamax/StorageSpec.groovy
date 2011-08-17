package betamax

import betamax.storage.Tape
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.message.BasicHttpResponse
import spock.lang.Specification
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import spock.lang.Stepwise

@Stepwise
class StorageSpec extends Specification {

    Tape tape = new Tape()
    HttpRequest getRequest
    HttpResponse plainTextResponse

    def setup() {
        getRequest = new HttpGet("http://icanhascheezburger.com/")
        plainTextResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK")
        plainTextResponse.addHeader(CONTENT_TYPE, "text/plain")
        plainTextResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
        plainTextResponse.addHeader(CONTENT_ENCODING, "gzip")
        plainTextResponse.entity = new BasicHttpEntity()
        plainTextResponse.entity.content = new ByteArrayInputStream("O HAI!".bytes)
        plainTextResponse.entity.contentLength = 6L
    }

    def "can record an HTTP interaction to a tape"() {
        when:
        tape.record(getRequest, plainTextResponse)

        then:
        tape.programmes.size() == 1

        and:
        def programme = tape.programmes.iterator().next()
        programme.request.method == "GET"
        programme.request.uri == "http://icanhascheezburger.com/"
        programme.response.status == 200
        programme.response.body == "O HAI!"
        programme.response.headers[CONTENT_TYPE] == "text/plain"
        programme.response.headers[CONTENT_LANGUAGE] == "en-GB"
        programme.response.headers[CONTENT_ENCODING] == "gzip"
    }

    def "can retrieve a stored HTTP interaction"() {
        given:
        def response = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK")

        when:
        tape.play(getRequest, response)

        then:
        response.statusLine.statusCode == 200
    }

}
