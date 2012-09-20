package betamax.examples

import grails.test.mixin.TestFor
import spock.lang.Specification

import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE

@TestFor(TwitterController)
class TwitterControllerSpec extends Specification {

	TwitterService twitterService = Mock(TwitterService)
	
	def setup() {
		controller.twitterService = twitterService
	}
	
	def "default query is '#gr8conf'"() {
		when:
		controller.clients()
		
		then:
		1 * twitterService.tweetsByClient("#gr8conf")
	}
	
	def "passes query and results to view"() {
		given:
		def results = [web: 2, iPhone: 4, android: 1]
		twitterService.tweetsByClient("#gr8conf") >> results
		
		when:
		def model = controller.clients()
		
		then:
		model.q == "#gr8conf"
		model.clients == results
	}
	
	def "handles twitter error"() {
		given:
		twitterService.tweetsByClient("#gr8conf") >> { throw new TwitterException('Fail Whale!') }
		
		when:
		controller.clients()
		
		then:
		response.status == SC_SERVICE_UNAVAILABLE
	}
	
}