package co.freeside.betamax.tape

import co.freeside.betamax.Recorder
import co.freeside.betamax.TapeMode
import co.freeside.betamax.tape.yaml.YamlTape
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.junit.Rule
import org.yaml.snakeyaml.Yaml
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import static co.freeside.betamax.TapeMode.WRITE_ONLY

@Issue('https://github.com/robfletcher/betamax/issues/50')
class PostBodySpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = new File(System.properties.'java.io.tmpdir', 'tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)

	private DefaultHttpClient httpClient = new DefaultHttpClient()

	void setup() {
		httpClient.routePlanner = new ProxySelectorRoutePlanner(
				httpClient.connectionManager.schemeRegistry,
				ProxySelector.default
		)
	}

	void 'post body is stored on tape'() {
		given:
		def postBody = '{"foo":"bar"}'
		def httpPost = new HttpPost('http://httpbin.org/post')
		httpPost.setHeader('Content-Type', 'application/json')
		def reqEntity = new StringEntity(postBody, 'UTF-8')
		reqEntity.setContentType('application/json')
		httpPost.entity = reqEntity

		and:
		recorder.startProxy('post_body_spec', [mode: WRITE_ONLY])

		when:
		httpClient.execute(httpPost)

		and:
		recorder.stopProxy()

		then:
		def file = new File(tapeRoot, 'post_body_spec.yaml')
		def tapeData = file.withReader {
			new Yaml().loadAs(it, Map)
		}
		tapeData.interactions[0].request.body == postBody
	}

}
