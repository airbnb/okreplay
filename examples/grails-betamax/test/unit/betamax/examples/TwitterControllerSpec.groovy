package betamax.examples

import grails.plugin.spock.*
import spock.lang.*
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE

class TwitterControllerSpec extends ControllerSpec {

	TwitterService twitterService = Mock(TwitterService)
	
	def setup() {
		controller.twitterService = twitterService
	}
	
	def "default query is 'betamax'"() {
		when:
		controller.clients()
		
		then:
		1 * twitterService.tweetsByClient("betamax")
	}
	
	def "passes query and results to view"() {
		given:
		def results = [web: 2, iPhone: 4, android: 1]
		twitterService.tweetsByClient("betamax") >> results
		
		when:
		def model = controller.clients()
		
		then:
		model.q == "betamax"
		model.clients == results
	}
	
	def "handles twitter error"() {
		given:
		twitterService.tweetsByClient("betamax") >> { throw new TwitterException() }
		
		when:
		controller.clients()
		
		then:
		renderArgs.status == SC_SERVICE_UNAVAILABLE
	}
	
}