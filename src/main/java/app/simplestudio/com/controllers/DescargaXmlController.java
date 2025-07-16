package app.simplestudio.com.controllers;

import app.simplestudio.com.service.storage.S3FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api-4.3")
public class DescargaXmlController {

  private static final Logger log = LoggerFactory.getLogger(DescargaXmlController.class);

  @Autowired
  private S3FileService s3FileService;

  /**
   * Descarga archivos XML usando únicamente S3
   *
   * @param identificacion - Cédula/identificación del emisor
   * @param filename - Nombre del archivo XML
   * @return ResponseEntity con el archivo o error
   */
  @GetMapping("get-xml/{identificacion}/{filename:.+}")
  public ResponseEntity<?> getXmlEnviado(@PathVariable String identificacion, @PathVariable String filename) {

    log.info("Solicitud de descarga XML - Emisor: {}, Archivo: {}", identificacion, filename);

    try {
      // Validar parámetros de entrada
      ResponseEntity<?> validation = validateRequest(identificacion, filename);
      if (validation != null) {
        return validation;
      }
      // Obtener el archivo como Resource desde S3
      Resource resource = s3FileService.getXmlAsResource(identificacion, filename);

      if (resource == null || !resource.exists()) {
        log.error("Error obteniendo recurso de S3 - Emisor: {}, Archivo: {}", identificacion, filename);
        return buildErrorResponse(500, "Error interno del servidor");
      }

      // Construir respuesta exitosa
      log.info("Archivo XML servido exitosamente - Emisor: {}, Archivo: {}", identificacion, filename);

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .header(HttpHeaders.CONTENT_TYPE, "application/xml")
          .body(resource);

    } catch (Exception e) {
      log.error("Error inesperado descargando XML - Emisor: {}, Archivo: {}", identificacion, filename, e);
      return buildErrorResponse(500, "Error interno del servidor");
    }
  }

  /**
   * Valida los parámetros de entrada
   */
  private ResponseEntity<?> validateRequest(String identificacion, String filename) {

    // Validar identificación
    if (identificacion == null || identificacion.trim().isEmpty()) {
      log.warn("Identificación vacía o nula");
      return buildErrorResponse(400, "Identificación requerida");
    }

    // Validar que la identificación sea numérica y tenga longitud válida
    String cleanId = identificacion.trim();
    if (!cleanId.matches("\\d{9,12}")) {
      log.warn("Identificación inválida: {}", identificacion);
      return buildErrorResponse(400, "Identificación debe ser numérica de 9-12 dígitos");
    }

    // Validar filename
    if (filename == null || filename.trim().isEmpty()) {
      log.warn("Nombre de archivo vacío o nulo");
      return buildErrorResponse(400, "Nombre de archivo requerido");
    }

    // Validar extensión XML
    if (!filename.toLowerCase().endsWith(".xml")) {
      log.warn("Archivo debe ser XML: {}", filename);
      return buildErrorResponse(400, "Solo se permiten archivos XML");
    }

    // Validar caracteres peligrosos en filename
    if (containsDangerousCharacters(filename)) {
      log.warn("Nombre de archivo contiene caracteres peligrosos: {}", filename);
      return buildErrorResponse(400, "Nombre de archivo inválido");
    }

    // Validar longitud del filename
    if (filename.length() > 200) {
      log.warn("Nombre de archivo muy largo: {}", filename);
      return buildErrorResponse(400, "Nombre de archivo muy largo");
    }

    return null; // Todo válido
  }

  /**
   * Verifica si el filename contiene caracteres peligrosos
   */
  private boolean containsDangerousCharacters(String filename) {
    // Caracteres no permitidos que pueden ser peligrosos
    String[] dangerousChars = {"..", "/", "\\", ":", "*", "?", "\"", "<", ">", "|"};

    for (String dangerous : dangerousChars) {
      if (filename.contains(dangerous)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Construye respuesta de error estandarizada
   */
  private ResponseEntity<Map<String, Object>> buildErrorResponse(int code, String message) {
    Map<String, Object> response = new HashMap<>();
    response.put("resp", code);
    response.put("error", message);

    HttpStatus status = switch (code) {
      case 400 -> HttpStatus.BAD_REQUEST;
      case 404 -> HttpStatus.NOT_FOUND;
      case 500 -> HttpStatus.INTERNAL_SERVER_ERROR;
      default -> HttpStatus.BAD_REQUEST;
    };

    return new ResponseEntity<>(response, status);
  }
}