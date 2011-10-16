package betamax.examples

import betamax.Betamax
import betamax.Recorder
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
		log.level = Level.ALL
	}

	def setup() {
		def restClient = new RESTClient("http://search.twitter.com/search.json")
		restClient.client.routePlanner = new ProxySelectorRoutePlanner(restClient.client.connectionManager.schemeRegistry, ProxySelector.default)
		service.restClient = restClient
	}

	@Betamax(tape = "twitter success")
	def "returns aggregated twitter client stats when a successful response is received"() {
		when:
		def clients = service.tweetsByClient("betamax")

		then:
		clients.size() == 6
		clients["\u00DCberSocial for BlackBerry"] == 4
		clients["TweetDeck"] == 2
		clients["Echofon"] == 1
		clients["Mobile Web"] == 1
		clients["Snaptu"] == 1
		clients["Twitter for BlackBerry\u00AE"] == 1
	}

	@Betamax(tape = "twitter success")
	def "only retrieves tweets containing the search term"() {
		when:
		def tweets = service.tweets("betamax")

		then:
		tweets.size() == 10
		tweets.every { it.text =~ /(?i)betamax/ }
	}

	@Betamax(tape = "twitter rate limit")
	def "sets an error status when twitter rate limit is exceeded"() {
		when:
		service.tweetsByClient("betamax")

		then:
		thrown TwitterException
	}
}
