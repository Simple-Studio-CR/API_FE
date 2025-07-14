package app.simplestudio.com.mh;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

/**
 * Servicio con funciones utilitarias de cadena, generación de códigos y manejo de XML.
 */
@Service
public class FuncionesService {
  private static final Logger log = LoggerFactory.getLogger(FuncionesService.class);
  private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
  private final SecureRandom random = new SecureRandom();

  /**
   * Verifica si la cadena contiene sólo dígitos.
   *
   * @param str la cadena a evaluar
   * @return {@code true} si todos los caracteres son dígitos, {@code false} en otro caso
   */
  public boolean isNumeric(String str) {
    boolean matches = str != null && NUMERIC_PATTERN.matcher(str).matches();
    log.debug("isNumeric('{}') = {}", str, matches);
    return matches;
  }

  /**
   * Rellena la cadena a la longitud indicada usando el texto de relleno,
   * pudiendo elegir alineación a izquierda, derecha o centrado.
   *
   * @param input  texto original
   * @param length longitud total deseada
   * @param pad    texto de relleno (repetido o truncado)
   * @param sense  "LEFT", "RIGHT" o "BOTH"
   * @return texto relleno o el input si ya supera la longitud
   */
  public String strPad(String input, int length, String pad, String sense) {
    Objects.requireNonNull(input, "input must not be null");
    Objects.requireNonNull(pad, "pad must not be null");
    if (input.length() >= length) {
      log.debug("strPad: input length >= target length, returning original: {}", input);
      return input;
    }
    int padLen = length - input.length();
    log.debug("strPad: padding '{}' to length {} with '{}' sense={}", input, length, pad, sense);
    switch (sense.toUpperCase()) {
      case "LEFT":
        return fillString(pad, padLen) + input;
      case "RIGHT":
        return input + fillString(pad, padLen);
      case "BOTH":
        int left = padLen / 2;
        int right = padLen - left;
        return fillString(pad, left) + input + fillString(pad, right);
      default:
        log.warn("strPad: unknown sense '{}', defaulting to RIGHT", sense);
        return input + fillString(pad, padLen);
    }
  }

  /**
   * Genera un texto de longitud exacta replicando y truncando el string de pad.
   *
   * @param pad      texto a replicar
   * @param required longitud deseada del resultado
   * @return cadena de longitud {@code required}
   */
  private String fillString(String pad, int required) {
    StringBuilder sb = new StringBuilder(required);
    while (sb.length() < required) {
      sb.append(pad);
    }
    String result = sb.substring(0, required);
    log.trace("fillString: created '{}'", result);
    return result;
  }

  /**
   * Genera un código numérico aleatorio de la longitud dada.
   *
   * @param digits cantidad de dígitos
   * @return cadena numérica aleatoria
   */
  public String getCodigoSeguridad(int digits) {
    if (digits <= 0) {
      throw new IllegalArgumentException("digits must be positive");
    }
    StringBuilder sb = new StringBuilder(digits);
    for (int i = 0; i < digits; i++) {
      sb.append(random.nextInt(10));
    }
    String code = sb.toString();
    log.debug("getCodigoSeguridad({}) = {}", digits, code);
    return code;
  }

  /**
   * Parsea contenido XML desde String y lo convierte a Document.
   *
   * @param xmlContent contenido XML como String
   * @return Document parseado
   * @throws Exception si hay errores en el parsing
   */
  public Document parseXmlContent(String xmlContent) throws Exception {
    Objects.requireNonNull(xmlContent, "xmlContent must not be null");

    log.debug("parseXmlContent: parsing XML content of length {}", xmlContent.length());

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

      // Configuración de seguridad para prevenir XXE attacks
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);

      // Configurar namespace awareness si es necesario
      factory.setNamespaceAware(true);
      factory.setValidating(false);

      DocumentBuilder builder = factory.newDocumentBuilder();

      // Parsear el XML desde String
      ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes("UTF-8"));
      Document document = builder.parse(inputStream);

      // Normalizar el documento
      document.getDocumentElement().normalize();

      log.debug("parseXmlContent: successfully parsed XML document with root element: {}",
          document.getDocumentElement().getNodeName());

      return document;

    } catch (Exception e) {
      log.error("parseXmlContent: error parsing XML content", e);
      throw new Exception("Error parsing XML content: " + e.getMessage(), e);
    }
  }

  /**
   * Convierte un Document XML a String.
   *
   * @param document el Document a convertir
   * @return String representación del XML
   * @throws Exception si hay errores en la conversión
   */
  public String documentToString(Document document) throws Exception {
    Objects.requireNonNull(document, "document must not be null");

    log.debug("documentToString: converting document to string");

    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();

      // Configuración de seguridad
      transformerFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
      transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
      transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");

      Transformer transformer = transformerFactory.newTransformer();

      // Configurar la salida del XML
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");

      // Convertir Document a String
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      DOMSource source = new DOMSource(document);

      transformer.transform(source, result);

      String xmlString = writer.toString();

      log.debug("documentToString: successfully converted document to string of length {}",
          xmlString.length());

      return xmlString;

    } catch (Exception e) {
      log.error("documentToString: error converting document to string", e);
      throw new Exception("Error converting document to string: " + e.getMessage(), e);
    }
  }

  /**
   * Método utilitario para validar si un String contiene XML válido.
   *
   * @param xmlContent contenido a validar
   * @return true si es XML válido, false en caso contrario
   */
  public boolean isValidXml(String xmlContent) {
    if (xmlContent == null || xmlContent.trim().isEmpty()) {
      return false;
    }

    try {
      parseXmlContent(xmlContent);
      return true;
    } catch (Exception e) {
      log.debug("isValidXml: invalid XML content: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Extrae el nombre del elemento raíz de un documento XML.
   *
   * @param xmlContent contenido XML
   * @return nombre del elemento raíz o null si hay error
   */
  public String getRootElementName(String xmlContent) {
    try {
      Document document = parseXmlContent(xmlContent);
      String rootName = document.getDocumentElement().getNodeName();
      log.debug("getRootElementName: root element is '{}'", rootName);
      return rootName;
    } catch (Exception e) {
      log.error("getRootElementName: error extracting root element name", e);
      return null;
    }
  }

  /**
   * Formatea un documento XML con indentación para mejor legibilidad.
   *
   * @param xmlContent contenido XML a formatear
   * @return XML formateado con indentación
   * @throws Exception si hay errores en el procesamiento
   */
  public String formatXml(String xmlContent) throws Exception {
    Document document = parseXmlContent(xmlContent);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();

    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

    StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(document), new StreamResult(writer));

    String formattedXml = writer.toString();
    log.debug("formatXml: formatted XML content");

    return formattedXml;
  }

  /**
   * Elimina espacios en blanco innecesarios de un XML.
   *
   * @param xmlContent contenido XML
   * @return XML compactado
   * @throws Exception si hay errores en el procesamiento
   */
  public String compactXml(String xmlContent) throws Exception {
    Document document = parseXmlContent(xmlContent);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();

    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

    StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(document), new StreamResult(writer));

    String compactXml = writer.toString();
    log.debug("compactXml: compacted XML content from {} to {} characters",
        xmlContent.length(), compactXml.length());

    return compactXml;
  }
}