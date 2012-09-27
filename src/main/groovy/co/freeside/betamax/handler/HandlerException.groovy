package co.freeside.betamax.handler

import groovy.transform.InheritConstructors

/**
 * Thrown to indicates an exception with some part of the handling chain. The HTTP status that should be returned to the
 * client is specified.
 */
@InheritConstructors
abstract class HandlerException extends RuntimeException {

	abstract int getHttpStatus()

}
