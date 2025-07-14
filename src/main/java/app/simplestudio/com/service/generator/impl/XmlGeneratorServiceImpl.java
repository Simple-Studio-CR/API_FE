// ==================== XmlGeneratorService - Implementación ====================
package app.simplestudio.com.service.generator.impl;

import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.generator.XmlGeneratorService;
import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.mh.IGeneraXml;
import app.simplestudio.com.mh.ISigner;
import app.simplestudio.com.service.mapper.FacturaToXmlMapper;
import app.simplestudio.com.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.Path;

@Service
public class XmlGeneratorServiceImpl implements XmlGeneratorService {
    
    private static final Logger log = LoggerFactory.getLogger(XmlGeneratorServiceImpl.class);
    
    private final IGeneraXml generaXml;
    private final ISigner signer;
    private final FacturaToXmlMapper xmlMapper;
    
    @Value("${path.upload.files.api}")
    private String pathUploadFilesApi;
    
    public XmlGeneratorServiceImpl(IGeneraXml generaXml, 
                                 ISigner signer,
                                 FacturaToXmlMapper xmlMapper) {
        this.generaXml = generaXml;
        this.signer = signer;
        this.xmlMapper = xmlMapper;
    }
    
    @Override
    public String generarYFirmarXml(Factura factura, Emisor emisor) {
        log.debug("Generando XML para clave: {}", factura.getClave());
        
        try {
            // 1. Mapear Factura a CCampoFactura (formato requerido por el generador XML)
            CCampoFactura campoFactura = xmlMapper.mapToXmlFormat(factura, emisor);
            
            // 2. Generar rutas de archivos
            String fullPath = pathUploadFilesApi + "/" + emisor.getIdentificacion() + "/";
            String nameFacturaXml = factura.getClave() + "-factura";
            String nameOutFacturaXml = pathUploadFilesApi + "/" + emisor.getIdentificacion() + "/" + 
                                     factura.getClave() + "-factura-sign";
            
            // 3. Generar XML
            generaXml.generateXml(fullPath, generaXml.GeneraXml(campoFactura), nameFacturaXml);
            log.debug("XML generado: {}.xml", nameFacturaXml);
            
            // 4. Firmar XML
            String certificadoPath = pathUploadFilesApi + "/" + emisor.getIdentificacion() + 
                                   "/cert/" + emisor.getCertificado();
            
            signer.sign(
                Path.of(certificadoPath),
                emisor.getPingApi(),
                Path.of(fullPath + nameFacturaXml + ".xml"),
                Path.of(nameOutFacturaXml + ".xml")
            );
            
            String xmlFirmado = nameFacturaXml + "-sign.xml";
            log.debug("XML firmado exitosamente: {}", xmlFirmado);
            
            return xmlFirmado;
            
        } catch (Exception e) {
            log.error("Error generando/firmando XML para clave {}: {}", 
                     factura.getClave(), e.getMessage(), e);
            throw new ValidationException("Error generando XML: " + e.getMessage());
        }
    }
}