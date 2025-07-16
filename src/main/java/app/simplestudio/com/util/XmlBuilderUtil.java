package app.simplestudio.com.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Stack;

/**
 * Utilidad mejorada para construir XML bien formado
 * Mantiene la misma interfaz pública pero mejora la implementación interna
 */
@Component
public class XmlBuilderUtil {

    private static final Logger log = LoggerFactory.getLogger(XmlBuilderUtil.class);

    /**
     * Crea un nuevo builder para construir documentos XML
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil createDocument() {
        return new XmlBuilderUtil();
    }

    // Estado interno del builder
    private StringBuilder xmlContent;
    private Stack<String> openTags;
    private boolean hasContent;

    public XmlBuilderUtil() {
        this.xmlContent = new StringBuilder();
        this.openTags = new Stack<>();
        this.hasContent = false;
    }

    /**
     * Agrega una sección completa al XML
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil addSection(String section) {
        if (section != null && !section.trim().isEmpty()) {
            // Validar que la sección no tenga problemas
            String cleanSection = validateAndCleanSection(section);
            xmlContent.append(cleanSection);
            hasContent = true;
        }
        return this;
    }

    /**
     * Agrega contenido directo al XML
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil addContent(String content) {
        if (content != null && !content.trim().isEmpty()) {
            String cleanContent = validateAndCleanContent(content);
            xmlContent.append(cleanContent);
            hasContent = true;
        }
        return this;
    }

    /**
     * Agrega un elemento simple con valor
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil addElement(String tagName, String value) {
        if (tagName != null && value != null) {
            xmlContent.append("<").append(tagName).append(">");
            xmlContent.append(escapeXml(value));
            xmlContent.append("</").append(tagName).append(">");
            hasContent = true;
        }
        return this;
    }

    /**
     * Agrega un elemento con escape de caracteres especiales
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil addElementWithEscape(String tagName, String value) {
        return addElement(tagName, value); // Ya escapa por defecto
    }

    /**
     * Agrega un elemento condicionalmente
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil addElementIf(boolean condition, String tagName, String value) {
        if (condition && tagName != null && value != null) {
            return addElement(tagName, value);
        }
        return this;
    }

    /**
     * Abre un tag
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil openTag(String tagName) {
        if (tagName != null) {
            xmlContent.append("<").append(tagName).append(">");
            openTags.push(tagName);
            hasContent = true;
        }
        return this;
    }

    /**
     * Cierra un tag
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil closeTag(String tagName) {
        if (tagName != null) {
            // Validar que el tag que se cierra corresponde al último abierto
            if (!openTags.isEmpty() && openTags.peek().equals(tagName)) {
                openTags.pop();
                xmlContent.append("</").append(tagName).append(">");
            } else {
                log.warn("Intento de cerrar tag '{}' pero el último tag abierto es '{}'",
                    tagName, openTags.isEmpty() ? "ninguno" : openTags.peek());
                // Cerrar de todas formas para mantener compatibilidad
                xmlContent.append("</").append(tagName).append(">");
            }
        }
        return this;
    }

    /**
     * Agrega un bloque de identificación
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil addIdentificacionBlock(String tipo, String numero) {
        if (tipo != null && numero != null) {
            openTag("Identificacion");
            addElement("Tipo", tipo);
            addElement("Numero", numero);
            closeTag("Identificacion");
        }
        return this;
    }

    /**
     * Agrega un bloque de ubicación
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil addUbicacionBlock(String provincia, String canton, String distrito, String barrio, String otrasSenas) {
        openTag("Ubicacion");
        addElement("Provincia", provincia);
        addElement("Canton", canton);
        addElement("Distrito", distrito);
        if (barrio != null) {
            addElement("Barrio", barrio);
        }
        if (otrasSenas != null) {
            addElement("OtrasSenas", otrasSenas);
        }
        closeTag("Ubicacion");
        return this;
    }

    /**
     * Agrega un bloque de teléfono
     * INTERFAZ SIN CAMBIOS
     */
    public XmlBuilderUtil addTelefonoBlock(String tagName, String codigoPais, String numero) {
        if (tagName != null && codigoPais != null && numero != null) {
            openTag(tagName);
            addElement("CodigoPais", codigoPais);
            addElement("NumTelefono", numero);
            closeTag(tagName);
        }
        return this;
    }

    /**
     * Construye el XML final
     * INTERFAZ SIN CAMBIOS pero con validación mejorada
     */
    public String build() {
        // Cerrar cualquier tag que haya quedado abierto
        while (!openTags.isEmpty()) {
            String unclosedTag = openTags.pop();
            log.warn("Tag '{}' no fue cerrado correctamente. Cerrándolo automáticamente.", unclosedTag);
            xmlContent.append("</").append(unclosedTag).append(">");
        }

        String result = xmlContent.toString();

        // Validación final
        result = performFinalValidation(result);

        // Log para debugging en modo debug
        if (log.isDebugEnabled()) {
            log.debug("XML generado - Longitud: {} caracteres", result.length());
            if (result.length() > 200) {
                log.debug("Inicio: {}", result.substring(0, 100));
                log.debug("Final: {}", result.substring(result.length() - 100));
            }
        }

        return result;
    }

    /**
     * NUEVO: Valida y limpia una sección de XML
     */
    private String validateAndCleanSection(String section) {
        // Eliminar espacios al inicio y final
        section = section.trim();

        // Verificar que no haya múltiples declaraciones XML
        if (section.contains("<?xml") && xmlContent.toString().contains("<?xml")) {
            log.warn("Se detectó intento de agregar múltiple declaración XML");
            section = section.replaceAll("<\\?xml[^>]*\\?>", "");
        }

        return section;
    }

    /**
     * NUEVO: Valida y limpia contenido directo
     */
    private String validateAndCleanContent(String content) {
        // Similar a validateAndCleanSection pero más estricto
        content = content.trim();

        // Remover cualquier declaración XML
        content = content.replaceAll("<\\?xml[^>]*\\?>", "");

        return content;
    }

    /**
     * NUEVO: Escapa caracteres especiales XML
     */
    private String escapeXml(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    /**
     * NUEVO: Validación final del XML completo
     */
    private String performFinalValidation(String xml) {
        if (xml == null || xml.isEmpty()) {
            return "";
        }

        // Eliminar espacios al final
        xml = xml.trim();

        // Eliminar cualquier contenido después del último tag de cierre
        int lastClosingTag = xml.lastIndexOf(">");
        if (lastClosingTag != -1 && lastClosingTag < xml.length() - 1) {
            String trailing = xml.substring(lastClosingTag + 1).trim();
            if (!trailing.isEmpty()) {
                log.warn("Se encontró contenido después del XML: '{}'. Se eliminará.", trailing);
                xml = xml.substring(0, lastClosingTag + 1);
            }
        }

        // Verificar balance de tags (conteo básico)
        int openCount = countOccurrences(xml, "<", "<?", "</", "<!--");
        int closeCount = countOccurrences(xml, "</", null, null, null);

        if (openCount != closeCount) {
            log.warn("Posible desbalance de tags. Tags abiertos: {}, Tags cerrados: {}", openCount, closeCount);
        }

        return xml;
    }

    /**
     * NUEVO: Cuenta ocurrencias excluyendo ciertos patrones
     */
    private int countOccurrences(String text, String pattern, String exclude1, String exclude2, String exclude3) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(pattern, index)) != -1) {
            boolean shouldCount = true;

            // Verificar exclusiones
            if (exclude1 != null && text.startsWith(exclude1, index)) shouldCount = false;
            if (exclude2 != null && text.startsWith(exclude2, index)) shouldCount = false;
            if (exclude3 != null && text.startsWith(exclude3, index)) shouldCount = false;

            if (shouldCount) count++;
            index += pattern.length();
        }

        return count;
    }
}