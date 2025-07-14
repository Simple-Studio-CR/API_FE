
// ==================== Processor para Factura Electrónica de Exportación ====================
package app.simplestudio.com.service.processor.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.ConsecutivoService;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IFacturaService;
import app.simplestudio.com.service.builder.FacturaBuilder;
import app.simplestudio.com.service.generator.XmlGeneratorService;
import app.simplestudio.com.service.processor.BaseDocumentProcessor;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component("feeProcessor")
public class FacturaElectronicaExportacionProcessor extends BaseDocumentProcessor {

    public FacturaElectronicaExportacionProcessor(
        IEmisorService emisorService,
        IFacturaService facturaService,
        IComprobantesElectronicosService comprobantesService,
        ConsecutivoService consecutivoService,
        FacturaBuilder facturaBuilder,
        XmlGeneratorService xmlGeneratorService) {
        super(emisorService, facturaService, comprobantesService,
            consecutivoService, facturaBuilder, xmlGeneratorService);
    }

    @Override
    protected void procesarEspecifico(FacturaRequestDTO request,
        Emisor emisor,
        String clave,
        String consecutivo) {
        log.info("Procesamiento específico para Factura Electrónica de Exportación");

        // FEE tiene reglas específicas para exportación
        validarReceptorExtranjero(request);
        validarMonedaExtranjera(request);
        validarExoneraciones(request);
    }

    private void validarReceptorExtranjero(FacturaRequestDTO request) {
        // Para exportación, el receptor debe ser extranjero
        if ("05".equals(request.receptorTipoIdentif())) {
            // Identificación extranjera - validar campos específicos
            if (request.receptorNumIdentif() == null ||
                request.receptorNumIdentif().isEmpty()) {
                throw new ValidationException("La identificación extranjera es requerida para FEE.");
            }

            if (request.receptorOtrasSenas() == null || request.receptorOtrasSenas().isEmpty()) {
                throw new ValidationException("Las otras señas del extranjero son requeridas para FEE.");
            }
        }
    }

    private void validarMonedaExtranjera(FacturaRequestDTO request) {
        // Para exportación, generalmente se usa moneda extranjera
        if (request.codMoneda() == null || "CRC".equals(request.codMoneda())) {
            log.warn("FEE usando CRC - Verificar si debería usar moneda extranjera (USD, EUR, etc.)");
        }

        if (request.tipoCambio() != null) {
            try {
                double tipoCambio = Double.parseDouble(String.valueOf(request.tipoCambio()));
                if (tipoCambio <= 0) {
                    throw new ValidationException("El tipo de cambio debe ser mayor a cero para FEE.");
                }
            } catch (NumberFormatException e) {
                throw new ValidationException("Tipo de cambio inválido para FEE.");
            }
        }
    }

    private void validarExoneraciones(FacturaRequestDTO request) {
        // Para exportación, generalmente hay exoneraciones de impuestos
        log.info("Procesando FEE - Verificar configuración de exoneraciones para exportación");

        // Aquí se podría validar que los items tengan las exoneraciones correspondientes
        // para productos de exportación
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        return "FEE".equals(tipoDocumento);
    }
}