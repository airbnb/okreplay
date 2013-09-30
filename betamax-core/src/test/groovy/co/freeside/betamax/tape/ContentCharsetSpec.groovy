package co.freeside.betamax.tape

import co.freeside.betamax.tape.yaml.YamlTape
import co.freeside.betamax.util.message.*
import spock.lang.*
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.*

@Issue('https://github.com/robfletcher/betamax/issues/21')
@Unroll
class ContentCharsetSpec extends Specification {

	void 'a response with a #charset body is recorded correctly'() {
		given:
		def request = new BasicRequest()

		def response = new BasicResponse(HTTP_OK, 'OK')
		response.addHeader(CONTENT_TYPE, "text/plain;charset=$charset")
		response.addHeader(CONTENT_ENCODING, "none")
		response.body = '\u00a3'.getBytes(charset)

		and:
		def tape = new YamlTape(name: 'charsets')
		tape.record(request, response)

		when:
		def writer = new StringWriter()
		tape.writeTo(writer)

		then:
		def yaml = writer.toString()
		yaml.contains('body: \u00a3')

		where:
		charset << ['UTF-8', 'ISO-8859-1']
	}

	void 'a response with a #charset body is played back correctly'() {
		given:
		def yaml = """\
!tape
name: charsets
interactions:
- recorded: 2011-08-27T23:25:45.000Z
  request:
    method: GET
    uri: http://freeside.co/betamax
  response:
    status: 200
    headers:
      Content-Type: text/plain;charset=$charset
      Content-Encoding: none
    body: \u00a3
"""
		def tape = YamlTape.readFrom(new StringReader(yaml))

		and:
		def request = new BasicRequest('GET', 'http://freeside.co/betamax')

		when:
		def response = tape.play(request)

		then:
		def expected = '\u00a3'.getBytes(charset)
		response.bodyAsBinary.bytes == expected

		where:
		charset << ['UTF-8', 'ISO-8859-1']
	}

}
