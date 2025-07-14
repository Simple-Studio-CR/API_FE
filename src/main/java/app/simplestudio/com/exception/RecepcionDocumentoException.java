// src/main/java/app/simplestudio/com/exception/RecepcionDocumentoException.java
package app.simplestudio.com.exception;

/**
 * Excepción lanzada cuando ocurre un error en el procesamiento
 * de recepción de comprobantes electrónicos (FE, TE, NC, ND, etc.).
 */
public class RecepcionDocumentoException extends RuntimeException {

    public RecepcionDocumentoException(String message) {
        super(message);
    }

    public RecepcionDocumentoException(String message, Throwable cause) {
        super(message, cause);
    }
}