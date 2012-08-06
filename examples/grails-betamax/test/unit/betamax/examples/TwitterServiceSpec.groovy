package betamax.examples

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import grails.plugin.spock.UnitSpec
import grails.util.BuildSettingsHolder
import groovyx.net.http.RESTClient
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.junit.Rule

class TwitterServiceSpec extends UnitSpec {

	File baseDir = BuildSettingsHolder.settings?.baseDir ?: new File("examples/grails-betamax")
	@Rule Recorder recorder = new Recorder(tapeRoot: new File(baseDir, "test/resources/tapes"))

	TwitterService service = new TwitterService()

	def setupSpec() {
		def log = Logger.getLogger("betamax")
		log.addHandler(new ConsoleHandler())
	}

	def setup() {
		def restClient = new RESTClient()
		restClient.client.routePlanner = new ProxySelectorRoutePlanner(restClient.client.connectionManager.schemeRegistry, ProxySelector.default)
		service.restClient = restClient
	}

	@Betamax(tape = "twitter success")
	def "returns aggregated twitter client stats when a successful response is received"() {
		when:
		def clients = service.tweetsByClient("#gr8conf")

		then:
		clients.size() == 6
		clients["Twitterrific"] == 1
		clients["Echofon"] == 1
		clients["Tweetbot for Mac"] == 1
		clients["Twitter for Mac"] == 5
		clients["Twitter for iPhone"] == 1
		clients["Twitter for Android"] == 1
	}

	@Betamax(tape = "twitter success")
	def "only retrieves tweets containing the search term"() {
		when:
		def tweets = service.tweets("#gr8conf")

		then:
		tweets.size() == 10
		tweets.every { it.text =~ /(?i)#gr8conf/ }
	}

	@Betamax(tape = "twitter rate limit")
	def "sets an error status when twitter rate limit is exceeded"() {
		when:
		service.tweetsByClient("#gr8conf")

		then:
		thrown TwitterException
	}
}
