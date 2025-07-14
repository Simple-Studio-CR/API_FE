package app.simplestudio.com.exception;

/** Cuando algo falla al firmar */
public class SignException extends ApiException {
    public SignException(String message) {
        super(message);
    }
    public SignException(String message, Throwable cause) {
        super(message, cause);
    }
}