// ==================== Validador de Exoneración ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ExoneracionValidator extends BaseValidationRule {
    
    @Override
    protected void doValidate(FacturaRequestDTO request) {
        BigDecimal totalExonerado = request.totalExonerado();
        
        // Si hay exoneración, validar campos requeridos
        if (totalExonerado != null && totalExonerado.compareTo(BigDecimal.ZERO) > 0) {
            // Aquí se validarían los campos de exoneración si vinieran en el request
            // Como no están en el DTO principal, la validación se hace a nivel de items
            validarExoneracionEnItems(request);
        }
    }
    
    private void validarExoneracionEnItems(FacturaRequestDTO request) {
        if (request.detalleLinea() == null) {
            return;
        }
        
        request.detalleLinea().stream()
            .flatMap(detalle -> detalle.impuestos() != null ? detalle.impuestos().stream() : null)
            .filter(impuesto -> impuesto.exoneracion() != null)
            .forEach(impuesto -> {
                var exoneracion = impuesto.exoneracion();
                
                if (exoneracion.tipoDocumento() == null || exoneracion.tipoDocumento().isEmpty()) {
                    throw new ValidationException("Tipo de documento de exoneración es requerido.");
                }
                
                if (exoneracion.numeroDocumento() == null || exoneracion.numeroDocumento().isEmpty()) {
                    throw new ValidationException("Número de exoneración es requerido.");
                }
                
                if (exoneracion.nombreInstitucion() == null || exoneracion.nombreInstitucion().isEmpty()) {
                    throw new ValidationException("Nombre de institución exonerada es requerido.");
                }
                
                if (exoneracion.fechaEmision() == null || exoneracion.fechaEmision().isEmpty()) {
                    throw new ValidationException("Fecha de emisión de exoneración es requerido.");
                }
                
                if (exoneracion.montoExoneracion() == null || 
                    exoneracion.montoExoneracion().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ValidationException("Monto de impuesto de exoneración es requerido.");
                }
                
                if (exoneracion.porcentajeExoneracion() == null || 
                    exoneracion.porcentajeExoneracion() < 0 || exoneracion.porcentajeExoneracion() > 100) {
                    throw new ValidationException("Porcentaje de exoneración es requerido.");
                }
            });
    }
}