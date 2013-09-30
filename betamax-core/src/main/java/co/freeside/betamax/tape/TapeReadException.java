package co.freeside.betamax.tape;

public class TapeReadException extends RuntimeException {
    public TapeReadException() {
        super();
    }

    public TapeReadException(String message) {
        super(message);
    }

    public TapeReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public TapeReadException(Throwable cause) {
        super(cause);
    }

    protected TapeReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
