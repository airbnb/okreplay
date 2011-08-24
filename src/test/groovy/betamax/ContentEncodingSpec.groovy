package betamax

import betamax.encoding.GzipEncoder
import betamax.storage.yaml.YamlTapeLoader
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.message.BasicHttpResponse
import betamax.storage.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1
import spock.lang.*

@Issue("https://github.com/robfletcher/betamax/issues/3")
class ContentEncodingSpec extends Specification {

	TapeLoader loader = new YamlTapeLoader()

	def "an encoded response body is stored as plain text in a tape file"() {
		given:
		def request = new HttpGet("http://icanhascheezburger.com/")
		request.addHeader(ACCEPT_ENCODING, "gzip")

		def response = new BasicHttpResponse(HTTP_1_1, HTTP_OK, "OK")
		response.addHeader(CONTENT_TYPE, "text/plain")
		response.addHeader(CONTENT_LANGUAGE, "en-GB")
		response.addHeader(CONTENT_ENCODING, "gzip")
		response.entity = new ByteArrayEntity(GzipEncoder.encode("O HAI!"))

		and:
		def tape = new Tape(name: "encoded response tape")
		tape.record(request, response)

		when:
		def writer = new StringWriter()
		loader.writeTape(tape, writer)

		then:
		def yaml = writer.toString()
		yaml.contains("Content-Encoding: gzip")
		yaml.contains("body: O HAI!")
	}

	def "response body is encoded when loaded from tape and a content-encoding header is present"() {
		given:
		def yaml = """\
tape:
  name: encoded response tape
  interactions:
  - recorded: 2011-08-24T20:38:40.000Z
    request:
      protocol: HTTP/1.1
      method: GET
      uri: http://icanhascheezburger.com/
      headers: {Accept-Encoding: gzip}
    response:
      protocol: HTTP/1.1
      status: 200
      headers: {Content-Type: text/plain, Content-Language: en-GB, Content-Encoding: gzip}
      body: O HAI!
"""
		when:
		def tape = loader.readTape(new StringReader(yaml))

		then:
		tape.name == "encoded response tape"
		tape.interactions[0].response.getFirstHeader(CONTENT_ENCODING).value == "gzip"
		GzipEncoder.decode(tape.interactions[0].response.entity.content) == "O HAI!"
	}

}
