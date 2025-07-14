// ==================== Service para Consecutivos ====================
package app.simplestudio.com.service;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.models.entity.Emisor;

public interface ConsecutivoService {
    
    /**
     * Genera el próximo consecutivo para el documento
     */
    String generarConsecutivo(FacturaRequestDTO request, Emisor emisor);
    
    /**
     * Genera la clave única del documento
     */
    String generarClave(FacturaRequestDTO request, Emisor emisor, String consecutivo);
}