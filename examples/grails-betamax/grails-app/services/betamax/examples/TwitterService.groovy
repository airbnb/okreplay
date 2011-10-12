package betamax.examples

import groovy.transform.InheritConstructors
import net.sf.json.JSONArray
import org.apache.commons.lang.StringEscapeUtils
import groovyx.net.http.*

class TwitterService {

    static transactional = false

	RESTClient restClient

	Map<String, Integer> tweetsByClient(String q) {
		int resultsPerPage = 10
		int page = 1

		try {
			def response = restClient.get(query: [q: q, rpp: resultsPerPage, page: page], contentType: "application/json")
			def results = JSONArray.toCollection(response.data.results)

			def clients = [:].withDefault { 0 }
			for (result in results) {
				def name = StringEscapeUtils.unescapeHtml(result.source).replaceAll(/<.*?>/, "")
				clients[name]++
			}
			clients
		} catch (HttpResponseException e) {
			throw new TwitterException(e)
		}
	}
}

@InheritConstructors
class TwitterException extends RuntimeException {}
