// ==================== Mapper para convertir Factura a CCampoFactura ====================
package app.simplestudio.com.service.mapper;

import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.mh.CCampoFactura;

/**
 * Mapper para convertir entidades Factura al formato requerido por el generador XML
 */
public interface FacturaToXmlMapper {
    
    /**
     * Convierte una Factura a CCampoFactura (formato XML)
     * @param factura Entidad factura
     * @param emisor Entidad emisor
     * @return CCampoFactura para generación de XML
     */
    CCampoFactura mapToXmlFormat(Factura factura, Emisor emisor);
}
