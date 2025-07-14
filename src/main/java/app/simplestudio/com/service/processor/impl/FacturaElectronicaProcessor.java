// ==================== Processor para Factura Electrónica ====================
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

@Component("feProcessor")
public class FacturaElectronicaProcessor extends BaseDocumentProcessor {

    public FacturaElectronicaProcessor(
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
        log.info("Procesamiento específico para Factura Electrónica");

        // FE requiere datos completos del receptor
        validarReceptorObligatorio(request);

        // Validar que no se omita receptor para FE
        if ("true".equalsIgnoreCase(request.omitirReceptor())) {
            throw new ValidationException(
                "Los datos del receptor nombre y identificación (tipo y número) son requeridos para Factura Electrónica!!!");
        }
    }

    private void validarReceptorObligatorio(FacturaRequestDTO request) {
        if (request.receptorNombre() == null || request.receptorNombre().isEmpty()) {
            throw new ValidationException("El nombre del receptor es requerido para FE!!!");
        }

        if (request.receptorTipoIdentif() == null || request.receptorTipoIdentif().isEmpty()) {
            throw new ValidationException("El tipo de identificación es requerido para FE!!!");
        }

        if (request.receptorNumIdentif() == null || request.receptorNumIdentif().isEmpty()) {
            throw new ValidationException("El número de identificación es requerido para FE!!!");
        }
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        return "FE".equals(tipoDocumento);
    }
}

