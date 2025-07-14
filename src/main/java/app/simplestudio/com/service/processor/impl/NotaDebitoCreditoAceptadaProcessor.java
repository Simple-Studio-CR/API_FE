// ==================== FacturaRequestDTO con Builder ====================
// Nota: El DTO actual necesitaría un builder pattern, o usar el record como está
// Si usas record, el mapeo sería directo con el constructor

// ==================== Processor para ND/NC Aceptadas ====================
package app.simplestudio.com.service.processor.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.service.ConsecutivoService;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IFacturaService;
import app.simplestudio.com.service.builder.FacturaBuilder;
import app.simplestudio.com.service.generator.XmlGeneratorService;
import app.simplestudio.com.service.processor.BaseDocumentProcessor;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component("ndNcAceptadasProcessor")
public class NotaDebitoCreditoAceptadaProcessor extends BaseDocumentProcessor {

    private final IFacturaService facturaService;

    public NotaDebitoCreditoAceptadaProcessor(
        IEmisorService emisorService,
        IFacturaService facturaService,
        IComprobantesElectronicosService comprobantesService,
        ConsecutivoService consecutivoService,
        FacturaBuilder facturaBuilder,
        XmlGeneratorService xmlGeneratorService) {
        super(emisorService, facturaService, comprobantesService,
            consecutivoService, facturaBuilder, xmlGeneratorService);
        this.facturaService = facturaService;
    }

    @Override
    protected void procesarEspecifico(FacturaRequestDTO request,
        Emisor emisor,
        String clave,
        String consecutivo) {
        log.info("Procesamiento específico para ND/NC Aceptadas");

        // Validar que solo acepta ND y NC
        if (!("ND".equals(request.tipoDocumento()) || "NC".equals(request.tipoDocumento()))) {
            throw new ValidationException("Solo se aceptan Notas de Débito y Notas de Crédito aceptadas por Hacienda.");
        }

        // Obtener la clave del documento de referencia
        String claveDocumentoReferencia = obtenerClaveReferencia(request);
        
        // Buscar la factura original
        Factura facturaOriginal = facturaService.findFacturaByClave(claveDocumentoReferencia);
        
        if (facturaOriginal == null) {
            throw new ValidationException("La clave " + claveDocumentoReferencia + " no existe en nuestro sistema.");
        }

        // Aquí se podría copiar datos de la factura original si es necesario
        // O validar que los datos sean consistentes
        validarConsistenciaConOriginal(request, facturaOriginal);
    }

    private String obtenerClaveReferencia(FacturaRequestDTO request) {
        // Extraer clave de diferentes posibles ubicaciones en el JSON
        if (request.referencias() != null && !request.referencias().isEmpty()) {
            return request.referencias().get(0).numero();
        }
        
        // Si viene en campo "numero" directamente
        if (request.clave() != null && request.clave().length() == 50) {
            return request.clave();
        }
        
        throw new ValidationException("No se encontró referencia al documento original.");
    }

    private void validarConsistenciaConOriginal(FacturaRequestDTO request, Factura facturaOriginal) {
        // Validaciones adicionales entre el documento nuevo y el original
        log.debug("Validando consistencia con factura original: {}", facturaOriginal.getClave());
        
        // Aquí se pueden agregar validaciones específicas según reglas de negocio
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        return "ND".equals(tipoDocumento) || "NC".equals(tipoDocumento);
    }
}