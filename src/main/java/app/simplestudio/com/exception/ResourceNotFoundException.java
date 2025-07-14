package app.simplestudio.com.exception;

/** Recurso no encontrado (p.ej. descarga de XML ausente) */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}