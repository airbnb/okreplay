package betamax

import betamax.storage.yaml.YamlTapeLoader
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.message.BasicHttpResponse
import betamax.encoding.*
import betamax.storage.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1
import spock.lang.*

@Issue("https://github.com/robfletcher/betamax/issues/3")
class ContentEncodingSpec extends Specification {

	TapeLoader loader = new YamlTapeLoader()

	@Unroll({"a $encoding encoded response body is stored as plain text in a tape file"})
	def "an encoded response body is stored as plain text in a tape file"() {
		given:
		def request = new HttpGet("http://icanhascheezburger.com/")
		request.addHeader(ACCEPT_ENCODING, encoding)

		def response = new BasicHttpResponse(HTTP_1_1, HTTP_OK, "OK")
		response.addHeader(CONTENT_TYPE, "text/plain")
		response.addHeader(CONTENT_LANGUAGE, "en-GB")
		response.addHeader(CONTENT_ENCODING, encoding)
		response.entity = new ByteArrayEntity(encoder.encode("O HAI!"))

		and:
		def tape = new Tape(name: "encoded response tape")
		tape.record(request, response)

		when:
		def writer = new StringWriter()
		loader.writeTape(tape, writer)

		then:
		def yaml = writer.toString()
		yaml.contains("Content-Encoding: $encoding")
		yaml.contains("body: O HAI!")

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
	}

	@Unroll({"response body is encoded when loaded from tape and a $encoding content-encoding header is present"})
	def "response body is encoded when loaded from tape and a content-encoding header is present"() {
		given:
		def yaml = """\
!tape
name: encoded response tape
interactions:
- recorded: 2011-08-24T20:38:40.000Z
  request:
    protocol: HTTP/1.1
    method: GET
    uri: http://icanhascheezburger.com/
    headers: {Accept-Encoding: $encoding}
  response:
    protocol: HTTP/1.1
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB, Content-Encoding: $encoding}
    body: O HAI!
"""
		when:
		def tape = loader.readTape(new StringReader(yaml))

		then:
		tape.name == "encoded response tape"
		tape.interactions[0].response.headers[CONTENT_ENCODING] == encoding
		encoder.decode(tape.interactions[0].response.body) == "O HAI!"

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
	}

}
