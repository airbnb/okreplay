package co.freeside.betamax.tape

import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response
import co.freeside.betamax.tape.yaml.YamlTape
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.message.BasicResponse
import org.yaml.snakeyaml.Yaml
import spock.lang.Shared
import spock.lang.Specification

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.*

class WriteTapeToYamlSpec extends Specification {

	@Shared Request getRequest
	@Shared Request postRequest
	@Shared Response successResponse
	@Shared Response failureResponse
	@Shared Response imageResponse
	@Shared File image

	Yaml yamlReader

	void setupSpec() {
		getRequest = new BasicRequest('GET', 'http://freeside.co/betamax')
		getRequest.addHeader(ACCEPT_LANGUAGE, 'en-GB,en')
		getRequest.addHeader(IF_NONE_MATCH, 'b00b135')

		postRequest = new BasicRequest('POST', 'http://github.com/')
		postRequest.body = 'q=1'.bytes

		successResponse = new BasicResponse(HTTP_OK, 'OK')
		successResponse.addHeader(CONTENT_TYPE, 'text/plain')
		successResponse.addHeader(CONTENT_LANGUAGE, 'en-GB')
		successResponse.addHeader(CONTENT_ENCODING, 'none')
		successResponse.body = 'O HAI!'.getBytes('UTF-8')

		failureResponse = new BasicResponse(HTTP_BAD_REQUEST, 'BAD REQUEST')
		failureResponse.addHeader(CONTENT_TYPE, 'text/plain')
		failureResponse.addHeader(CONTENT_LANGUAGE, 'en-GB')
		failureResponse.addHeader(CONTENT_ENCODING, 'none')
		failureResponse.body = 'KTHXBYE!'.getBytes('UTF-8')

		image = new File(Class.getResource('/image.png').toURI())
		imageResponse = new BasicResponse(HTTP_OK, 'OK')
		imageResponse.addHeader(CONTENT_TYPE, 'image/png')
		imageResponse.body = image.bytes
	}

	void setup() {
		yamlReader = new Yaml()
	}

	void 'can write a tape to storage'() {
		given:
		def tape = new YamlTape(name: 'tape_loading_spec')
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.name == tape.name

		yaml.interactions.size() == 1
		yaml.interactions[0].recorded instanceof Date
		yaml.interactions[0].request.method == 'GET'
		yaml.interactions[0].request.uri == 'http://freeside.co/betamax'
		yaml.interactions[0].response.status == HTTP_OK
		yaml.interactions[0].response.body == 'O HAI!'
	}

	void 'writes request headers'() {
		given:
		def tape = new YamlTape(name: 'tape_loading_spec')
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.interactions[0].request.headers[ACCEPT_LANGUAGE] == 'en-GB,en'
		yaml.interactions[0].request.headers[IF_NONE_MATCH] == 'b00b135'
	}

	void 'writes response headers'() {
		given:
		def tape = new YamlTape(name: 'tape_loading_spec')
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.interactions[0].response.headers[CONTENT_TYPE] == 'text/plain'
		yaml.interactions[0].response.headers[CONTENT_LANGUAGE] == 'en-GB'
		yaml.interactions[0].response.headers[CONTENT_ENCODING] == 'none'
	}

	void 'can write requests with a body'() {
		given:
		def tape = new YamlTape(name: 'tape_loading_spec')
		def writer = new StringWriter()

		when:
		tape.record(postRequest, successResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.interactions[0].request.method == 'POST'
		yaml.interactions[0].request.body == 'q=1'
	}

	void 'can write multiple interactions'() {
		given:
		def tape = new YamlTape(name: 'tape_loading_spec')
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.record(postRequest, failureResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.interactions.size() == 2
		yaml.interactions[0].request.method == 'GET'
		yaml.interactions[1].request.method == 'POST'
		yaml.interactions[0].response.status == HTTP_OK
		yaml.interactions[1].response.status == HTTP_BAD_REQUEST
	}

	void 'can write a binary response body'() {
		given:
		def tape = new YamlTape(name: 'tape_loading_spec')
		def writer = new StringWriter()

		when:
		tape.record(getRequest, imageResponse)
		tape.writeTo(writer)

		then:
		def yaml = yamlReader.loadAs(writer.toString(), Map)
		yaml.interactions[0].response.headers[CONTENT_TYPE] == 'image/png'
		yaml.interactions[0].response.body == image.bytes
	}

	void 'text response body is written to file as plain text'() {
		given:
		def tape = new YamlTape(name: 'tape_loading_spec')
		def writer = new StringWriter()

		when:
		tape.record(getRequest, successResponse)
		tape.writeTo(writer)

		then:
		writer.toString().contains('body: O HAI!')
	}

	void 'binary response body is written to file as binary data'() {
		given:
		def tape = new YamlTape(name: 'tape_loading_spec')
		def writer = new StringWriter()

		when:
		tape.record(getRequest, imageResponse)
		tape.writeTo(writer)

		then:
		writer.toString().contains('body: !!binary |-')
	}

}
