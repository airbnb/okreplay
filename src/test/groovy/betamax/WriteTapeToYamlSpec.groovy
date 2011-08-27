package betamax

import betamax.storage.yaml.YamlTape
import org.apache.http.HttpResponse
import org.yaml.snakeyaml.Yaml
import static java.net.HttpURLConnection.*
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1
import org.apache.http.client.methods.*
import org.apache.http.entity.*
import org.apache.http.message.*
import spock.lang.*

class WriteTapeToYamlSpec extends Specification {

	@Shared HttpGet getRequest
	@Shared HttpPost postRequest
	@Shared HttpResponse successResponse
	@Shared HttpResponse failureResponse
	@Shared HttpResponse imageResponse
	@Shared File image

	Yaml yamlReader

	def setupSpec() {
		getRequest = new HttpGet("http://icanhascheezburger.com/")
		getRequest.addHeader(ACCEPT_LANGUAGE, "en-GB,en")
		getRequest.addHeader(IF_NONE_MATCH, "b00b135")

		postRequest = new HttpPost("http://github.com/")
		postRequest.entity = new ByteArrayEntity("q=1".bytes)

		successResponse = new BasicHttpResponse(HTTP_1_1, HTTP_OK, "OK")
		successResponse.addHeader(CONTENT_TYPE, "text/plain")
		successResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
		successResponse.addHeader(CONTENT_ENCODING, "none")
		successResponse.entity = new StringEntity("O HAI!", "text/plain", "UTF-8")

		failureResponse = new BasicHttpResponse(HTTP_1_1, HTTP_BAD_REQUEST, "BAD REQUEST")
		failureResponse.addHeader(CONTENT_TYPE, "text/plain")
		failureResponse.addHeader(CONTENT_LANGUAGE, "en-GB")
		failureResponse.addHeader(CONTENT_ENCODING, "none")
		failureResponse.entity = new StringEntity("KTHXBYE!", "text/plain", "UTF-8")

		image = new File("src/test/resources/image.png")
		imageResponse = new BasicHttpResponse(HTTP_1_1, HTTP_OK, "OK")
		imageResponse.addHeader(CONTENT_TYPE, "image/png")
		imageResponse.entity = new ByteArrayEntity(image.bytes)
		imageResponse.entity.contentType = new BasicHeader(CONTENT_TYPE, "image/png")
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
		yaml.interactions[0].request.protocol == "HTTP/1.1"
		yaml.interactions[0].request.method == "GET"
		yaml.interactions[0].request.uri == "http://icanhascheezburger.com/"
		yaml.interactions[0].response.protocol == "HTTP/1.1"
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
}
