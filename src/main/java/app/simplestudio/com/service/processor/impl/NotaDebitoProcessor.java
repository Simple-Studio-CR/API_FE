// ==================== Processor para Nota de Débito ====================
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

@Component("ndProcessor")
public class NotaDebitoProcessor extends BaseDocumentProcessor {

    public NotaDebitoProcessor(
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
        log.info("Procesamiento específico para Nota de Débito");

        // ND requiere referencia obligatoria al documento original
        validarReferenciaObligatoria(request);

        // Validar que tenga motivo de la nota
        validarMotivoNota(request);

        // Validar montos positivos (es un cargo adicional)
        validarMontosPositivos(request);
    }

    private void validarReferenciaObligatoria(FacturaRequestDTO request) {
        if (request.referencias() == null || request.referencias().isEmpty()) {
            throw new ValidationException("La referencia al documento original es obligatoria para Nota de Débito.");
        }

        // Validar que la referencia tenga formato correcto (clave de 50 dígitos)
        boolean tieneReferenciaValida = request.referencias().stream()
            .anyMatch(ref -> ref.numero() != null && ref.numero().length() == 50);

        if (!tieneReferenciaValida) {
            throw new ValidationException("La Nota de Débito debe referenciar un documento electrónico válido (clave de 50 dígitos).");
        }
    }

    private void validarMotivoNota(FacturaRequestDTO request) {
        if (request.referencias() != null && !request.referencias().isEmpty()) {
            boolean tieneRazon = request.referencias().stream()
                .anyMatch(ref -> ref.razon() != null && !ref.razon().trim().isEmpty());

            if (!tieneRazon) {
                log.warn("Nota de Débito sin razón específica - Considerar agregar motivo de la nota");
            }
        }
    }

    private void validarMontosPositivos(FacturaRequestDTO request) {
        // Validar que los montos sean positivos (es un cargo adicional)
        try {
            if (request.totalComprobante() != null) {
                double total = Double.parseDouble(String.valueOf(request.totalComprobante()));
                if (total <= 0) {
                    throw new ValidationException("El monto total de la Nota de Débito debe ser positivo.");
                }
            }
        } catch (NumberFormatException e) {
            log.warn("No se pudo validar el monto total de la Nota de Débito: {}", request.totalComprobante());
        }
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        return "ND".equals(tipoDocumento);
    }
}