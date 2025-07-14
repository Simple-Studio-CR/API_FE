// ==================== Validador de Moneda ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class MonedaValidator extends BaseValidationRule {
    
    @Override
    protected void doValidate(FacturaRequestDTO request) {
        String codMoneda = request.codMoneda();
        BigDecimal tipoCambio = request.tipoCambio();
        
        if (codMoneda == null || codMoneda.isEmpty()) {
            throw new ValidationException("Debe asignar el código de moneda para continuar");
        }
        
        if (tipoCambio == null || tipoCambio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Debe asignar el tipo de cambio de la moneda para continuar");
        }
        
        // Validar monedas permitidas
        if (!("CRC".equals(codMoneda) || "USD".equals(codMoneda) || "EUR".equals(codMoneda))) {
            throw new ValidationException("Código de moneda no válido: " + codMoneda);
        }
    }
}
