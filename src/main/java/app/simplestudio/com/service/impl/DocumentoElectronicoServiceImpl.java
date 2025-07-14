// ==================== DocumentoElectronicoServiceImpl Corregido ====================
package app.simplestudio.com.service.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.dto.DocumentoElectronicoResponse;
import app.simplestudio.com.service.DocumentoElectronicoService;
import app.simplestudio.com.service.processor.DocumentProcessor;
import app.simplestudio.com.service.processor.DocumentProcessorFactory;
import app.simplestudio.com.service.validator.DocumentValidator;
import app.simplestudio.com.service.mapper.JsonToFacturaMapper;
import app.simplestudio.com.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DocumentoElectronicoServiceImpl implements DocumentoElectronicoService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoElectronicoServiceImpl.class);

    private final DocumentProcessorFactory processorFactory;
    private final DocumentValidator documentValidator;
    private final JsonToFacturaMapper jsonMapper;

    public DocumentoElectronicoServiceImpl(
        DocumentProcessorFactory processorFactory,
        DocumentValidator documentValidator,
        JsonToFacturaMapper jsonMapper) {
        this.processorFactory = processorFactory;
        this.documentValidator = documentValidator;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public DocumentoElectronicoResponse procesarDocumento(String jsonRequest, String flujo) {
        try {
            log.info("Procesando documento JSON - Flujo: {}", flujo);

            // 1. Convertir JSON a DTO
            FacturaRequestDTO request = jsonMapper.mapFromJson(jsonRequest);

            // 2. Aplicar contexto del flujo si es necesario
            request = aplicarContextoFlujo(request, flujo);

            // 3. Procesar usando el método principal
            return procesarDocumento(request);

        } catch (ValidationException e) {
            log.warn("Error de validación en flujo {}: {}", flujo, e.getMessage());
            return DocumentoElectronicoResponse.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error procesando JSON en flujo {}: {}", flujo, e.getMessage(), e);
            return DocumentoElectronicoResponse.error(500, "Error interno del servidor: " + e.getMessage());
        }
    }

    @Override
    public DocumentoElectronicoResponse procesarDocumento(FacturaRequestDTO request) {
        try {
            log.info("Iniciando procesamiento de documento tipo: {}", request.tipoDocumento());

            // 1. Validaciones específicas del documento
            documentValidator.validarDocumento(request);

            // 2. Obtener processor específico para el tipo de documento
            DocumentProcessor processor = processorFactory.getProcessor(request.tipoDocumento());

            // 3. Procesar documento
            return processor.procesar(request);

        } catch (ValidationException e) {
            log.warn("Error de validación: {}", e.getMessage());
            return DocumentoElectronicoResponse.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error procesando documento tipo {}: {}", request.tipoDocumento(), e.getMessage(), e);
            return DocumentoElectronicoResponse.error(500,
                "Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Aplica contexto específico según el flujo de procesamiento
     */
    private FacturaRequestDTO aplicarContextoFlujo(FacturaRequestDTO request, String flujo) {
        switch (flujo) {
            case "ND_NC_ACEPTADAS":
                // Para este flujo, validar que sea ND o NC
                if (!"ND".equals(request.tipoDocumento()) && !"NC".equals(request.tipoDocumento())) {
                    throw new ValidationException("Este endpoint solo acepta Notas de Débito (ND) y Notas de Crédito (NC)");
                }
                break;

            case "MENSAJE_RECEPTOR":
                // Para mensajes de receptor, validar que tenga los campos necesarios
                if (request.claveDocumentoEmisor() == null || request.claveDocumentoEmisor().isEmpty()) {
                    throw new ValidationException("La clave del documento emisor es requerida para mensajes de receptor");
                }
                break;

            case "RECEPCION":
                // Flujo general, no requiere validaciones adicionales
                break;

            default:
                log.warn("Flujo desconocido: {}", flujo);
        }

        return request;
    }
}