package app.simplestudio.com.mh.helpers;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import lombok.extern.slf4j.Slf4j;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class XmlResponseHelper {
    
    /**
     * Extrae el mensaje de Hacienda del XML de respuesta
     * 
     * @param xmlBase64 XML en formato Base64
     * @return El mensaje de Hacienda procesado
     */
    public String extraerMensajeHacienda(String xmlBase64) {
        try {
            // Decodificar Base64
            String xmlString = new String(Base64.decodeBase64(xmlBase64), StandardCharsets.UTF_8);
            
            // Parsear XML
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlString));
            Document doc = db.parse(is);
            
            // Buscar MensajeHacienda
            NodeList nodes = doc.getElementsByTagName("MensajeHacienda");
            
            if (nodes.getLength() > 0) {
                Element element = (Element) nodes.item(0);
                NodeList detalleMensaje = element.getElementsByTagName("DetalleMensaje");
                
                if (detalleMensaje.getLength() > 0) {
                    Element line = (Element) detalleMensaje.item(0);
                    return StringEscapeUtils.escapeJava(getCharacterDataFromElement(line));
                }
            }
            
            return "";
            
        } catch (Exception e) {
            log.error("Error procesando XML de respuesta", e);
            return "Error procesando respuesta XML";
        }
    }
    
    /**
     * Extrae el contenido de texto de un elemento XML
     * 
     * @param element Elemento XML
     * @return El contenido de texto del elemento
     */
    private String getCharacterDataFromElement(Element element) {
        Node child = element.getFirstChild();
        if (child instanceof org.w3c.dom.CharacterData) {
            org.w3c.dom.CharacterData cd = (org.w3c.dom.CharacterData) child;
            return cd.getData();
        }
        return "";
    }
}