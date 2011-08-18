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
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import groovyx.net.http.ContentType
import static groovyx.net.http.ContentType.URLENC
import groovy.json.JsonSlurper

@Stepwise
class TapeSpec extends Specification {

	private static final ProtocolVersion HTTP_1_1 = new ProtocolVersion("HTTP", 1, 1)

	@Shared Tape tape = new Tape(name: "tape_spec")
	HttpRequest getRequest = new HttpGet("http://icanhascheezburger.com/")
	HttpResponse plainTextResponse = new BasicHttpResponse(HTTP_1_1, 200, "OK")

    def setupSpec() {
        Betamax.instance.tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
        Betamax.instance.tapeRoot.mkdirs()
    }

	def setup() {
		plainTextResponse.addHeader(CONTENT_TYPE, "text/plain")
		plainTextResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
		plainTextResponse.addHeader(CONTENT_ENCODING, "gzip")
		plainTextResponse.entity = new BasicHttpEntity()
		plainTextResponse.entity.content = new ByteArrayInputStream("O HAI!".bytes)
		plainTextResponse.entity.contentLength = 6L
	}

    def cleanupSpec() {
        assert Betamax.instance.tapeRoot.deleteDir()
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

	def "can record post requests with a body"() {
		given:
		def request = new HttpPost("http://github.com/")
		request.entity = new StringEntity("q=1", URLENC.toString(), "UTF-8")

		when:
		tape.record(request, plainTextResponse)

		then:
		def interaction = tape.interactions[-1]
		interaction.request.entity.content.text == request.entity.content.text
	}
    
    def "can write the tape to disk"() {
        when:
        tape.eject()

        then:
        tape.file.isFile()

        and:
		println tape.file.text
        def json = tape.file.withReader { reader ->
			new JsonSlurper().parse(reader)
		}
		json.tape.name == tape.name

		json.tape.interactions.size() == 2
		json.tape.interactions[0].request.protocol == "HTTP/1.1"
		json.tape.interactions[0].request.method == "GET"
		json.tape.interactions[0].request.uri == "http://icanhascheezburger.com/"
		json.tape.interactions[0].response.protocol == "HTTP/1.1"
		json.tape.interactions[0].response.status == 200
		json.tape.interactions[0].response.body == "O HAI!"
             
		json.tape.interactions[1].request.method == "POST"
		json.tape.interactions[1].request.uri == "http://github.com/"
		json.tape.interactions[1].request.body == "q=1"
	}

}
