package app.simplestudio.com.controllers;

import app.simplestudio.com.util.DownloadXmlUtil;
import app.simplestudio.com.util.FileManagerUtil;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.MalformedURLException;
import java.util.Map;

@Controller
@RequestMapping({"/api-4.3"})
public class DescargaXmlController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  // ==================== CONFIGURACIÓN ORIGINAL ====================
  @Value("${path.upload.files.api}")
  private String pathUploadFilesApi;

  // ==================== NUEVOS UTILS ====================
  @Autowired
  private DownloadXmlUtil downloadXmlUtil;

  @Autowired
  private FileManagerUtil fileManagerUtil;

  // ==================== ENDPOINT ORIGINAL MANTENIDO ====================

  /**
   * FIRMA ORIGINAL MANTENIDA - Descarga de archivos XML
   */
  @GetMapping({"get-xml/{identificacion}/{filename:.+}"})
  public ResponseEntity<?> getXmlEnviado(@PathVariable String identificacion, @PathVariable String filename) {

    return processXmlDownload(identificacion, filename);
  }

  // ==================== MÉTODOS AUXILIARES REFACTORIZADOS ====================

  /**
   * Procesa la solicitud completa de descarga de XML
   */
  private ResponseEntity<?> processXmlDownload(String identificacion, String filename) {
    try {
      // Logging de inicio
      downloadXmlUtil.logDownloadStart(identificacion, filename);

      // Validaciones previas
      ResponseEntity<?> validationResponse = validateDownloadRequest(identificacion, filename);
      if (validationResponse != null) {
        return validationResponse;
      }

      // Construir ruta segura
      String filePath = downloadXmlUtil.buildSecureFilePath(pathUploadFilesApi, identificacion, filename);
      if (filePath == null) {
        downloadXmlUtil.logDownloadError(identificacion, filename, "No se pudo construir ruta segura");
        return downloadXmlUtil.buildErrorResponseEntity("403", "Acceso denegado");
      }

      // Validar acceso al archivo
      Map<String, Object> fileValidation = downloadXmlUtil.validateFileAccess(filePath);
      if (!(Boolean) fileValidation.get("valid")) {
        String errorCode = (String) fileValidation.get("errorCode");
        String errorMessage = (String) fileValidation.get("errorMessage");
        downloadXmlUtil.logDownloadError(identificacion, filename, errorMessage);
        return downloadXmlUtil.buildErrorResponseEntity(errorCode, errorMessage);
      }

      // Construir respuesta de descarga
      ResponseEntity<Resource> downloadResponse = buildSuccessfulDownloadResponse(filePath, filename);

      // Log de éxito
      long fileSize = (Long) fileValidation.getOrDefault("fileSize", 0L);
      downloadXmlUtil.logDownloadSuccess(identificacion, filename, fileSize);

      return downloadResponse;

    } catch (Exception e) {
      downloadXmlUtil.logDownloadError(identificacion, filename, e.getMessage());
      log.error("Error inesperado en descarga de XML", e);
      return downloadXmlUtil.buildErrorResponseEntity("500", "Error interno del servidor");
    }
  }

  /**
   * Valida la solicitud de descarga
   */
  private ResponseEntity<?> validateDownloadRequest(String identificacion, String filename) {
    // Validar configuración del sistema
    Map<String, Object> systemValidation = downloadXmlUtil.validateSystemConfiguration();
    if (!(Boolean) systemValidation.get("valid")) {
      String issues = (String) systemValidation.get("issues");
      log.error("Sistema mal configurado para descarga: {}", issues);
      return downloadXmlUtil.buildErrorResponseEntity("500", "Sistema mal configurado");
    }

    // Validar parámetros de entrada
    Map<String, Object> parameterValidation = downloadXmlUtil.validateDownloadParameters(identificacion, filename);
    if (!(Boolean) parameterValidation.get("valid")) {
      String errorCode = (String) parameterValidation.get("errorCode");
      String errorMessage = (String) parameterValidation.get("errorMessage");

      // Log de evento de seguridad si es necesario
      if ("403".equals(errorCode)) {
        downloadXmlUtil.logSecurityEvent(identificacion, filename, errorMessage);
      }

      downloadXmlUtil.logDownloadError(identificacion, filename, errorMessage);
      return downloadXmlUtil.buildErrorResponseEntity(errorCode, errorMessage);
    }

    // Validaciones pasaron correctamente
    return null;
  }

  /**
   * Construye respuesta exitosa de descarga
   */
  private ResponseEntity<Resource> buildSuccessfulDownloadResponse(String filePath, String filename) throws MalformedURLException {
    try {
      ResponseEntity<Resource> response = downloadXmlUtil.buildDownloadResponse(filePath, filename);

      log.info("Archivo XML servido exitosamente: {}", filename);
      return response;

    } catch (MalformedURLException e) {
      log.error("Error creando URL para archivo: {}", filePath, e);
      throw e;
    } catch (Exception e) {
      log.error("Error construyendo respuesta de descarga para: {}", filename, e);
      throw new RuntimeException("Error construyendo respuesta de descarga", e);
    }
  }

  // ==================== MÉTODOS DE UTILIDAD ADICIONALES ====================

  /**
   * Verifica el estado del sistema de descarga
   */
  public Map<String, Object> checkDownloadSystemHealth() {
    return downloadXmlUtil.validateSystemConfiguration();
  }

  /**
   * Obtiene información del sistema de descarga (útil para debugging)
   */
  public Map<String, Object> getDownloadSystemInfo() {
    Map<String, Object> info = downloadXmlUtil.getDownloadSystemInfo();

    // Agregar información específica del controller
    info.put("controllerClass", this.getClass().getSimpleName());
    info.put("basePathConfigured", pathUploadFilesApi != null && !pathUploadFilesApi.trim().isEmpty());
    info.put("basePathExists", fileManagerUtil.directoryExists(pathUploadFilesApi));

    return info;
  }

  /**
   * Endpoint de diagnóstico (útil para debugging en desarrollo)
   * NOTA: Este método debería estar protegido o deshabilitado en producción
   */
  public ResponseEntity<?> diagnosticInfo() {
    Map<String, Object> diagnostics = new HashMap<>();

    // Información del sistema
    diagnostics.put("systemHealth", checkDownloadSystemHealth());
    diagnostics.put("systemInfo", getDownloadSystemInfo());

    // Información de configuración (sin datos sensibles)
    diagnostics.put("configuration", Map.of(
        "basePathLength", pathUploadFilesApi != null ? pathUploadFilesApi.length() : 0,
        "basePathPattern", pathUploadFilesApi != null ? pathUploadFilesApi.replaceAll("[^/\\\\]", "*") : "null"
    ));

    return ResponseEntity.ok(diagnostics);
  }
}