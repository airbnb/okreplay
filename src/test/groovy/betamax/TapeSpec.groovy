package betamax

import betamax.encoding.GzipEncoder
import betamax.storage.MemoryTape
import org.apache.http.message.BasicHttpResponse
import static betamax.TapeMode.*
import static groovyx.net.http.ContentType.URLENC
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1
import org.apache.http.client.methods.*
import org.apache.http.entity.*
import spock.lang.*

@Stepwise
class TapeSpec extends Specification {

	@Shared Tape tape = new MemoryTape(name: "tape_spec")
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

	def cleanup() {
		tape.reset()
		tape.mode = READ_WRITE
	}

	def "reading from an empty tape throws an exception"() {
		when: "an empty tape is played"
		tape.play(new BasicHttpResponse(HTTP_1_1, 200, "OK"))

		then: "an exception is thrown"
		thrown IllegalStateException
	}

	def "can write an HTTP interaction to a tape"() {
		when: "an HTTP interaction is recorded to tape"
		tape.record(getRequest, plainTextResponse)

		then: "the size of the tape increases"
		tape.size() == old(tape.size()) + 1
		def interaction = tape.interactions[-1]

		and: "the request data is correctly stored"
		interaction.request.method == getRequest.requestLine.method
		interaction.request.uri == getRequest.requestLine.uri
		interaction.request.protocol == getRequest.requestLine.protocolVersion.toString()

		and: "the response data is correctly stored"
		interaction.response.protocol == plainTextResponse.statusLine.protocolVersion.toString()
		interaction.response.status == plainTextResponse.statusLine.statusCode
		interaction.response.body == "O HAI!"
		interaction.response.headers[CONTENT_TYPE] == plainTextResponse.getFirstHeader(CONTENT_TYPE).value
		interaction.response.headers[CONTENT_LANGUAGE] == plainTextResponse.getFirstHeader(CONTENT_LANGUAGE).value
		interaction.response.headers[CONTENT_ENCODING] == plainTextResponse.getFirstHeader(CONTENT_ENCODING).value
	}
	
	def "can overwrite a recorded interaction"() {
		given: "the tape is ready to play"
		tape.seek(getRequest)

		when: "a recording is made"
		tape.record(getRequest, plainTextResponse)

		then: "the tape size does not increase"
		tape.size() == old(tape.size())

		and: "the previous recording was overwritten"
		tape.interactions[-1].recorded > old(tape.interactions[-1].recorded)
	}

	def "seek does not match a request for a different URI"() {
		given:
		def request = new HttpGet("http://qwantz.com/")

		expect:
		!tape.seek(request)
	}

	def "can seek for a previously recorded interaction"() {
		expect:
		tape.seek(getRequest)
	}

	def "can read a stored interaction"() {
		given: "an http response to play back to"
		def response = new BasicHttpResponse(HTTP_1_1, 200, "OK")

		and: "the tape is ready to play"
		tape.seek(getRequest)

		when: "the tape is played"
		tape.play(response)

		then: "the recorded response data is copied onto the response"
		response.statusLine.protocolVersion == plainTextResponse.statusLine.protocolVersion
		response.statusLine.statusCode == plainTextResponse.statusLine.statusCode
		new GzipEncoder().decode(response.entity.content) == "O HAI!"
		response.getHeaders(CONTENT_TYPE).value == plainTextResponse.getHeaders(CONTENT_TYPE).value
		response.getHeaders(CONTENT_LANGUAGE).value == plainTextResponse.getHeaders(CONTENT_LANGUAGE).value
		response.getHeaders(CONTENT_ENCODING).value == plainTextResponse.getHeaders(CONTENT_ENCODING).value
	}

	def "can record post requests with a body"() {
		given: "a request with some content"
		def request = new HttpPost("http://github.com/")
		request.entity = new StringEntity("q=1", URLENC.toString(), "UTF-8")

		when: "the request and its response are recorded"
		tape.record(request, plainTextResponse)

		then: "the request body is stored on the tape"
		def interaction = tape.interactions[-1]
		interaction.request.body == request.entity.content.text
	}

	def "can reset the tape position"() {
		given: "the tape is ready to read"
		tape.seek(getRequest)

		when: "the tape position is reset"
		tape.reset()

		and: "the tape is played"
		tape.play(new BasicHttpResponse(HTTP_1_1, 200, "OK"))

		then: "an exception is thrown"
		def e = thrown(IllegalStateException)
		e.message == "the tape is not ready to play"
	}

	def "a write-only tape cannot be read from"() {
		given: "the tape is put into write-only mode"
		tape.mode = WRITE_ONLY

		and: "the tape is ready to read"
		tape.seek(getRequest)

		when: "the tape is played"
		tape.play(new BasicHttpResponse(HTTP_1_1, 200, "OK"))

		then: "an exception is thrown"
		def e = thrown(IllegalStateException)
		e.message == "the tape is not readable"
	}

	def "a read-only tape cannot be written to"() {
		given: "the tape is put into read-only mode"
		tape.mode = READ_ONLY

		when: "the tape is recorded to"
		tape.record(getRequest, plainTextResponse)

		then: "an exception is thrown"
		def e = thrown(IllegalStateException)
		e.message == "the tape is not writable"
	}

}
