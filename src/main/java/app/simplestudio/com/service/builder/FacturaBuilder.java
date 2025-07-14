// ==================== Interface del Builder ====================
package app.simplestudio.com.service.builder;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.models.entity.Emisor;

/**
 * Builder para construir entidades Factura desde DTOs
 */
public interface FacturaBuilder {
    
    /**
     * Construye una Factura completa desde el DTO
     * @param request DTO con datos de la factura
     * @param emisor Entidad del emisor
     * @param clave Clave generada del documento
     * @param consecutivo Consecutivo generado
     * @return Factura construida y lista para persistir
     */
    Factura construir(FacturaRequestDTO request, Emisor emisor, String clave, String consecutivo);
}