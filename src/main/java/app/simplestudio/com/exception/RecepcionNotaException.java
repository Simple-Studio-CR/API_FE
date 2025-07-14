// src/main/java/app/simplestudio/com/service/RecepcionNotaException.java
package app.simplestudio.com.exception;

/**
 * Excepción de negocio al procesar recepción de nota.
 */
public class RecepcionNotaException extends RuntimeException {
    public RecepcionNotaException(String message) {
        super(message);
    }
    public RecepcionNotaException(String message, Throwable cause) {
        super(message, cause);
    }
}