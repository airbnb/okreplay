package betamax

import betamax.storage.yaml.YamlTape
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.ByteArrayEntity
import betamax.encoding.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1
import org.apache.http.message.*
import spock.lang.*

@Issue("https://github.com/robfletcher/betamax/issues/21")
class ContentCharsetSpec extends Specification {

	@Unroll({"a response with a $charset body is recorded correctly"})
	def "response body's charset is recorded correctly"() {
		given:
		def request = new HttpGet("http://icanhascheezburger.com/")

		def bytes = encoder ? encoder.encode("\u00a3", charset) : "\u00a3".getBytes(charset)

		def entity = new ByteArrayEntity(bytes)
		entity.contentType = new BasicHeader(CONTENT_TYPE, "text/plain;charset=$charset")
		entity.contentEncoding = new BasicHeader(CONTENT_ENCODING, encoding)

		def response = new BasicHttpResponse(HTTP_1_1, HTTP_OK, "OK")
		response.entity = entity

		and:
		def tape = new YamlTape(name: "charsets")
		tape.record(request, response)

		when:
		def writer = new StringWriter()
		tape.writeTo(writer)

		then:
		def yaml = writer.toString()
		yaml.contains("body: \u00a3")

		where:
		charset      | encoding  | encoder
		"UTF-8"      | "none"    | null
		"ISO-8859-1" | "none"    | null
		"UTF-8"      | "gzip"    | new GzipEncoder()
		"ISO-8859-1" | "gzip"    | new GzipEncoder()
		"UTF-8"      | "deflate" | new DeflateEncoder()
		"ISO-8859-1" | "deflate" | new DeflateEncoder()
	}

	@Unroll({"a response with a $charset body is played back correctly"})
	def "response body's charset is played back correctly"() {
		given:
		def yaml = """\
!tape
name: charsets
interactions:
- recorded: 2011-08-27T23:25:45.000Z
  request:
    protocol: HTTP/1.1
    method: GET
    uri: http://icanhascheezburger.com/
  response:
    protocol: HTTP/1.1
    status: 200
    headers:
      Content-Type: text/plain;charset=$charset
      Content-Encoding: $encoding
    body: \u00a3
"""
		def tape = YamlTape.readFrom(new StringReader(yaml))

		and:
		def response = new BasicHttpResponse(HTTP_1_1, HTTP_OK, "OK")

		when:
		tape.seek(new HttpGet("http://icanhascheezburger.com/"))
		tape.play(response)

		then:
		response.entity.content.bytes == encoder ? encoder.encode("\u00a3", charset) : "\u00a3".getBytes(charset)

		where:
		charset      | encoding  | encoder
		"UTF-8"      | "none"    | null
		"ISO-8859-1" | "none"    | null
		"UTF-8"      | "gzip"    | new GzipEncoder()
		"ISO-8859-1" | "gzip"    | new GzipEncoder()
		"UTF-8"      | "deflate" | new DeflateEncoder()
		"ISO-8859-1" | "deflate" | new DeflateEncoder()
	}

}
