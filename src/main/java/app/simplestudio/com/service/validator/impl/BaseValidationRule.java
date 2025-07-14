// ==================== Validador Base Abstracto ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.service.validator.ValidationRule;

/**
 * Clase base para todos los validadores
 */
public abstract class BaseValidationRule implements ValidationRule {
    
    private ValidationRule nextValidator;
    
    @Override
    public ValidationRule setNext(ValidationRule next) {
        this.nextValidator = next;
        return next;
    }
    
    @Override
    public final void validate(FacturaRequestDTO request) {
        // Ejecutar validación específica
        doValidate(request);
        
        // Continuar con el siguiente validador en la cadena
        if (nextValidator != null) {
            nextValidator.validate(request);
        }
    }
    
    /**
     * Implementación específica de validación para cada validador
     * @param request DTO a validar
     */
    protected abstract void doValidate(FacturaRequestDTO request);
}