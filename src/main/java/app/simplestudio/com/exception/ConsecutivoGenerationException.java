// ==================== Excepción Custom para Consecutivos ====================
package app.simplestudio.com.exception;

public class ConsecutivoGenerationException extends RuntimeException {
    
    public ConsecutivoGenerationException(String message) {
        super(message);
    }
    
    public ConsecutivoGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
