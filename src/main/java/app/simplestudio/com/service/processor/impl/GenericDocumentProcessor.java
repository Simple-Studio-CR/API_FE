// ==================== Processor Genérico para Casos Especiales ====================
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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Processor genérico para manejar casos especiales o tipos de documento futuros.
 * Se usa como fallback cuando no existe un processor específico para el tipo de documento.
 * Tiene la prioridad más baja para que los processors específicos sean evaluados primero.
 */
@Component("genericProcessor")
@Order(Integer.MAX_VALUE) // Prioridad más baja para ser el último en evaluarse
public class GenericDocumentProcessor extends BaseDocumentProcessor {

    public GenericDocumentProcessor(
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
        log.info("Procesamiento genérico para documento tipo: {}", request.tipoDocumento());

        // Validaciones básicas que aplican a cualquier documento
        validarDatosBasicos(request);

        // Validaciones adicionales según contexto
        validarSegunContexto(request);

        // Log para seguimiento de tipos de documento no implementados específicamente
        log.warn("Usando processor genérico para tipo: {} - Considerar crear processor específico",
            request.tipoDocumento());

        // Notificar para análisis futuro
        notificarUsoGenerico(request);
    }

    private void validarDatosBasicos(FacturaRequestDTO request) {
        // Validaciones que aplican a cualquier tipo de documento

        if (request.totalComprobante() == null) {
            throw new ValidationException("El total del comprobante es requerido.");
        }

        try {
            double total = Double.parseDouble(String.valueOf(request.totalComprobante()));
            if (total <= 0) {
                throw new ValidationException("El total del comprobante debe ser mayor a cero.");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("El total del comprobante debe ser un número válido.");
        }

        if (request.detalleLinea() == null || request.detalleLinea().isEmpty()) {
            throw new ValidationException("El documento debe tener al menos una línea de detalle.");
        }

        // Validar que emisor no esté vacío
        if (request.emisor() == null || request.emisor().trim().isEmpty()) {
            throw new ValidationException("La identificación del emisor es requerida.");
        }

        // Validar token de acceso
        if (request.tokenAccess() == null || request.tokenAccess().trim().isEmpty()) {
            throw new ValidationException("El token de acceso es requerido.");
        }
    }

    private void validarSegunContexto(FacturaRequestDTO request) {
        // Validaciones específicas según el contexto del documento

        // Para documentos que requieren receptor (la mayoría)
        if (requiereReceptor(request.tipoDocumento())) {
            validarReceptorBasico(request);
        }

        // Para documentos que manejan moneda extranjera
        if (manejaMonedaExtranjera(request)) {
            validarMoneda(request);
        }

        // Para documentos que requieren referencias
        if (posiblementeRequiereReferencias(request.tipoDocumento())) {
            validarReferenciasOpcionales(request);
        }
    }

    private boolean requiereReceptor(String tipoDocumento) {
        // La mayoría de documentos requieren receptor, excepto algunos como TE
        return !"TE".equals(tipoDocumento) &&
            !"omitir".equalsIgnoreCase(String.valueOf(tipoDocumento));
    }

    private boolean manejaMonedaExtranjera(FacturaRequestDTO request) {
        return request.codMoneda() != null &&
            !"CRC".equals(request.codMoneda()) &&
            !request.codMoneda().isEmpty();
    }

    private boolean posiblementeRequiereReferencias(String tipoDocumento) {
        // Tipos que usualmente requieren referencias a otros documentos
        return tipoDocumento.startsWith("N") || // NC, ND
            tipoDocumento.contains("C") ||    // CCE, CPCE, RCE
            tipoDocumento.length() > 2;       // Tipos complejos
    }

    private void validarReceptorBasico(FacturaRequestDTO request) {
        if (!"true".equalsIgnoreCase(request.omitirReceptor())) {
            if (request.receptorNombre() == null || request.receptorNombre().trim().isEmpty()) {
                log.warn("Documento tipo {} sin nombre de receptor - Verificar si es correcto",
                    request.tipoDocumento());
            }

            if (request.receptorTipoIdentif() == null || request.receptorTipoIdentif().trim().isEmpty()) {
                log.warn("Documento tipo {} sin tipo de identificación del receptor",
                    request.tipoDocumento());
            }
        }
    }

    private void validarMoneda(FacturaRequestDTO request) {
        if (request.tipoCambio() == null) {
            log.warn("Documento con moneda extranjera {} sin tipo de cambio", request.codMoneda());
        } else {
            try {
                double tipoCambio = Double.parseDouble(String.valueOf(request.tipoCambio()));
                if (tipoCambio <= 0) {
                    throw new ValidationException("El tipo de cambio debe ser mayor a cero.");
                }
            } catch (NumberFormatException e) {
                throw new ValidationException("Tipo de cambio inválido: " + request.tipoCambio());
            }
        }
    }

    private void validarReferenciasOpcionales(FacturaRequestDTO request) {
        if (request.referencias() == null || request.referencias().isEmpty()) {
            log.info("Documento tipo {} sin referencias - Verificar si es correcto",
                request.tipoDocumento());
        } else {
            // Validar formato básico de referencias
            request.referencias().forEach(ref -> {
                if (ref.numero() != null && ref.numero().length() == 50) {
                    log.info("Referencia a documento electrónico detectada: {}",
                        ref.numero().substring(21, 41)); // Mostrar solo consecutivo
                }
            });
        }
    }

    private void notificarUsoGenerico(FacturaRequestDTO request) {
        // Esto podría enviarse a un sistema de métricas o logging especial
        // para analizar qué tipos de documento necesitan processors específicos

        log.info("METRICA: Uso de processor genérico - Tipo: {}, Emisor: {}, Total: {}",
            request.tipoDocumento(),
            request.emisor().substring(0, Math.min(4, request.emisor().length())) + "***", // Parcialmente oculto
            request.totalComprobante());

        // Aquí se podría enviar a un sistema de métricas como Micrometer
        // meterRegistry.counter("generic.processor.usage", "tipo", request.tipoDocumento()).increment();
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        // Este processor puede manejar cualquier tipo no implementado específicamente
        // Pero debería ser el último en evaluarse (fallback)
        return true;
    }

    /**
     * Método de utilidad para verificar si existe un processor específico
     * para un tipo de documento dado
     */
    public boolean esProcessorGenerico() {
        return true;
    }
}