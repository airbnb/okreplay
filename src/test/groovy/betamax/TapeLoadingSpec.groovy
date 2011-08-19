package betamax

import betamax.storage.json.JsonTapeLoader
import java.text.ParseException
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.message.BasicHttpResponse
import spock.lang.Specification
import betamax.storage.*
import groovy.json.*
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1

class TapeLoadingSpec extends Specification {

	TapeLoader loader = new JsonTapeLoader()

	def "can write a tape to storage"() {
		given:
		def request = new HttpGet("http://icanhascheezburger.com/")
		def response = new BasicHttpResponse(HTTP_1_1, 200, "OK")
		response.addHeader(CONTENT_TYPE, "text/plain")
		response.addHeader(CONTENT_LANGUAGE, "en-GB")
		response.addHeader(CONTENT_ENCODING, "gzip")
		response.entity = new BasicHttpEntity()
		response.entity.content = new ByteArrayInputStream("O HAI!".bytes)
		response.entity.contentLength = 6L

		and:
		def tape = new Tape(name: "tape_loading_spec")
		def writer = new StringWriter()

		when:
		tape.record(request, response)
		loader.writeTape(tape, writer)

		then:
		def json = new JsonSlurper().parseText(writer.toString())
		json.tape.name == tape.name

		json.tape.interactions.size() == 1
		json.tape.interactions[0].recorded ==~ /\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} [\+-]\d{4}/

		json.tape.interactions[0].request.protocol == "HTTP/1.1"
		json.tape.interactions[0].request.method == "GET"
		json.tape.interactions[0].request.uri == "http://icanhascheezburger.com/"
		json.tape.interactions[0].response.protocol == "HTTP/1.1"
		json.tape.interactions[0].response.status == 200
		json.tape.interactions[0].response.body == "O HAI!"
	}

	def "can load a valid tape with a single interaction"() {
		given:
		def json = """\
{
	"tape": {
		"name": "single_interaction_tape",
		"interactions": [
			{
				"recorded": "2011-08-19 12:45:33 +0100",
				"request": {
					"protocol": "HTTP/1.1",
					"method": "GET",
					"uri": "http://icanhascheezburger.com/"
				},
				"response": {
					"protocol": "HTTP/1.1",
					"status": 200,
					"body": "O HAI!"
				}
			}
		]
	}
}"""
		when:
		def tape = loader.readTape(new StringReader(json))

		then:
		tape.name == "single_interaction_tape"
		tape.interactions.size() == 1
		tape.interactions[0].recorded == new Date(111, 7, 19, 12, 45, 33)
		tape.interactions[0].request.requestLine.protocolVersion == HTTP_1_1
		tape.interactions[0].request.requestLine.method == "GET"
		tape.interactions[0].request.requestLine.uri == "http://icanhascheezburger.com/"
		tape.interactions[0].response.statusLine.protocolVersion == HTTP_1_1
		tape.interactions[0].response.statusLine.statusCode == 200
		tape.interactions[0].response.entity.content.text == "O HAI!"
	}

	def "barfs on non-json data"() {
		given:
		def json = "THIS IS NOT JSON"

		when:
		loader.readTape(new StringReader(json))

		then:
		def e = thrown(TapeLoadException)
		e.cause instanceof JsonException
	}

	def "barfs on an invalid record date"() {
		given:
		def json = """\
{
	"tape": {
		"name": "invalid_timestamp_tape",
		"interactions": [
			{
				"recorded": "THIS IS NOT A DATE",
				"request": {
					"protocol": "HTTP/1.1",
					"method": "GET",
					"uri": "http://icanhascheezburger.com/"
				},
				"response": {
					"protocol": "HTTP/1.1",
					"status": 200,
					"body": "O HAI!"
				}
			}
		]
	}
}"""
		when:
		loader.readTape(new StringReader(json))

		then:
		def e = thrown(TapeLoadException)
		e.cause instanceof ParseException
	}

}
