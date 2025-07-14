package app.simplestudio.com.exception;

/** Error comunicándose con un servicio externo (p.ej. S3, MH) */
public class ExternalServiceException extends ApiException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
