package snn.soluciones.com.util;


import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class XmlParserUtil {
    
    /**
     * Convierte XML string a Document
     */
    public Document parseXmlFromString(String xmlContent) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(new InputSource(new StringReader(xmlContent)));
    }
    
    /**
     * Extrae valor de un nodo usando XPath
     */
    public String extractNodeValue(Document xml, String xpathExpression) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xPath.evaluate(xpathExpression, xml.getDocumentElement(), XPathConstants.NODESET);
        
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }
    
    /**
     * Extrae valores múltiples de nodos usando XPath
     */
    public NodeList extractNodes(Document xml, String xpathExpression) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        return (NodeList) xPath.evaluate(xpathExpression, xml.getDocumentElement(), XPathConstants.NODESET);
    }
    
    /**
     * Verifica si un nodo existe y tiene contenido
     */
    public boolean hasNodeContent(Document xml, String xpathExpression) throws Exception {
        NodeList nodes = extractNodes(xml, xpathExpression);
        return nodes != null && nodes.getLength() > 0 && !nodes.item(0).getTextContent().trim().isEmpty();
    }
}