package app.simplestudio.com.mh;

import app.simplestudio.com.exception.XmlProcessingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

/**
 * Helper para cargar y normalizar documentos XML desde el sistema de archivos.
 */
@Component
public class XmlHelper {
  private static final Logger log = LoggerFactory.getLogger(XmlHelper.class);

  private final DocumentBuilderFactory factory;

  public XmlHelper() {
    this.factory = DocumentBuilderFactory.newInstance();
    this.factory.setNamespaceAware(true);
  }

  /**
   * Carga un XML desde la ruta indicada y devuelve su DOM normalizado.
   *
   * @param xmlPath ruta (absoluta o relativa) al archivo XML
   * @return {@link Document} con el contenido parseado y normalizado
   * @throws XmlProcessingException si ocurre un error de I/O o parsing
   */
  public Document parse(Path xmlPath) {
    Objects.requireNonNull(xmlPath, "xmlPath must not be null");
    log.info("Cargando XML desde '{}'", xmlPath);

    try (var is = Files.newInputStream(xmlPath)) {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(is);
      doc.getDocumentElement().normalize();
      log.info("XML cargado y normalizado correctamente");
      return doc;
    } catch (Exception e) {
      log.error("Error al parsear XML '{}': {}", xmlPath, e.getMessage(), e);
      throw new XmlProcessingException("Fallo al cargar XML: " + xmlPath, e);
    }
  }
}