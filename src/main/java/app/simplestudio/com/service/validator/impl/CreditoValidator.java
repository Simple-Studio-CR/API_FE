// ==================== Validador de Crédito ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreditoValidator extends BaseValidationRule {

    @Override
    protected void doValidate(FacturaRequestDTO request) {
        String condVenta = request.condVenta();

        // Si es venta a crédito (condición 2)
        if ("2".equals(condVenta)) {
            // Validar plazo de crédito
            if (request.plazoCredito() == null || request.plazoCredito() <= 0) {
                throw new ValidationException(
                    "Plazo de Crédito no puede ser igual a \"0\" o nulo.");
            }

            // Para crédito se requiere receptor (excepto casos especiales)
            boolean omitirReceptor = "true".equals(request.omitirReceptor());
            if (omitirReceptor) {
                throw new ValidationException(
                    "Debe completar el receptor para poder emitir la factura a crédito");
            }
        }
    }
}