// ==================== Validador de Sucursal y Terminal ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class SucursalTerminalValidator extends BaseValidationRule {
    
    @Override
    protected void doValidate(FacturaRequestDTO request) {
        if (request.situacion() == null || request.situacion().isEmpty()) {
            throw new ValidationException("La situación es requerida.");
        }
        
        if (request.sucursal() == null) {
            throw new ValidationException("La sucursal es requerida.");
        }
        
        if (request.terminal() == null) {
            throw new ValidationException("La terminal es requerida.");
        }
    }
}
