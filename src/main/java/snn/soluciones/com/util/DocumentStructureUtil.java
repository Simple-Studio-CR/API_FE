package snn.soluciones.com.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad mejorada para manejar la estructura de documentos XML de Hacienda v4.3
 * Mantiene exactamente las mismas interfaces públicas
 */
@Component
public class DocumentStructureUtil {

    private static final Logger log = LoggerFactory.getLogger(DocumentStructureUtil.class);

    // Mapeo de tipos de documento a sus elementos raíz
    private static final Map<String, String> DOCUMENT_ROOT_ELEMENTS = new HashMap<>();
    private static final Map<String, String> DOCUMENT_SCHEMAS = new HashMap<>();
    private static final Map<String, String> DOCUMENT_DESCRIPTIONS = new HashMap<>();

    static {
        // Configuración según especificación Hacienda v4.3
        DOCUMENT_ROOT_ELEMENTS.put("01", "FacturaElectronica");
        DOCUMENT_ROOT_ELEMENTS.put("02", "NotaDebitoElectronica");
        DOCUMENT_ROOT_ELEMENTS.put("03", "NotaCreditoElectronica");
        DOCUMENT_ROOT_ELEMENTS.put("04", "TiqueteElectronico");
        DOCUMENT_ROOT_ELEMENTS.put("05", "MensajeReceptor");
        DOCUMENT_ROOT_ELEMENTS.put("08", "FacturaElectronicaCompra");
        DOCUMENT_ROOT_ELEMENTS.put("09", "FacturaElectronicaExportacion");

        DOCUMENT_SCHEMAS.put("01", "facturaElectronica");
        DOCUMENT_SCHEMAS.put("02", "notaDebitoElectronica");
        DOCUMENT_SCHEMAS.put("03", "notaCreditoElectronica");
        DOCUMENT_SCHEMAS.put("04", "tiqueteElectronico");
        DOCUMENT_SCHEMAS.put("05", "mensajeReceptor");
        DOCUMENT_SCHEMAS.put("08", "facturaElectronicaCompra");
        DOCUMENT_SCHEMAS.put("09", "facturaElectronicaExportacion");

        DOCUMENT_DESCRIPTIONS.put("01", "Factura Electrónica");
        DOCUMENT_DESCRIPTIONS.put("02", "Nota de Débito Electrónica");
        DOCUMENT_DESCRIPTIONS.put("03", "Nota de Crédito Electrónica");
        DOCUMENT_DESCRIPTIONS.put("04", "Tiquete Electrónico");
        DOCUMENT_DESCRIPTIONS.put("05", "Mensaje Receptor");
        DOCUMENT_DESCRIPTIONS.put("08", "Factura Electrónica de Compra");
        DOCUMENT_DESCRIPTIONS.put("09", "Factura Electrónica de Exportación");
    }

    /**
     * Obtiene el header del documento XML según el tipo
     * INTERFAZ SIN CAMBIOS
     */
    public String getDocumentHeader(String tipoDocumento) {
        String rootElement = DOCUMENT_ROOT_ELEMENTS.getOrDefault(tipoDocumento, "FacturaElectronica");
        String schema = DOCUMENT_SCHEMAS.getOrDefault(tipoDocumento, "facturaElectronica");

        // Construir header con StringBuilder para evitar problemas de concatenación
        StringBuilder header = new StringBuilder();

        // Declaración XML (solo una vez, al principio)
        header.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        // Elemento raíz con namespaces
        header.append("<").append(rootElement);
        header.append(" xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/").append(schema).append("\"");
        header.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        header.append(" xsi:schemaLocation=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/").append(schema);
        header.append(" https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/").append(schema).append(".xsd\">");

        String result = header.toString();

        if (log.isDebugEnabled()) {
            log.debug("Header generado para tipo {}: {}", tipoDocumento, result);
        }

        return result;
    }

    /**
     * Obtiene el footer del documento XML según el tipo
     * INTERFAZ SIN CAMBIOS
     */
    public String getDocumentFooter(String tipoDocumento) {
        String rootElement = DOCUMENT_ROOT_ELEMENTS.getOrDefault(tipoDocumento, "FacturaElectronica");
        String footer = "</" + rootElement + ">";

        if (log.isDebugEnabled()) {
            log.debug("Footer generado para tipo {}: {}", tipoDocumento, footer);
        }

        return footer;
    }

    /**
     * Obtiene la descripción del tipo de documento
     * INTERFAZ SIN CAMBIOS
     */
    public String getDocumentDescription(String tipoDocumento) {
        return DOCUMENT_DESCRIPTIONS.getOrDefault(tipoDocumento, "Documento Desconocido");
    }

    /**
     * Verifica si es un mensaje receptor
     * INTERFAZ SIN CAMBIOS
     */
    public boolean isMensajeReceptor(String tipoDocumento) {
        return "05".equals(tipoDocumento);
    }

    /**
     * Verifica si es una factura de exportación
     * INTERFAZ SIN CAMBIOS
     */
    public boolean isFacturaExportacion(String tipoDocumento) {
        return "09".equals(tipoDocumento);
    }

    /**
     * Construye la sección "Otros" del documento
     * INTERFAZ SIN CAMBIOS
     */
    public String buildOtrosSection(String otros) {
        if (otros == null || otros.trim().isEmpty()) {
            return "";
        }

        // Validar que la sección Otros esté bien formada
        String cleanOtros = otros.trim();

        // Si no empieza con <Otros>, agregarlo
        if (!cleanOtros.startsWith("<Otros>")) {
            cleanOtros = "<Otros>" + cleanOtros;
        }

        // Si no termina con </Otros>, agregarlo
        if (!cleanOtros.endsWith("</Otros>")) {
            cleanOtros = cleanOtros + "</Otros>";
        }

        return cleanOtros;
    }

    /**
     * NUEVO: Valida que un documento XML completo esté bien formado
     * Método interno para validación adicional
     */
    public boolean validateCompleteDocument(String xmlDocument, String tipoDocumento) {
        if (xmlDocument == null || xmlDocument.trim().isEmpty()) {
            log.error("Documento XML vacío o nulo");
            return false;
        }

        String rootElement = DOCUMENT_ROOT_ELEMENTS.getOrDefault(tipoDocumento, "FacturaElectronica");

        // Verificar que empiece con declaración XML
        if (!xmlDocument.trim().startsWith("<?xml")) {
            log.error("El documento no empieza con declaración XML");
            return false;
        }

        // Verificar que tenga el elemento raíz correcto
        if (!xmlDocument.contains("<" + rootElement)) {
            log.error("El documento no contiene el elemento raíz esperado: {}", rootElement);
            return false;
        }

        // Verificar que cierre correctamente
        if (!xmlDocument.contains("</" + rootElement + ">")) {
            log.error("El documento no cierra correctamente el elemento raíz: {}", rootElement);
            return false;
        }

        // Verificar que no haya contenido después del cierre
        int closeIndex = xmlDocument.lastIndexOf("</" + rootElement + ">");
        if (closeIndex != -1) {
            String afterClose = xmlDocument.substring(closeIndex + rootElement.length() + 3).trim();
            if (!afterClose.isEmpty()) {
                log.error("Hay contenido después del cierre del elemento raíz: '{}'", afterClose);
                return false;
            }
        }

        return true;
    }
}