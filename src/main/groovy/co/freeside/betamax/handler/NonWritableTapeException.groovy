package co.freeside.betamax.handler

import static java.net.HttpURLConnection.HTTP_FORBIDDEN

class NonWritableTapeException extends HandlerException {

	NonWritableTapeException() {
		super('Tape is not writable')
	}

	@Override
	int getHttpStatus() {
		HTTP_FORBIDDEN
	}
}
