package co.freeside.betamax.handler

import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response
import co.freeside.betamax.message.filtering.HeaderFilteringRequest
import co.freeside.betamax.message.filtering.HeaderFilteringResponse

class HeaderFilter extends ChainedHttpHandler {

	Response handle(Request request) {
		def filteredRequest = new HeaderFilteringRequest(request)
		def response = chain(filteredRequest)
		new HeaderFilteringResponse(response)
	}

}
