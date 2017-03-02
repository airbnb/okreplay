package walkman;

public class TapeLoadException extends RuntimeException {
  public TapeLoadException() {
    super();
  }

  public TapeLoadException(String message) {
    super(message);
  }

  public TapeLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  public TapeLoadException(Throwable cause) {
    super(cause);
  }
}
