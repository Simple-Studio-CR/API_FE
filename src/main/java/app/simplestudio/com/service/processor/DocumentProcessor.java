// ==================== Interface para Strategy Pattern ====================
package app.simplestudio.com.service.processor;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.dto.DocumentoElectronicoResponse;

/**
 * Interface para implementar Strategy Pattern por tipo de documento
 */
public interface DocumentProcessor {

    /**
     * Procesa un documento específico
     * @param request Datos del documento
     * @return Response con resultado del procesamiento
     */
    DocumentoElectronicoResponse procesar(FacturaRequestDTO request);

    /**
     * Indica si este processor puede manejar el tipo de documento
     * @param tipoDocumento Tipo de documento
     * @return true si puede procesar
     */
    boolean puedeProceser(String tipoDocumento);
}