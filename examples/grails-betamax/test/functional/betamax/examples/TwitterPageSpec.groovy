package betamax.examples

import com.gargoylesoftware.htmlunit.ProxyConfig
import geb.spock.GebSpec
import grails.util.BuildSettingsHolder
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.junit.Rule
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import betamax.*

class TwitterPageSpec extends GebSpec {

	File baseDir = BuildSettingsHolder.settings?.baseDir ?: new File("examples/grails-betamax")
	@Rule Recorder recorder = new Recorder(tapeRoot: new File(baseDir, "test/resources/tapes"), ignoreLocalhost: true)

	def setupSpec() {
		def restClient = ApplicationHolder.application.mainContext.restClient
		restClient.client.routePlanner = new ProxySelectorRoutePlanner(restClient.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	def setup() {
		browser.baseUrl = "http://localhost:8080/grails-betamax/"
		HtmlUnitDriver driver = browser.driver
		def proxyConfig = new ProxyConfig("localhost", 5555)
		proxyConfig.addHostsToProxyBypass("localhost")
		driver.webClient.proxyConfig = proxyConfig
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

	@Betamax(tape = "twitter success")
	def "can follow a link to a twitter user"() {
		given:
		go "twitter"

		when:
		$('#tweets li').eq(0).find('small a').click()

		then:
		title == "Christine Romero (@la_dyosa) on Twitter"
	}

}
