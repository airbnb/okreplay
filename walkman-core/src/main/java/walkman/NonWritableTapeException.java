package walkman;

public class NonWritableTapeException extends HandlerException {
  NonWritableTapeException() {
    super("Tape is not writable");
  }
}
