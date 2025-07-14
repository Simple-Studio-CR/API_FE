// ==================== Interface Principal del Validator ====================
package app.simplestudio.com.service.validator;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.exception.ValidationException;

/**
 * Validator principal para documentos electrónicos
 */
public interface DocumentValidator {
    
    /**
     * Valida un documento completo ejecutando todas las validaciones necesarias
     * @param request DTO del documento a validar
     * @throws ValidationException si alguna validación falla
     */
    void validarDocumento(FacturaRequestDTO request);
}