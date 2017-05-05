package okreplay;

public class NonWritableTapeException extends HandlerException {
  NonWritableTapeException(String message) {
    super(message);
  }

  NonWritableTapeException() {
    super("Tape is not writable");
  }
}
