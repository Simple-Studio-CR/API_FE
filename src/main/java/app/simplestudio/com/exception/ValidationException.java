// ==================== Excepción Personalizada ====================
package app.simplestudio.com.exception;

/**
 * Excepción para errores de validación de documentos
 */
public class ValidationException extends RuntimeException {
    
    private final int errorCode;
    
    public ValidationException(String message) {
        this(401, message);
    }
    
    public ValidationException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}