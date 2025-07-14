package app.simplestudio.com.service.processor.impl;

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

// Rechazo Comprobante Electrónico
@Component("rceProcessor")
public class RechazoComprobanteProcessor extends BaseDocumentProcessor {

    public RechazoComprobanteProcessor(
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
        log.info("Procesamiento específico para Rechazo Comprobante Electrónico");

        // RCE es mensaje tipo 3 - Rechazo total
        validarDatosRechazo(request);
    }

    private void validarDatosRechazo(FacturaRequestDTO request) {
        if (request.receptorNumIdentif() == null || request.receptorNumIdentif().isEmpty()) {
            throw new ValidationException("El número de cédula del emisor es requerido para RCE.");
        }

        if (request.clave() == null || request.clave().length() != 50) {
            throw new ValidationException("La clave del documento original es requerida para RCE.");
        }

        if (request.detalleLinea() == null || request.detalleLinea().isEmpty()) {
            throw new ValidationException("El detalle del rechazo es obligatorio para RCE.");
        }
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        return "RCE".equals(tipoDocumento);
    }
}