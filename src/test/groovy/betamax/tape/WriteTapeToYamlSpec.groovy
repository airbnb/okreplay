package betamax.tape

import betamax.tape.yaml.YamlTape
import org.apache.http.HttpEntity
import org.apache.http.entity.ByteArrayEntity
import org.yaml.snakeyaml.Yaml
import betamax.proxy.*
import betamax.util.message.*
import static java.net.HttpURLConnection.*
import static org.apache.http.HttpHeaders.*
import spock.lang.*

class WriteTapeToYamlSpec extends Specification {

	@Shared Request getRequest
	@Shared Request postRequest
	@Shared Response successResponse
	@Shared Response failureResponse
	@Shared Response imageResponse
	@Shared File image

	Yaml yamlReader

	def setupSpec() {
		getRequest = new BasicRequest("GET", "http://robfletcher.github.com/betamax")
		getRequest.addHeader(ACCEPT_LANGUAGE, "en-GB,en")
		getRequest.addHeader(IF_NONE_MATCH, "b00b135")

		postRequest = new BasicRequest("POST", "http://github.com/")
		postRequest.body = "q=1".bytes

		successResponse = new BasicResponse(HTTP_OK, "OK")
		successResponse.addHeader(CONTENT_TYPE, "text/plain")
		successResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
		successResponse.addHeader(CONTENT_ENCODING, "none")
		successResponse.body = "O HAI!".getBytes("UTF-8")

		failureResponse = new BasicResponse(HTTP_BAD_REQUEST, "BAD REQUEST")
		failureResponse.addHeader(CONTENT_TYPE, "text/plain")
		failureResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
		failureResponse.addHeader(CONTENT_ENCODING, "none")
		failureResponse.body = "KTHXBYE!".getBytes("UTF-8")

		image = new File("src/test/resources/image.png")
		imageResponse = new BasicResponse(HTTP_OK, "OK")
		imageResponse.addHeader(CONTENT_TYPE, "image/png")
		imageResponse.body = image.bytes
	}

	def setup() {
		yamlReader = new Yaml()
	}

	def "can write a tape to storage"() {
		given:
		def tape = new YamlTape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.name == tape.name

		yaml.interactions.size() == 1
		yaml.interactions[0].recorded instanceof Date
		yaml.interactions[0].request.method == "GET"
		yaml.interactions[0].request.uri == "http://robfletcher.github.com/betamax"
		yaml.interactions[0].response.status == HTTP_OK
		yaml.interactions[0].response.body == "O HAI!"
	}

	def "writes request headers"() {
		given:
		def tape = new YamlTape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.interactions[0].request.headers[ACCEPT_LANGUAGE] == "en-GB,en"
		yaml.interactions[0].request.headers[IF_NONE_MATCH] == "b00b135"
	}

	def "writes response headers"() {
		given:
		def tape = new YamlTape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.interactions[0].response.headers[CONTENT_TYPE] == "text/plain"
		yaml.interactions[0].response.headers[CONTENT_LANGUAGE] == "en-GB"
		yaml.interactions[0].response.headers[CONTENT_ENCODING] == "none"
	}

	def "can write requests with a body"() {
		given:
		def tape = new YamlTape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(postRequest, successResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.interactions[0].request.method == "POST"
		yaml.interactions[0].request.body == "q=1"
	}

	def "can write multiple interactions"() {
		given:
		def tape = new YamlTape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.record(postRequest, failureResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.interactions.size() == 2
		yaml.interactions[0].request.method == "GET"
		yaml.interactions[1].request.method == "POST"
		yaml.interactions[0].response.status == HTTP_OK
		yaml.interactions[1].response.status == HTTP_BAD_REQUEST
	}

	def "can write a binary response body"() {
		given:
		def tape = new YamlTape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, imageResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.interactions[0].response.headers[CONTENT_TYPE] == "image/png"
		yaml.interactions[0].response.body == image.bytes
	}

	def "text response body is written to file as plain text"() {
		given:
		def tape = new YamlTape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.writeTo(writer)

		then:
		writer.toString().contains("body: O HAI!")
	}

	def "binary response body is written to file as binary data"() {
		given:
		def tape = new YamlTape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(getRequest, imageResponse)
		tape.writeTo(writer)

		then:
		writer.toString().contains("body: !!binary |-")
	}

	private HttpEntity createEntity(String text, String contentType, String encoding, String charset) {
		def entity = new ByteArrayEntity(text.getBytes(charset))
		entity.setContentEncoding(encoding)
		entity.setContentType(contentType)
		return entity
	}

}
