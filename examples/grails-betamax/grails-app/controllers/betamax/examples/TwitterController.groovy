package betamax.examples

import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE

class TwitterController {

	def twitterService

	def index() {
		def q = params.q ?: "betamax"
		try {
			def clients = twitterService.tweetsByClient(q)
			[q: q, clients: clients]
		} catch (TwitterException e) {
			render status: SC_SERVICE_UNAVAILABLE, text: e.message
		}
	}

}
