// ==================== Processor para Nota de Crédito ====================
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

@Component("ncProcessor")
public class NotaCreditoProcessor extends BaseDocumentProcessor {

    public NotaCreditoProcessor(
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
        log.info("Procesamiento específico para Nota de Crédito");

        // NC requiere referencia obligatoria al documento original
        validarReferenciaObligatoria(request);

        // Validar que tenga motivo de la nota
        validarMotivoNota(request);
    }

    private void validarReferenciaObligatoria(FacturaRequestDTO request) {
        if (request.referencias() == null || request.referencias().isEmpty()) {
            throw new ValidationException("La referencia al documento original es obligatoria para Nota de Crédito.");
        }

        // Validar que la referencia tenga formato correcto (clave de 50 dígitos)
        boolean tieneReferenciaValida = request.referencias().stream()
            .anyMatch(ref -> ref.numero() != null && ref.numero().length() == 50);

        if (!tieneReferenciaValida) {
            throw new ValidationException("La Nota de Crédito debe referenciar un documento electrónico válido (clave de 50 dígitos).");
        }
    }

    private void validarMotivoNota(FacturaRequestDTO request) {
        // El motivo generalmente viene en el campo "otros" o en las referencias
        if (request.referencias() != null && !request.referencias().isEmpty()) {
            boolean tieneRazon = request.referencias().stream()
                .anyMatch(ref -> ref.razon() != null && !ref.razon().trim().isEmpty());

            if (!tieneRazon) {
                log.warn("Nota de Crédito sin razón específica - Considerar agregar motivo de la nota");
            }
        }
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        return "NC".equals(tipoDocumento);
    }
}