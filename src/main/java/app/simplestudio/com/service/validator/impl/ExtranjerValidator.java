// ==================== Validador de Extranjeros ====================
package app.simplestudio.com.service.validator.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ExtranjerValidator extends BaseValidationRule {

    @Override
    protected void doValidate(FacturaRequestDTO request) {
        String tipoDocumento = request.tipoDocumento();
        String tipoIdentifReceptor = request.receptorTipoIdentif();

        // Validar que extranjeros (tipo 05) no puedan recibir FE o FEC
        if (("FE".equals(tipoDocumento) || "FEC".equals(tipoDocumento)) && "05".equals(
            tipoIdentifReceptor)) {
            throw new ValidationException(
                "A un cliente extranjero NO se le puede emitir Factura Electrónica ni " +
                    "Factura Electrónica de Compra, debe cambiar el tipo de documento, " +
                    "por ejemplo: Tiquete Electrónico."
            );
        }
    }
}