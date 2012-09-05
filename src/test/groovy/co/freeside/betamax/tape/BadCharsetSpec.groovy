package co.freeside.betamax.tape

import co.freeside.betamax.tape.yaml.YamlTape
import co.freeside.betamax.tape.yaml.YamlTapeLoader
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.message.BasicResponse
import org.yaml.snakeyaml.Yaml
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.*

@Issue('https://github.com/robfletcher/betamax/issues/52')
@Unroll
class BadCharsetSpec extends Specification {

	private static final File FILE_WITH_UTF8_DATA = new File('src/test/resources/views_one.css')

	Yaml yaml = new Yaml()
	File file

	void setup() {
		file = File.createTempFile('data', '.yaml')
	}

	void cleanup() {
		file.delete()
	}

	void 'can read back data encoded as #assumedCharset'() {
		given:
		def request = new BasicRequest('GET', 'http://freeside.co/betamax')

		def response = new RecordedResponse()
		response.status = HTTP_OK
		response.headers[CONTENT_TYPE] = 'text/plain'
		response.headers[CONTENT_LANGUAGE] = 'en-GB'
		response.headers[CONTENT_ENCODING] = 'none'
		response.body = new String(bytes.bytes, assumedCharset)
//		response.body = bytes.bytes

		and:
		file.withWriter { writer ->
			def tape = new YamlTape(name: 'bad_charset_spec')
			tape.record request, response
			tape.writeTo writer
		}

		when:
		def tape = file.withReader { reader ->
			YamlTape.readFrom reader
		}

		then:
		println file.text; sleep 50
		tape.interactions.first().response.bodyAsText.text.contains('\u2666')

		cleanup:
		bytes.close()

		where:
		assumedCharset | bytes
		'UTF-8'        | new ByteArrayInputStream('dt:before{content:"\u2666 "}'.getBytes('UTF-8'))
		'ISO-8859-1'   | new ByteArrayInputStream('dt:before{content:"\u2666 "}'.getBytes('UTF-8'))
		'UTF-8'        | FILE_WITH_UTF8_DATA.newInputStream()
		'ISO-8859-1'   | FILE_WITH_UTF8_DATA.newInputStream()
	}

}
