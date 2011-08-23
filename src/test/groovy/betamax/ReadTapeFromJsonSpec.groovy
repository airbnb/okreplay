package betamax

import betamax.storage.json.JsonTapeLoader
import groovy.json.JsonException
import spock.lang.Specification
import betamax.storage.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1

class ReadTapeFromJsonSpec extends Specification {

	TapeLoader loader = new JsonTapeLoader()

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
					"headers": {
						"Content-Type": "text/plain",
						"Content-Language": "en-GB",
						"Content-Encoding": "gzip"
					},
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
		tape.interactions[0].response.statusLine.statusCode == HTTP_OK
		tape.interactions[0].response.getFirstHeader(CONTENT_TYPE).value == "text/plain"
		tape.interactions[0].response.getFirstHeader(CONTENT_LANGUAGE).value == "en-GB"
		tape.interactions[0].response.getFirstHeader(CONTENT_ENCODING).value == "gzip"
		tape.interactions[0].response.entity.content.text == "O HAI!"
	}

	def "can load a valid tape with multiple interactions"() {
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
			},
			{
				"recorded": "2011-08-19 21:19:14 +0100",
				"request": {
					"protocol": "HTTP/1.1",
					"method": "GET",
					"uri": "http://en.wikipedia.org/wiki/Hyper_Text_Coffee_Pot_Control_Protocol"
				},
				"response": {
					"protocol": "HTTP/1.1",
					"status": 418,
					"body": "I'm a teapot"
				}
			}
		]
	}
}"""
		when:
		def tape = loader.readTape(new StringReader(json))

		then:
		tape.interactions.size() == 2
		tape.interactions[0].request.requestLine.uri == "http://icanhascheezburger.com/"
		tape.interactions[1].request.requestLine.uri == "http://en.wikipedia.org/wiki/Hyper_Text_Coffee_Pot_Control_Protocol"
		tape.interactions[0].response.statusLine.statusCode == HTTP_OK
		tape.interactions[1].response.statusLine.statusCode == 418
		tape.interactions[0].response.entity.content.text == "O HAI!"
		tape.interactions[1].response.entity.content.text == "I'm a teapot"
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
		e.cause instanceof java.text.ParseException
	}

	def "barfs on missing fields"() {
		given:
		def json = """\
{
	"tape": {
		"name": "invalid_timestamp_tape",
		"interactions": [
			{
				"recorded": "2011-08-23 17:35:26 +0100",
				"request": {
					"protocol": "HTTP/1.1",
					"method": "GET",
					"uri": "http://icanhascheezburger.com/"
				},
				"response": {
					"protocol": "HTTP/1.1",
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
		e.message == "Missing element 'status'"
	}

	def "barfs if no tape at root"() {
		given:
		def json = """\
{
	"name": "invalid_structure_tape"
}"""
		when:
		loader.readTape(new StringReader(json))

		then:
		def e = thrown(TapeLoadException)
		e.message == "Missing element 'tape'"
	}

}
