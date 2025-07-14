// ==================== Validador de Receptor ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ReceptorValidator extends BaseValidationRule {
    
    @Override
    protected void doValidate(FacturaRequestDTO request) {
        boolean omitirReceptor = "true".equals(request.omitirReceptor());
        String tipoDocumento = request.tipoDocumento();
        
        // FE y FEC siempre requieren receptor
        if (("FE".equals(tipoDocumento) || "FEC".equals(tipoDocumento)) && omitirReceptor) {
            throw new ValidationException(
                "Los datos del receptor nombre y identificación (tipo y número) son requeridos para " +
                "Factura Electrónica y para Factura Electrónica Compra!!!"
            );
        }
        
        // Si no se omite receptor, validar campos requeridos
        if (!omitirReceptor) {
            if (request.receptorNombre() == null || request.receptorNombre().isEmpty()) {
                throw new ValidationException("El nombre del receptor es requerido!!!");
            }
            
            if (request.receptorTipoIdentif() == null || request.receptorTipoIdentif().isEmpty()) {
                throw new ValidationException("El tipo de identificación es requerido!!!");
            }
            
            if (request.receptorNumIdentif() == null || request.receptorNumIdentif().isEmpty()) {
                throw new ValidationException("El número de identificación es requerido!!!");
            }
        }
        
        // Validaciones específicas para FEC
        if ("FEC".equals(tipoDocumento)) {
            validarReceptorFEC(request);
        }
    }
    
    private void validarReceptorFEC(FacturaRequestDTO request) {
        if (request.receptorProvincia() == null || request.receptorProvincia().isEmpty()) {
            throw new ValidationException("Campo provincia es requerida.");
        }
        
        if (request.receptorCanton() == null || request.receptorCanton().isEmpty()) {
            throw new ValidationException("Campo cantón es requerido.");
        }
        
        if (request.receptorDistrito() == null || request.receptorDistrito().isEmpty()) {
            throw new ValidationException("Campo distrito es requerido.");
        }
        
        if (request.receptorOtrasSenas() == null || request.receptorOtrasSenas().isEmpty()) {
            throw new ValidationException("Campo Otras señas es requerido.");
        }
    }
}
