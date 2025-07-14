package app.simplestudio.com.exception;

/** Cuando hay un problema procesando XML */
public class XmlProcessingException extends ApiException {
    public XmlProcessingException(String message) {
        super(message);
    }
    public XmlProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

