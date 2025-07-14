// ==================== Validador de Tipo de Documento ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class TipoDocumentoValidator extends BaseValidationRule {
    
    @Override
    protected void doValidate(FacturaRequestDTO request) {
        String tipoDocumento = request.tipoDocumento();
        
        // Validar tipos de documento permitidos
        if (!isValidTipoDocumento(tipoDocumento)) {
            throw new ValidationException("Tipo de documento no válido: " + tipoDocumento);
        }
        
        // Validaciones específicas por tipo
        switch (tipoDocumento) {
            case "FEC":
                validarFEC(request);
                break;
            case "FEE":
                validarFEE(request);
                break;
            default:
                // Otros tipos no requieren validaciones especiales
                break;
        }
    }
    
    private boolean isValidTipoDocumento(String tipoDocumento) {
        return ("FE".equals(tipoDocumento) || "TE".equals(tipoDocumento) ||
            "NC".equals(tipoDocumento) || "ND".equals(tipoDocumento) ||
            "FEC".equals(tipoDocumento) || "FEE".equals(tipoDocumento));
    }
    
    private void validarFEC(FacturaRequestDTO request) {
        // FEC se utiliza solo para régimen simplificado o sector agropecuario
        // Aquí se pueden agregar validaciones específicas si es necesario
    }
    
    private void validarFEE(FacturaRequestDTO request) {
        // Para FEE no se puede aplicar exoneración
        if (request.totalExonerado() != null && request.totalExonerado().doubleValue() > 0) {
            throw new ValidationException("La Factura Electrónica de Exportación no se puede exonerar.");
        }
    }
}