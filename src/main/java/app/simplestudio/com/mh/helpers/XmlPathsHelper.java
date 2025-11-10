package app.simplestudio.com.mh.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper para generar los paths de archivos XML
 */
@Component
public class XmlPathsHelper {
    
    @Value("${path.upload.files.api}")
    private String pathUploadFilesApi;
    
    /**
     * Resultado con todos los paths necesarios para los XMLs
     */
    public static class XmlPaths {
        public final String fullPath;
        public final String nameFacturaXml;
        public final String nameOutFacturaXml;
        public final String nameFacturaFirmada;
        
        public XmlPaths(String fullPath, String nameFacturaXml, 
                       String nameOutFacturaXml, String nameFacturaFirmada) {
            this.fullPath = fullPath;
            this.nameFacturaXml = nameFacturaXml;
            this.nameOutFacturaXml = nameOutFacturaXml;
            this.nameFacturaFirmada = nameFacturaFirmada;
        }
    }
    
    /**
     * Genera todos los paths necesarios para facturas y documentos estándar
     * 
     * @param emisor Identificación del emisor
     * @param clave Clave del documento
     * @return XmlPaths con todos los paths generados
     */
    public XmlPaths generarPathsFactura(String emisor, String clave) {
        String fullPath = pathUploadFilesApi + "/" + emisor + "/";
        String nameFacturaXml = clave + "-factura";
        String nameOutFacturaXml = pathUploadFilesApi + "/" + emisor + "/" + clave + "-factura-sign";
        String nameFacturaFirmada = nameFacturaXml + "-sign.xml";
        
        return new XmlPaths(fullPath, nameFacturaXml, nameOutFacturaXml, nameFacturaFirmada);
    }
    
    /**
     * Genera todos los paths necesarios para Mensajes Receptor (MR)
     * 
     * @param emisor Identificación del emisor
     * @param clave Clave del documento
     * @return XmlPaths con todos los paths generados
     */
    public XmlPaths generarPathsMensajeReceptor(String emisor, String clave) {
        String fullPath = pathUploadFilesApi + "/" + emisor + "/";
        String nameFacturaXml = clave + "-mr";
        String nameOutFacturaXml = pathUploadFilesApi + "/" + emisor + "/" + clave + "-mr-sign";
        String nameFacturaFirmada = nameFacturaXml + "-sign.xml";
        
        return new XmlPaths(fullPath, nameFacturaXml, nameOutFacturaXml, nameFacturaFirmada);
    }
    
    /**
     * Genera paths con sufijo personalizado
     * 
     * @param emisor Identificación del emisor
     * @param clave Clave del documento
     * @param sufijo Sufijo para el archivo (ej: "factura", "mr", "nc", etc)
     * @return XmlPaths con todos los paths generados
     */
    public XmlPaths generarPathsConSufijo(String emisor, String clave, String sufijo) {
        String fullPath = pathUploadFilesApi + "/" + emisor + "/";
        String nameFacturaXml = clave + "-" + sufijo;
        String nameOutFacturaXml = pathUploadFilesApi + "/" + emisor + "/" + clave + "-" + sufijo + "-sign";
        String nameFacturaFirmada = nameFacturaXml + "-sign.xml";
        
        return new XmlPaths(fullPath, nameFacturaXml, nameOutFacturaXml, nameFacturaFirmada);
    }
}