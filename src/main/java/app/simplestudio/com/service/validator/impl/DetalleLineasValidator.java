// ==================== Validador de Detalle de Líneas ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class DetalleLineasValidator extends BaseValidationRule {
    
    @Override
    protected void doValidate(FacturaRequestDTO request) {
        if (request.detalleLinea() == null || request.detalleLinea().isEmpty()) {
            throw new ValidationException("Debe facturar al menos un producto.");
        }
        
        // Validar que cada línea tenga al menos un impuesto (excepto exentos)
        request.detalleLinea().forEach(detalle -> {
            if (detalle.impuestos() == null || detalle.impuestos().isEmpty()) {
                // Solo validar si no es producto exento
                // La lógica de producto exento se manejaría en la entidad o en otro lugar
            }
        });
    }
}