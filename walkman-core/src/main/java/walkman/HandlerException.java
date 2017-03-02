package walkman;

/**
 * Thrown to indicates an exception with some part of the handling chain.
 */
abstract class HandlerException extends RuntimeException {
  HandlerException(String message) {
    super(message);
  }
}
