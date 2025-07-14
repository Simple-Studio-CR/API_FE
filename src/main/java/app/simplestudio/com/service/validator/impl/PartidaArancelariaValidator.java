// ==================== Validador de Partida Arancelaria ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.dto.DetalleLineaDTO;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class PartidaArancelariaValidator extends BaseValidationRule {
    
    // Unidades de medida que no requieren partida arancelaria
    private static final Set<String> UNIDADES_SERVICIOS = Set.of(
        "Al", "Alc", "Cm", "I", "Os", "Sp", "Spe", "St", "d", "h", "s"
    );
    
    @Override
    protected void doValidate(FacturaRequestDTO request) {
        // Solo FEE requiere partida arancelaria para mercancías
        if (!"FEE".equals(request.tipoDocumento())) {
            return;
        }
        
        if (request.detalleLinea() == null || request.detalleLinea().isEmpty()) {
            return;
        }
        
        for (DetalleLineaDTO detalle : request.detalleLinea()) {
            String unidadMedida = detalle.unidadMedida();
            
            // Si no es unidad de servicio, debe tener partida arancelaria
            if (!UNIDADES_SERVICIOS.contains(unidadMedida)) {
                if (detalle.partidaArancelaria() == null || detalle.partidaArancelaria().isEmpty()) {
                    throw new ValidationException(
                        "Es requerido completar la partida arancelaria para las mercancías."
                    );
                }
            }
        }
    }
}