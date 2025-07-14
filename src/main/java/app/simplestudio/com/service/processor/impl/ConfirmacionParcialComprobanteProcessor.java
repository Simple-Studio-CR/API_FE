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

// Confirmación Parcial Comprobante Electrónico
@Component("cpceProcessor")
public class ConfirmacionParcialComprobanteProcessor extends BaseDocumentProcessor {

    public ConfirmacionParcialComprobanteProcessor(
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
        log.info("Procesamiento específico para Confirmación Parcial Comprobante Electrónico");

        // CPCE es mensaje tipo 2 - Aceptación parcial
        validarDatosConfirmacionParcial(request);
    }

    private void validarDatosConfirmacionParcial(FacturaRequestDTO request) {
        if (request.receptorNumIdentif() == null || request.receptorNumIdentif().isEmpty()) {
            throw new ValidationException("El número de cédula del emisor es requerido para CPCE.");
        }

        if (request.clave() == null || request.clave().length() != 50) {
            throw new ValidationException("La clave del documento original es requerida para CPCE.");
        }

        if (request.detalleLinea() == null || request.detalleLinea().isEmpty()) {
            throw new ValidationException("El detalle del mensaje es requerido para CPCE.");
        }
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        return "CPCE".equals(tipoDocumento);
    }
}
