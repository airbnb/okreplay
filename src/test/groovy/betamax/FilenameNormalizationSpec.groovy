package betamax

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHttpResponse
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1
import spock.lang.*

class FilenameNormalizationSpec extends Specification {

	@Shared Recorder recorder = Recorder.instance
	@Shared HttpGet request
	@Shared HttpResponse response

	def setupSpec() {
		recorder.tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")

		request = new HttpGet("http://icanhascheezburger.com/")

		response = new BasicHttpResponse(HTTP_1_1, HTTP_OK, "OK")
		response.addHeader(CONTENT_TYPE, "text/plain")
		response.addHeader(CONTENT_LANGUAGE, "en-GB")
		response.addHeader(CONTENT_ENCODING, "gzip")
		response.entity = new StringEntity("O HAI!", "text/plain", "UTF-8")
	}

	def cleanup() {
		assert recorder.tapeRoot.deleteDir()
	}

	@Unroll({"a tape named '$tapeName' is written to a file called '$filename'"})
	def "tape filenames are normalized"() {
		given:
		def tape = recorder.insertTape(tapeName)
		tape.record(request, response)

		when:
		recorder.ejectTape()

		then:
		new File(recorder.tapeRoot, filename).isFile()

		where:
		tapeName  | filename
		"my_tape" | "my_tape.json"
		"my tape" | "my_tape.json"
		"%)1aé"   | "_1ae.json"
	}
}
