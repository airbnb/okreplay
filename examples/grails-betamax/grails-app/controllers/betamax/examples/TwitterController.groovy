package betamax.examples

import net.sf.json.JSONArray
import org.apache.commons.lang.StringEscapeUtils
import groovyx.net.http.*
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE

class TwitterController {

	RESTClient restClient

	def index() {
		def q = params.q ?: "betamax"
		int resultsPerPage = 10
		int page = 1

		try {
			def response = restClient.get(query: [q: q, rpp: resultsPerPage, page: page], contentType: "application/json")
			def results = JSONArray.toCollection(response.data.results)

			def clients = results.countBy {
				StringEscapeUtils.unescapeHtml(it.source)
			}

			[q: q, clients: clients]
		} catch (HttpResponseException e) {
			render status: SC_SERVICE_UNAVAILABLE, text: e.message
		}
	}

}
