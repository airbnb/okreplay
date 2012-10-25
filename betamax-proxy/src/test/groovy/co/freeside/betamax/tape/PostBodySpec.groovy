package co.freeside.betamax.tape

import co.freeside.betamax.*
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.*
import org.junit.Rule
import org.yaml.snakeyaml.Yaml
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static co.freeside.betamax.util.FileUtils.newTempDir

@Issue('https://github.com/robfletcher/betamax/issues/50')
class PostBodySpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot)

	private DefaultHttpClient httpClient = new SystemDefaultHttpClient()

	void 'post body is stored on tape when using UrlConnection'() {
		given:
		def postBody = '{"foo":"bar"}'
		HttpURLConnection connection = 'http://httpbin.org/post'.toURL().openConnection()
		connection.doOutput = true
		connection.requestMethod = 'POST'
		connection.addRequestProperty('Content-Type', 'application/json')

		and:
		recorder.start('post_body_with_url_connection', [mode: WRITE_ONLY])

		when:
		connection.outputStream.withStream { stream ->
			stream << postBody.getBytes('UTF-8')
		}
		println connection.inputStream.text

		and:
		recorder.stop()

		then:
		def file = new File(tapeRoot, 'post_body_with_url_connection.yaml')
		def tapeData = file.withReader {
			new Yaml().loadAs(it, Map)
		}
		tapeData.interactions[0].request.body == postBody
	}

	void 'post body is stored on tape when using HttpClient'() {
		given:
		def postBody = '{"foo":"bar"}'
		def httpPost = new HttpPost('http://httpbin.org/post')
		httpPost.setHeader('Content-Type', 'application/json')
		def reqEntity = new StringEntity(postBody, 'UTF-8')
		reqEntity.setContentType('application/json')
		httpPost.entity = reqEntity

		and:
		recorder.start('post_body_with_http_client', [mode: WRITE_ONLY])

		when:
		httpClient.execute(httpPost)

		and:
		recorder.stop()

		then:
		def file = new File(tapeRoot, 'post_body_with_http_client.yaml')
		def tapeData = file.withReader {
			new Yaml().loadAs(it, Map)
		}
		tapeData.interactions[0].request.body == postBody
	}

}
