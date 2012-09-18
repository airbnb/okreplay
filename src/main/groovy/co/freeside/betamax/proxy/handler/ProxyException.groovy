package co.freeside.betamax.proxy.handler

/**
 * Thrown to indicates an exception with some part of the proxy handling chain. The HTTP status that should be returned
 * to the client is specified.
 */
class ProxyException extends RuntimeException {

	final int httpStatus

	ProxyException(int httpStatus, String message) {
		super(message)
		this.httpStatus = httpStatus
	}

	ProxyException(int httpStatus, String message, Throwable cause) {
		super(message, cause)
		this.httpStatus = httpStatus
	}
}
