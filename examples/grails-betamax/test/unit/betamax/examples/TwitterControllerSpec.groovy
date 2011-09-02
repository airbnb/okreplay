package betamax.examples

import grails.test.mixin.TestFor
import grails.util.BuildSettingsHolder

import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.junit.Rule
import spock.lang.Specification
import betamax.*
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE

@TestFor(TwitterController)
class TwitterControllerSpec extends Specification {

	File baseDir = BuildSettingsHolder.settings?.baseDir ?: new File("examples/grails-betamax")
	@Rule Recorder recorder = new Recorder(tapeRoot: new File(baseDir, "test/resources/tapes"))

	def setup() {
		def restClient = new RESTClient("http://search.twitter.com/search.json")
		restClient.client.routePlanner = new ProxySelectorRoutePlanner(restClient.client.connectionManager.schemeRegistry, ProxySelector.default)
		controller.restClient = restClient
	}

	@Betamax(tape = "twitter success")
	def "returns aggregated twitter client stats when a successful response is received"() {
		when:
		def model = controller.index()

		then:
		model.q == "betamax"
		model.clients.size() == 6
	}

	@Betamax(tape = "twitter rate limit")
	def "sets an error status when twitter rate limit is exceeded"() {
		when:
		controller.index()

		then:
		controller.response.status == SC_SERVICE_UNAVAILABLE
	}
}
