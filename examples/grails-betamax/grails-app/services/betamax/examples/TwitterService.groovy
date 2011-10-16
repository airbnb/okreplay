package betamax.examples

import groovy.transform.InheritConstructors
import org.apache.commons.lang.StringEscapeUtils
import groovyx.net.http.*
import net.sf.json.*

class TwitterService {

    static transactional = false

	RESTClient restClient

	Map<String, Integer> tweetsByClient(String q) {
		def results = searchTwitter(q)
		def clients = [:].withDefault { 0 }
		for (result in results) {
			def name = StringEscapeUtils.unescapeHtml(result.source).replaceAll(/<.*?>/, "")
			clients[name]++
		}
		clients
	}

	List<String> tweets(String q) {
		def results = searchTwitter(q)
		results.collect { [user: it.from_user, text: it.text] }
	}

	private Collection<JSONObject> searchTwitter(String q) {
		int resultsPerPage = 10
		int page = 1

		try {
			def response = restClient.get(query: [q: q, rpp: resultsPerPage, page: page], contentType: "application/json")
			JSONArray.toCollection(response.data.results)
		} catch (HttpResponseException e) {
			throw new TwitterException(e)
		}
	}

}

@InheritConstructors
class TwitterException extends RuntimeException {}
