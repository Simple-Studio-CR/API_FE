package app.simplestudio.com.controllers;

import app.simplestudio.com.service.S3Funciones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para la descarga de archivos XML y logos desde DigitalOcean Spaces (S3 compatible).
 */
@RestController
@RequestMapping("/api-4.3")
public class DescargaController {
  private static final Logger log = LoggerFactory.getLogger(DescargaController.class);

  private final S3Funciones s3Funciones;
  private final String bucketName;

  public DescargaController(
      S3Funciones s3Funciones,
      @Value("${cloud.aws.s3.bucket}") String bucketName) {
    this.s3Funciones = s3Funciones;
    this.bucketName = bucketName;
    log.info("DescargaController initialized with bucket='{}'", bucketName);
  }

  /**
   * Descarga un archivo XML dado cédula, clave y tipo.
   */
  @GetMapping("/xml/{cedula}/{clave}/{tipo}")
  public ResponseEntity<?> downloadXml(
      @PathVariable String cedula,
      @PathVariable String clave,
      @PathVariable String tipo) {
    log.info("Download XML request: cedula={}, clave={}, tipo={}", cedula, clave, tipo);

    String key = String.format("XmlClientes/%s/%s-%s.xml", cedula, clave, tipo);
    log.debug("Constructed S3 key for XML: {}", key);

    try {
      byte[] xmlBytes = s3Funciones.downloadFileFromS3(bucketName, key);
      if (xmlBytes == null || xmlBytes.length == 0) {
        log.warn("XML file is empty or not found: bucket={}, key={}", bucketName, key);
        return ResponseEntity.status(404)
            .body("Archivo XML no encontrado o vacío: " + key);
      }

      String fileName = String.format("%s-%s.xml", clave, tipo);
      log.info("Serving XML file '{}' ({} bytes) from bucket='{}'", fileName, xmlBytes.length, bucketName);

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
          .contentType(MediaType.APPLICATION_XML)
          .body(new ByteArrayResource(xmlBytes));
    } catch (Exception e) {
      log.error("Error downloading XML from S3: bucket={}, key={}", bucketName, key, e);
      return ResponseEntity.badRequest()
          .body("Error al descargar el archivo XML: " + e.getMessage());
    }
  }

  /**
   * Descarga un logo PNG dado el nombre del logo.
   */
  @GetMapping("/download-logo/{logoName}")
  public ResponseEntity<?> downloadLogo(@PathVariable String logoName) {
    log.info("Download logo request: {}", logoName);

    String key = String.format("XmlClientes/logo/%s", logoName);
    log.debug("Constructed S3 key for logo: {}", key);

    try {
      byte[] logoBytes = s3Funciones.downloadFileFromS3(bucketName, key);
      if (logoBytes == null || logoBytes.length == 0) {
        log.warn("Logo file is empty or not found: bucket={}, key={}", bucketName, key);
        return ResponseEntity.status(404)
            .body("Logo no encontrado o vacío: " + key);
      }

      String fileName = logoName;
      log.info("Serving logo file '{}' ({} bytes) from bucket='{}'", fileName, logoBytes.length, bucketName);

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
          .contentType(MediaType.IMAGE_PNG)
          .body(new ByteArrayResource(logoBytes));
    } catch (Exception e) {
      log.error("Error downloading logo from S3: bucket={}, key={}", bucketName, key, e);
      return ResponseEntity.badRequest()
          .body("Error al descargar el logo: " + e.getMessage());
    }
  }
}
