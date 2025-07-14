package app.simplestudio.com.exception;

/** Parámetros inválidos en la petición */
public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message);
    }
}

