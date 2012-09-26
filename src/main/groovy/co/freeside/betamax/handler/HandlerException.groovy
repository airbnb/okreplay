package co.freeside.betamax.handler

/**
 * Thrown to indicates an exception with some part of the handling chain. The HTTP status that should be returned to the
 * client is specified.
 */
class HandlerException extends RuntimeException {

	final int httpStatus

	HandlerException(int httpStatus, String message) {
		super(message)
		this.httpStatus = httpStatus
	}

	HandlerException(int httpStatus, String message, Throwable cause) {
		super(message, cause)
		this.httpStatus = httpStatus
	}
}
