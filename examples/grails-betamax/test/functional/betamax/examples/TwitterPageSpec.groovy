package betamax.examples

import geb.spock.GebSpec
import betamax.Recorder
import org.junit.Rule
import betamax.Betamax
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import grails.util.BuildSettingsHolder

class TwitterPageSpec extends GebSpec {

	File baseDir = BuildSettingsHolder.settings?.baseDir ?: new File("examples/grails-betamax")
	@Rule Recorder recorder = new Recorder(tapeRoot: new File(baseDir, "test/resources/tapes"))

	def setupSpec() {
		def restClient = ApplicationHolder.application.mainContext.restClient
		restClient.client.routePlanner = new ProxySelectorRoutePlanner(restClient.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	def setup() {
		browser.baseUrl = "http://localhost:8080/grails-betamax/"
	}

	@Betamax(tape = "twitter success")
	def "displays list of tweets based on query"() {
		given:
		go "twitter"

		expect:
		title == "Twitter Search Results"

		and:
		$('#tweets li').size() == 10
		$('#tweets li p')*.text().every { it =~ /(?i)betamax/ }
		$('#tweets li').eq(0).find('p').text() == "Haha! RT @iamrhouan: Collector's item! Aylayk! RT @la_dyosa: Haha! Betamax copy pa! :D"
	}

}
