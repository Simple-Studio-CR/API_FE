package app.simplestudio.com.service.processor.impl;// ==================== Processors para Mensajes de Receptor ====================

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.exception.ValidationException;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.ConsecutivoService;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IFacturaService;
import app.simplestudio.com.service.builder.FacturaBuilder;
import app.simplestudio.com.service.generator.XmlGeneratorService;
import app.simplestudio.com.service.processor.BaseDocumentProcessor;
import org.springframework.stereotype.Component;

// Confirmación Comprobante Electrónico
@Component("cceProcessor")
public class ConfirmacionComprobanteProcessor extends BaseDocumentProcessor {

    public ConfirmacionComprobanteProcessor(
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
        log.info("Procesamiento específico para Confirmación Comprobante Electrónico");

        // CCE es mensaje tipo 1 - Aceptación total
        validarDatosConfirmacion(request);
    }

    private void validarDatosConfirmacion(FacturaRequestDTO request) {
        if (request.receptorNumIdentif() == null || request.receptorNumIdentif().isEmpty()) {
            throw new ValidationException("El número de cédula del emisor es requerido para CCE.");
        }

        if (request.clave() == null || request.clave().length() != 50) {
            throw new ValidationException("La clave del documento original es requerida para CCE.");
        }
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        return "CCE".equals(tipoDocumento);
    }
}