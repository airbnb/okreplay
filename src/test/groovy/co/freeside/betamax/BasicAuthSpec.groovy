package co.freeside.betamax

import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import groovyx.net.http.RESTClient
import org.apache.http.auth.*
import org.junit.Rule
import spock.lang.*
import static org.apache.http.auth.AuthScope.ANY

class BasicAuthSpec extends Specification {

	static final URL endpoint = 'http://httpbin.org/basic-auth/user/passwd'.toURL()

	@Shared @AutoCleanup('deleteDir') File tapeRoot = new File(System.properties.'java.io.tmpdir', 'tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	RESTClient http = new BetamaxRESTClient()

	@Betamax(tape = 'basic auth')
	void 'can record response from authenticated endpoint'() {
		given:
		http.client.credentialsProvider.setCredentials(ANY, new UsernamePasswordCredentials('user', 'passwd'))

		when:
		def response = http.get(uri: endpoint)

		then:
		response.data.authenticated
		response.data.user == 'user'
	}

}
