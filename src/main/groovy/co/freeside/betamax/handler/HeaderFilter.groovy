package co.freeside.betamax.handler

import co.freeside.betamax.message.*
import co.freeside.betamax.message.filtering.*

class HeaderFilter extends ChainedHttpHandler {

	Response handle(Request request) {
		def filteredRequest = new HeaderFilteringRequest(request)
		def response = chain(filteredRequest)
		new HeaderFilteringResponse(response)
	}

}
