package app.simplestudio.com.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DocumentStructureUtil {
    
    /**
     * Configuración de documentos XML por tipo
     */
    public static class DocumentConfig {
        private String xmlDeclaration;
        private String rootElementOpen;
        private String rootElementClose;
        private String description;
        
        public DocumentConfig(String xmlDeclaration, String rootElementOpen, String rootElementClose, String description) {
            this.xmlDeclaration = xmlDeclaration;
            this.rootElementOpen = rootElementOpen;
            this.rootElementClose = rootElementClose;
            this.description = description;
        }
        
        public String getXmlDeclaration() { return xmlDeclaration; }
        public String getRootElementOpen() { return rootElementOpen; }
        public String getRootElementClose() { return rootElementClose; }
        public String getDescription() { return description; }
        
        public String getFullHeader() {
            return xmlDeclaration + rootElementOpen;
        }
        
        public String getFullFooter() {
            return rootElementClose;
        }
    }
    
    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    private static final String SCHEMA_BASE = "https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/";
    private static final String XSD_ATTRS = " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
    
    private final Map<String, DocumentConfig> documentConfigs;
    
    public DocumentStructureUtil() {
        documentConfigs = new HashMap<>();
        initializeDocumentConfigs();
    }
    
    /**
     * Inicializa las configuraciones de todos los tipos de documentos
     */
    private void initializeDocumentConfigs() {
        // 01 - Factura Electrónica
        documentConfigs.put("01", new DocumentConfig(
            XML_DECLARATION,
            "<FacturaElectronica" + XSD_ATTRS + " xmlns=\"" + SCHEMA_BASE + "facturaElectronica\">",
            "</FacturaElectronica>",
            "Factura Electrónica"
        ));
        
        // 02 - Nota de Débito Electrónica
        documentConfigs.put("02", new DocumentConfig(
            XML_DECLARATION,
            "<NotaDebitoElectronica" + XSD_ATTRS + " xmlns=\"" + SCHEMA_BASE + "notaDebitoElectronica\">",
            "</NotaDebitoElectronica>",
            "Nota de Débito Electrónica"
        ));
        
        // 03 - Nota de Crédito Electrónica
        documentConfigs.put("03", new DocumentConfig(
            XML_DECLARATION,
            "<NotaCreditoElectronica" + XSD_ATTRS + " xmlns=\"" + SCHEMA_BASE + "notaCreditoElectronica\">",
            "</NotaCreditoElectronica>",
            "Nota de Crédito Electrónica"
        ));
        
        // 04 - Tiquete Electrónico
        documentConfigs.put("04", new DocumentConfig(
            XML_DECLARATION,
            "<TiqueteElectronico" + XSD_ATTRS + " xmlns=\"" + SCHEMA_BASE + "tiqueteElectronico\">",
            "</TiqueteElectronico>",
            "Tiquete Electrónico"
        ));
        
        // 05, 06, 07 - Mensaje Receptor
        DocumentConfig mensajeReceptorConfig = new DocumentConfig(
            XML_DECLARATION,
            "<MensajeReceptor xmlns=\"" + SCHEMA_BASE + "mensajeReceptor\"" + XSD_ATTRS + ">",
            "</MensajeReceptor>",
            "Mensaje Receptor"
        );
        documentConfigs.put("05", mensajeReceptorConfig);
        documentConfigs.put("06", mensajeReceptorConfig);
        documentConfigs.put("07", mensajeReceptorConfig);
        
        // 08 - Factura Electrónica de Compra
        documentConfigs.put("08", new DocumentConfig(
            XML_DECLARATION,
            "<FacturaElectronicaCompra" + XSD_ATTRS + " xmlns=\"" + SCHEMA_BASE + "facturaElectronicaCompra\">",
            "</FacturaElectronicaCompra>",
            "Factura Electrónica de Compra"
        ));
        
        // 09 - Factura Electrónica de Exportación
        documentConfigs.put("09", new DocumentConfig(
            XML_DECLARATION,
            "<FacturaElectronicaExportacion" + XSD_ATTRS + " xmlns=\"" + SCHEMA_BASE + "facturaElectronicaExportacion\">",
            "</FacturaElectronicaExportacion>",
            "Factura Electrónica de Exportación"
        ));
    }
    
    /**
     * Obtiene la configuración del documento por tipo
     */
    public DocumentConfig getDocumentConfig(String tipoDocumento) {
        return documentConfigs.get(tipoDocumento);
    }
    
    /**
     * Obtiene el header completo del documento (XML declaration + root element)
     */
    public String getDocumentHeader(String tipoDocumento) {
        DocumentConfig config = getDocumentConfig(tipoDocumento);
        return config != null ? config.getFullHeader() : "";
    }
    
    /**
     * Obtiene el footer completo del documento (closing root element)
     */
    public String getDocumentFooter(String tipoDocumento) {
        DocumentConfig config = getDocumentConfig(tipoDocumento);
        return config != null ? config.getFullFooter() : "";
    }
    
    /**
     * Obtiene la descripción del tipo de documento
     */
    public String getDocumentDescription(String tipoDocumento) {
        DocumentConfig config = getDocumentConfig(tipoDocumento);
        return config != null ? config.getDescription() : "";
    }
    
    /**
     * Verifica si es un mensaje receptor (tipos 05, 06, 07)
     */
    public boolean isMensajeReceptor(String tipoDocumento) {
        return "05".equals(tipoDocumento) || "06".equals(tipoDocumento) || "07".equals(tipoDocumento);
    }
    
    /**
     * Verifica si es una factura estándar (tipos 01, 07, 04)
     */
    public boolean isFacturaEstandar(String tipoDocumento) {
        return "01".equals(tipoDocumento) || "07".equals(tipoDocumento) || "04".equals(tipoDocumento);
    }
    
    /**
     * Verifica si es factura de exportación (tipo 09)
     */
    public boolean isFacturaExportacion(String tipoDocumento) {
        return "09".equals(tipoDocumento);
    }
    
    /**
     * Verifica si es factura de compra (tipo 08)
     */
    public boolean isFacturaCompra(String tipoDocumento) {
        return "08".equals(tipoDocumento);
    }
    
    /**
     * Obtiene el XML del proveedor del sistema (ContactoDesarrollador)
     */
    public String getContactoDesarrolladorXml() {
        return "<ContactoDesarrollador xmlns=\"https://samyx.digital\">" +
               "<ProveedorSistemaComprobantesElectronicos>" +
               "<Nombre>SamyxFacturador</Nombre>" +
               "<Identificacion>" +
               "<Tipo>01</Tipo>" +
               "<Numero>114970286</Numero>" +
               "</Identificacion>" +
               "<CorreoElectronico>info@snnsoluciones.com</CorreoElectronico>" +
               "</ProveedorSistemaComprobantesElectronicos>" +
               "</ContactoDesarrollador>";
    }
    
    /**
     * Construye XML de "Otros" estándar con contacto de desarrollador
     */
    public String buildOtrosSection(String otroTexto) {
        StringBuilder otros = new StringBuilder();
        otros.append("<Otros>");
        
        if (otroTexto != null && !otroTexto.trim().isEmpty()) {
            otros.append("<OtroTexto>").append(otroTexto).append("</OtroTexto>");
        }
        
        otros.append("<OtroContenido>")
             .append(getContactoDesarrolladorXml())
             .append("</OtroContenido>")
             .append("</Otros>");
        
        return otros.toString();
    }
    
    /**
     * Valida si el tipo de documento es válido
     */
    public boolean isValidDocumentType(String tipoDocumento) {
        return documentConfigs.containsKey(tipoDocumento);
    }
    
    /**
     * Obtiene todos los tipos de documentos soportados
     */
    public String[] getSupportedDocumentTypes() {
        return documentConfigs.keySet().toArray(new String[0]);
    }
}