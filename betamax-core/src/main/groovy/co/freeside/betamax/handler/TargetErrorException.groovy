package co.freeside.betamax.handler

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY

class TargetErrorException extends HandlerException {

	TargetErrorException(uri, Throwable cause) {
		super("Problem connecting to $uri".toString(), cause)
	}

	@Override
	int getHttpStatus() {
		HTTP_BAD_GATEWAY
	}
}
