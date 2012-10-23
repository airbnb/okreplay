package co.freeside.betamax.message.filtering

import co.freeside.betamax.message.*

class HeaderFilteringRequest extends HeaderFilteringMessage implements Request {

	@Delegate private final Request request

	HeaderFilteringRequest(Request request) {
		this.request = request
	}

	protected Message getDelegate() {
		request
	}

}
