package app.simplestudio.com.mh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GeneraXmlImpl {
  private static final Logger log = LoggerFactory.getLogger(GeneraXmlImpl.class);

  // Método mejorado para generar XML
  public String generarXML(Object documento, String tipoDocumento) {
    try {
      log.info("Generando documento tipo: {}", tipoDocumento);

      // Construir el XML usando StringBuilder para evitar problemas de concatenación
      StringBuilder xmlBuilder = new StringBuilder();

      // Agregar declaración XML
      xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

      // Determinar el elemento raíz según el tipo de documento
      String rootElement = getRootElement(tipoDocumento);

      // Agregar namespace según especificación de Hacienda v4.3
      xmlBuilder.append("<").append(rootElement)
          .append(" xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/")
          .append(getSchemaName(tipoDocumento))
          .append("\"")
          .append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");

      // Aquí agregarías el contenido del documento
      // Por ahora, vamos a asegurar que el XML esté bien formado

      // Cerrar elemento raíz
      xmlBuilder.append("</").append(rootElement).append(">");

      String xmlFinal = xmlBuilder.toString();

      // Validar que el XML esté bien formado
      validateXmlStructure(xmlFinal);

      // Log para debugging
      log.debug("XML generado (primeros 500 chars): {}",
          xmlFinal.length() > 500 ? xmlFinal.substring(0, 500) + "..." : xmlFinal);

      return xmlFinal;

    } catch (Exception e) {
      log.error("Error generando XML: {}", e.getMessage(), e);
      throw new RuntimeException("Error generando XML: " + e.getMessage(), e);
    }
  }

  private String getRootElement(String tipoDocumento) {
    // Según la documentación de Hacienda v4.3
    switch (tipoDocumento) {
      case "01": return "FacturaElectronica";
      case "02": return "NotaDebitoElectronica";
      case "03": return "NotaCreditoElectronica";
      case "04": return "TiqueteElectronico";
      case "08": return "FacturaElectronicaCompra";
      case "09": return "FacturaElectronicaExportacion";
      default: return "FacturaElectronica";
    }
  }

  private String getSchemaName(String tipoDocumento) {
    switch (tipoDocumento) {
      case "01": return "facturaElectronica";
      case "02": return "notaDebitoElectronica";
      case "03": return "notaCreditoElectronica";
      case "04": return "tiqueteElectronico";
      case "08": return "facturaElectronicaCompra";
      case "09": return "facturaElectronicaExportacion";
      default: return "facturaElectronica";
    }
  }

  private void validateXmlStructure(String xml) throws Exception {
    // Verificaciones básicas
    if (xml == null || xml.trim().isEmpty()) {
      throw new Exception("XML vacío o nulo");
    }

    // Verificar que empiece con declaración XML
    if (!xml.trim().startsWith("<?xml")) {
      throw new Exception("XML no empieza con declaración XML");
    }

    // Verificar que tenga un solo elemento raíz
    int rootStart = xml.indexOf("<", xml.indexOf("?>") + 2);
    if (rootStart == -1) {
      throw new Exception("No se encontró elemento raíz");
    }

    // Extraer nombre del elemento raíz
    int rootEnd = xml.indexOf(">", rootStart);
    String rootTag = xml.substring(rootStart + 1, rootEnd);
    if (rootTag.contains(" ")) {
      rootTag = rootTag.substring(0, rootTag.indexOf(" "));
    }

    // Verificar que el elemento raíz se cierre correctamente
    String closeTag = "</" + rootTag + ">";
    int closeTagIndex = xml.lastIndexOf(closeTag);
    if (closeTagIndex == -1) {
      throw new Exception("Elemento raíz no está cerrado correctamente");
    }

    // Verificar que no haya contenido después del cierre del elemento raíz
    String afterRoot = xml.substring(closeTagIndex + closeTag.length()).trim();
    if (!afterRoot.isEmpty()) {
      throw new Exception("Hay contenido después del elemento raíz: " + afterRoot);
    }

    log.info("Estructura XML validada correctamente");
  }

  // Método para limpiar caracteres no válidos en XML
  public static String sanitizeXmlContent(String content) {
    if (content == null) return "";

    // Reemplazar caracteres de control no válidos
    content = content.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");

    // Escapar caracteres especiales XML
    content = content.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");

    return content;
  }
}