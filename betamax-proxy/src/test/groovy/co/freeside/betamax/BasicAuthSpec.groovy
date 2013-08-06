package co.freeside.betamax

import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import org.apache.http.HttpHost
import org.apache.http.auth.*
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.MatchRule.*
import static co.freeside.betamax.TapeMode.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.*
import static java.util.concurrent.TimeUnit.SECONDS

@Unroll
@Stepwise
@IgnoreIf({
	def url = "http://httpbin.org/".toURL()
	try {
		HttpURLConnection connection = url.openConnection()
		connection.requestMethod = "HEAD"
		connection.connectTimeout = SECONDS.toMillis(2)
		connection.connect()
		return connection.responseCode >= 400
	} catch (IOException e) {
		System.err.println "Skipping spec as $url is not available"
		return true
	}
})
class BasicAuthSpec extends Specification {

	@Shared endpoint = 'http://httpbin.org/basic-auth/user/passwd'.toURL()
	@Shared targetHost = new HttpHost(endpoint.host, endpoint.port)

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot)
	def http = new BetamaxRESTClient()

	void setup() {
		http.handler[HTTP_UNAUTHORIZED] = { resp -> resp }
	}

	@Betamax(tape = 'basic auth', mode = WRITE_ONLY, match = [method, uri, headers])
	void 'can record #status response from authenticated endpoint'() {
		given:
		http.client.credentialsProvider.setCredentials(new AuthScope(targetHost), credentials)

		when:
		def response = http.get(uri: endpoint)

		then:
		response.status == status
		response.getFirstHeader(X_BETAMAX).value == 'REC'

		where:
		password    | status
		'passwd'    | HTTP_OK
		'INCORRECT' | HTTP_UNAUTHORIZED

		credentials = new UsernamePasswordCredentials('user', password)
	}

	@Betamax(tape = 'basic auth', mode = READ_ONLY, match = [method, uri, headers])
	void 'can play back #status response from authenticated endpoint'() {
		given:
		http.client.credentialsProvider.setCredentials(new AuthScope(targetHost), credentials)

		when:
		def response = http.get(uri: endpoint)

		then:
		response.status == status
		response.getFirstHeader(X_BETAMAX).value == 'PLAY'

		where:
		password    | status
		'passwd'    | HTTP_OK
		'INCORRECT' | HTTP_UNAUTHORIZED

		credentials = new UsernamePasswordCredentials('user', password)
	}

}
