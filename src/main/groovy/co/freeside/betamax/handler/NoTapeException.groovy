package co.freeside.betamax.handler

import static java.net.HttpURLConnection.HTTP_FORBIDDEN

class NoTapeException extends HandlerException {

	NoTapeException() {
		super('No tape')
	}

	@Override
	int getHttpStatus() {
		HTTP_FORBIDDEN
	}
}
