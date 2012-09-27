package co.freeside.betamax.handler

import static java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT

class TargetTimeoutException extends HandlerException {

	TargetTimeoutException(uri, Throwable cause) {
		super("Timed out connecting to $uri".toString(), cause)
	}

	@Override
	int getHttpStatus() {
		HTTP_GATEWAY_TIMEOUT
	}

}
