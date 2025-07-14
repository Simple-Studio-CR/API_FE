// ==================== Interface para Chain of Responsibility ====================
package app.simplestudio.com.service.validator;

import app.simplestudio.com.dto.FacturaRequestDTO;

/**
 * Interface para validadores específicos usando Chain of Responsibility
 */
public interface ValidationRule {
    
    /**
     * Establece el siguiente validador en la cadena
     * @param next Siguiente validador
     * @return Este validador para encadenamiento fluido
     */
    ValidationRule setNext(ValidationRule next);
    
    /**
     * Ejecuta la validación
     * @param request DTO a validar
     */
    void validate(FacturaRequestDTO request);
}