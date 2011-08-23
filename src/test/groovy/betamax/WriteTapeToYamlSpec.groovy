package betamax

import betamax.storage.yaml.YamlTapeLoader
import org.apache.commons.codec.binary.Base64
import org.apache.http.HttpResponse
import org.yaml.snakeyaml.Yaml
import betamax.storage.*
import static java.net.HttpURLConnection.*
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1
import org.apache.http.client.methods.*
import org.apache.http.entity.*
import org.apache.http.message.*
import spock.lang.*

class WriteTapeToYamlSpec extends Specification {

	TapeLoader loader = new YamlTapeLoader()

	@Shared HttpGet getRequest
	@Shared HttpPost postRequest
	@Shared HttpResponse successResponse
	@Shared HttpResponse failureResponse
	@Shared HttpResponse imageResponse
	@Shared File image

	def setupSpec() {
		getRequest = new HttpGet("http://icanhascheezburger.com/")
		getRequest.addHeader(ACCEPT_LANGUAGE, "en-GB,en")
		getRequest.addHeader(IF_NONE_MATCH, "b00b135")

		postRequest = new HttpPost("http://github.com/")
		postRequest.entity = new ByteArrayEntity("q=1".bytes)

		successResponse = new BasicHttpResponse(HTTP_1_1, HTTP_OK, "OK")
		successResponse.addHeader(CONTENT_TYPE, "text/plain")
		successResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
		successResponse.addHeader(CONTENT_ENCODING, "gzip")
		successResponse.entity = new StringEntity("O HAI!", "text/plain", "UTF-8")

		failureResponse = new BasicHttpResponse(HTTP_1_1, HTTP_BAD_REQUEST, "BAD REQUEST")
		failureResponse.addHeader(CONTENT_TYPE, "text/plain")
		failureResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
		failureResponse.addHeader(CONTENT_ENCODING, "gzip")
		failureResponse.entity = new StringEntity("KTHXBYE!", "text/plain", "UTF-8")

		image = new File("src/test/resources/image.png")
		imageResponse = new BasicHttpResponse(HTTP_1_1, HTTP_OK, "OK")
		imageResponse.addHeader(CONTENT_TYPE, "image/png")
		imageResponse.entity = new ByteArrayEntity(image.bytes)
		imageResponse.entity.contentType = new BasicHeader(CONTENT_TYPE, "image/png")
	}

	def "can write a tape to storage"() {
		given:
		def tape = new Tape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		loader.writeTape(tape, writer)

		then:
		println writer.toString()
		def yaml = new Yaml().load(writer.toString())
		yaml.tape.name == tape.name

		yaml.tape.interactions.size() == 1
		yaml.tape.interactions[0].recorded ==~ /\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} [\+-]\d{4}/

		yaml.tape.interactions[0].request.protocol == "HTTP/1.1"
		yaml.tape.interactions[0].request.method == "GET"
		yaml.tape.interactions[0].request.uri == "http://icanhascheezburger.com/"
		yaml.tape.interactions[0].response.protocol == "HTTP/1.1"
		yaml.tape.interactions[0].response.status == HTTP_OK
		yaml.tape.interactions[0].response.body == "O HAI!"
	}

	def "writes request headers"() {
		given:
		def tape = new Tape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		loader.writeTape(tape, writer)

		then:
		def yaml = new Yaml().load(writer.toString())
		yaml.tape.interactions[0].request.headers[ACCEPT_LANGUAGE] == "en-GB,en"
		yaml.tape.interactions[0].request.headers[IF_NONE_MATCH] == "b00b135"
	}

	def "writes response headers"() {
		given:
		def tape = new Tape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		loader.writeTape(tape, writer)

		then:
		def yaml = new Yaml().load(writer.toString())
		yaml.tape.interactions[0].response.headers[CONTENT_TYPE] == "text/plain"
		yaml.tape.interactions[0].response.headers[CONTENT_LANGUAGE] == "en-GB"
		yaml.tape.interactions[0].response.headers[CONTENT_ENCODING] == "gzip"
	}

	def "can write requests with a body"() {
		given:
		def tape = new Tape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(postRequest, successResponse)
		loader.writeTape(tape, writer)

		then:
		def yaml = new Yaml().load(writer.toString())
		yaml.tape.interactions[0].request.method == "POST"
		yaml.tape.interactions[0].request.body == "q=1"
	}

	def "can write multiple interactions"() {
		given:
		def tape = new Tape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.record(postRequest, failureResponse)
		loader.writeTape(tape, writer)

		then:
		def yaml = new Yaml().load(writer.toString())
		yaml.tape.interactions.size() == 2
		yaml.tape.interactions[0].request.method == "GET"
		yaml.tape.interactions[1].request.method == "POST"
		yaml.tape.interactions[0].response.status == HTTP_OK
		yaml.tape.interactions[1].response.status == HTTP_BAD_REQUEST
	}

	@Ignore
	def "can write a binary response body"() {
		given:
		def tape = new Tape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, imageResponse)
		loader.writeTape(tape, writer)

		then:
		def yaml = new Yaml().load(writer.toString())
		yaml.tape.interactions[0].response.headers[CONTENT_TYPE] == "image/png"
		yaml.tape.interactions[0].response.body == Base64.encodeBase64String(image.bytes)
	}
}
