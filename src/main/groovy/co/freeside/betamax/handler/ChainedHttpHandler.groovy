package co.freeside.betamax.handler

import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response

abstract class ChainedHttpHandler implements HttpHandler {

	private HttpHandler next

	protected final Response chain(Request request) {
		if (!next) {
			throw new IllegalStateException('attempted to chain from the last handler in the chain')
		}
		next.handle(request)
	}

	final void setNext(HttpHandler next) {
		this.next = next
	}

	final HttpHandler leftShift(HttpHandler next) {
		setNext(next)
		this.next
	}
}
