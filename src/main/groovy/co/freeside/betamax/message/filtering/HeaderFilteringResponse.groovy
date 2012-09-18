package co.freeside.betamax.message.filtering

import co.freeside.betamax.message.Message
import co.freeside.betamax.message.Response

class HeaderFilteringResponse extends HeaderFilteringMessage implements Response {

	@Delegate private final Response response

	HeaderFilteringResponse(Response response) {
		this.response = response
	}

	protected Message getDelegate() {
		response
	}

}
