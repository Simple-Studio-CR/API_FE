package app.simplestudio.com.util;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class XmlBuilderUtil {
    
    private StringBuilder xml;
    
    /**
     * Inicia un nuevo builder XML
     */
    public XmlBuilderUtil createDocument() {
        this.xml = new StringBuilder();
        return this;
    }
    
    /**
     * Agrega un elemento XML simple
     */
    public XmlBuilderUtil addElement(String tagName, String value) {
        if (value != null && !value.trim().isEmpty()) {
            xml.append("<").append(tagName).append(">")
               .append(value)
               .append("</").append(tagName).append(">");
        }
        return this;
    }
    
    /**
     * Agrega un elemento XML con escape de caracteres
     */
    public XmlBuilderUtil addElementWithEscape(String tagName, String value) {
        if (value != null && !value.trim().isEmpty()) {
            xml.append("<").append(tagName).append(">")
               .append(escapeXml(value))
               .append("</").append(tagName).append(">");
        }
        return this;
    }
    
    /**
     * Agrega elemento solo si la condición es verdadera
     */
    public XmlBuilderUtil addElementIf(boolean condition, String tagName, String value) {
        if (condition && value != null && !value.trim().isEmpty()) {
            addElement(tagName, value);
        }
        return this;
    }
    
    /**
     * Abre un tag XML
     */
    public XmlBuilderUtil openTag(String tagName) {
        xml.append("<").append(tagName).append(">");
        return this;
    }
    
    /**
     * Cierra un tag XML
     */
    public XmlBuilderUtil closeTag(String tagName) {
        xml.append("</").append(tagName).append(">");
        return this;
    }
    
    /**
     * Agrega contenido directo sin tags
     */
    public XmlBuilderUtil addContent(String content) {
        if (content != null) {
            xml.append(content);
        }
        return this;
    }
    
    /**
     * Agrega una sección completa de XML
     */
    public XmlBuilderUtil addSection(String xmlSection) {
        if (xmlSection != null && !xmlSection.trim().isEmpty()) {
            xml.append(xmlSection);
        }
        return this;
    }
    
    /**
     * Agrega elemento con atributos
     */
    public XmlBuilderUtil addElementWithAttributes(String tagName, String value, Map<String, String> attributes) {
        xml.append("<").append(tagName);
        
        if (attributes != null) {
            for (Map.Entry<String, String> attr : attributes.entrySet()) {
                xml.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
            }
        }
        
        xml.append(">").append(value).append("</").append(tagName).append(">");
        return this;
    }
    
    /**
     * Agrega múltiples elementos del mismo tipo
     */
    public XmlBuilderUtil addRepeatingElements(String tagName, String... values) {
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.trim().isEmpty()) {
                    addElement(tagName, value);
                }
            }
        }
        return this;
    }
    
    /**
     * Construye bloque de identificación estándar
     */
    public XmlBuilderUtil addIdentificacionBlock(String tipo, String numero) {
        if (tipo != null && numero != null) {
            openTag("Identificacion")
                .addElement("Tipo", tipo)
                .addElement("Numero", numero)
                .closeTag("Identificacion");
        }
        return this;
    }
    
    /**
     * Construye bloque de ubicación estándar
     */
    public XmlBuilderUtil addUbicacionBlock(String provincia, String canton, String distrito, String barrio, String otrasSenas) {
        if (provincia != null && canton != null && distrito != null && otrasSenas != null) {
            openTag("Ubicacion")
                .addElement("Provincia", provincia)
                .addElement("Canton", canton)
                .addElement("Distrito", distrito);
            
            if (barrio != null && !barrio.trim().isEmpty()) {
                addElement("Barrio", barrio);
            }
            
            addElement("OtrasSenas", otrasSenas)
                .closeTag("Ubicacion");
        }
        return this;
    }
    
    /**
     * Construye bloque de teléfono estándar
     */
    public XmlBuilderUtil addTelefonoBlock(String tagName, String codigoPais, String numero) {
        if (codigoPais != null && numero != null && !codigoPais.trim().isEmpty() && !numero.trim().isEmpty()) {
            openTag(tagName)
                .addElement("CodigoPais", codigoPais)
                .addElement("NumTelefono", numero)
                .closeTag(tagName);
        }
        return this;
    }
    
    /**
     * Obtiene el XML construido como String
     */
    public String build() {
        return xml != null ? xml.toString() : "";
    }
    
    /**
     * Obtiene la longitud actual del XML
     */
    public int length() {
        return xml != null ? xml.length() : 0;
    }
    
    /**
     * Limpia el builder para reutilización
     */
    public XmlBuilderUtil clear() {
        if (xml != null) {
            xml.setLength(0);
        }
        return this;
    }
    
    /**
     * Escape básico para XML
     */
    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}