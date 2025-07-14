// ==================== DocumentoElectronicoService Interface ====================
package app.simplestudio.com.service;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.dto.DocumentoElectronicoResponse;

/**
 * Servicio principal para procesamiento de documentos electrónicos
 */
public interface DocumentoElectronicoService {

    /**
     * Procesa un documento electrónico desde JSON
     * @param jsonRequest JSON con datos del documento
     * @param flujo Tipo de flujo (RECEPCION, ND_NC_ACEPTADAS, MENSAJE_RECEPTOR)
     * @return Response con resultado del procesamiento
     */
    DocumentoElectronicoResponse procesarDocumento(String jsonRequest, String flujo);

    /**
     * Procesa un documento electrónico desde DTO
     * @param request DTO con datos del documento
     * @return Response con resultado del procesamiento
     */
    DocumentoElectronicoResponse procesarDocumento(FacturaRequestDTO request);
}