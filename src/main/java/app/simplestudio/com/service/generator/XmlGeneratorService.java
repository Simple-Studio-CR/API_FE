// ==================== Service para Generación de XML ====================
package app.simplestudio.com.service.generator;

import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.models.entity.Emisor;

public interface XmlGeneratorService {
    
    /**
     * Genera el XML del documento y lo firma digitalmente
     * @param factura Datos de la factura
     * @param emisor Datos del emisor
     * @return Nombre del archivo XML firmado
     */
    String generarYFirmarXml(Factura factura, Emisor emisor);
}