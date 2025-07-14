// ==================== Implementación Base del Validator ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.service.validator.DocumentValidator;
import app.simplestudio.com.service.validator.ValidationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DocumentValidatorImpl implements DocumentValidator {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentValidatorImpl.class);
    
    private final ValidationRule validationChain;
    
    public DocumentValidatorImpl(
            SucursalTerminalValidator sucursalTerminalValidator,
            ReceptorValidator receptorValidator,
            CreditoValidator creditoValidator,
            TipoDocumentoValidator tipoDocumentoValidator,
            MonedaValidator monedaValidator,
            PartidaArancelariaValidator partidaArancelariaValidator,
            ExoneracionValidator exoneracionValidator,
            ExtranjerValidator extranjerValidator,
            DetalleLineasValidator detalleLineasValidator
    ) {
        // Construir cadena de validadores
        this.validationChain = sucursalTerminalValidator
            .setNext(receptorValidator)
            .setNext(creditoValidator)
            .setNext(tipoDocumentoValidator)
            .setNext(monedaValidator)
            .setNext(partidaArancelariaValidator)
            .setNext(exoneracionValidator)
            .setNext(extranjerValidator)
            .setNext(detalleLineasValidator);
    }
    
    @Override
    public void validarDocumento(FacturaRequestDTO request) {
        log.debug("Iniciando validación de documento tipo: {}", request.tipoDocumento());
        
        try {
            validationChain.validate(request);
            log.debug("Validación completada exitosamente para documento tipo: {}", request.tipoDocumento());
        } catch (Exception e) {
            log.error("Error en validación de documento tipo {}: {}", request.tipoDocumento(), e.getMessage());
            throw e;
        }
    }
}