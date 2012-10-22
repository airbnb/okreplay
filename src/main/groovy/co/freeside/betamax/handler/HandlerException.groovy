package co.freeside.betamax.handler

import groovy.transform.InheritConstructors

/**
 * Thrown to indicates an exception with some part of the handling chain.
 */
@InheritConstructors
abstract class HandlerException extends RuntimeException {

	abstract int getHttpStatus()

}
