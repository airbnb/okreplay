package co.freeside.betamax.tape

import co.freeside.betamax.tape.yaml.YamlTape
import co.freeside.betamax.encoding.*
import co.freeside.betamax.util.message.*
import spock.lang.*

import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.*

@Issue("https://github.com/robfletcher/betamax/issues/3")
class ContentEncodingSpec extends Specification {

	@Unroll("a #encoding encoded response body is stored as plain text in a tape file")
	def "an encoded response body is stored as plain text in a tape file"() {
		given:
		def request = new BasicRequest("GET", "http://freeside.co/betamax")
		request.addHeader(ACCEPT_ENCODING, encoding)

		def response = new BasicResponse(HTTP_OK, "OK")
		response.addHeader(CONTENT_TYPE, "text/plain")
		response.addHeader(CONTENT_ENCODING, encoding)
		response.body = encoder.encode("O HAI!")

		and:
		def tape = new YamlTape(name: "encoded response tape")
		tape.record(request, response)

		when:
		def writer = new StringWriter()
		tape.writeTo(writer)

		then:
		def yaml = writer.toString()
		yaml.contains("body: O HAI!")

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
	}

	@Unroll("response body is encoded when played from tape and a #encoding content-encoding header is present")
	def "response body is encoded when played from tape and a content-encoding header is present"() {
		given:
		def yaml = """\
!tape
name: encoded response tape
interactions:
- recorded: 2011-08-24T20:38:40.000Z
  request:
    method: GET
    uri: http://freeside.co/betamax
    headers: {Accept-Encoding: $encoding}
  response:
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB, Content-Encoding: $encoding}
    body: O HAI!
"""
		def tape = YamlTape.readFrom(new StringReader(yaml))

		and:
		def request = new BasicRequest("GET", "http://freeside.co/betamax")
		def response = new BasicResponse(200, "OK")

		when:
		tape.play(request, response)

		then:
		response.getHeader(CONTENT_ENCODING) == encoding
		encoder.decode(new ByteArrayInputStream(response.body)) == "O HAI!"

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
	}

}
