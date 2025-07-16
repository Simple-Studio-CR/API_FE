package app.simplestudio.com.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilidad especializada para descarga segura de archivos XML
 * Maneja validaciones de seguridad, construcción de rutas y respuestas de descarga
 */
@Component
public class DownloadXmlUtil {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private FileManagerUtil fileManagerUtil;

    @Value("${path.upload.files.api}")
    private String pathUploadFilesApi;

    // Patrones de seguridad
    private static final Pattern VALID_IDENTIFICACION_PATTERN = Pattern.compile("^[0-9]{9,12}$");
    private static final Pattern VALID_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+\\.xml$");
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(\\.\\./|\\.\\\\|\\.\\.\\\\)");
    private static final Pattern INVALID_CHARS_PATTERN = Pattern.compile("[<>:\"|?*\\x00-\\x1f]");

    // Constantes de configuración
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String[] ALLOWED_EXTENSIONS = {".xml"};

    // ==================== VALIDACIONES PRINCIPALES ====================

    /**
     * Valida todos los parámetros de descarga
     */
    public Map<String, Object> validateDownloadParameters(String identificacion, String filename) {
        Map<String, Object> result = new HashMap<>();

        // Validar identificación
        Map<String, Object> idValidation = validateIdentificacion(identificacion);
        if (!(Boolean) idValidation.get("valid")) {
            return idValidation;
        }

        // Validar filename
        Map<String, Object> filenameValidation = validateFilename(filename);
        if (!(Boolean) filenameValidation.get("valid")) {
            return filenameValidation;
        }

        // Validar constrains de seguridad
        Map<String, Object> securityValidation = validateSecurityConstraints(identificacion, filename);
        if (!(Boolean) securityValidation.get("valid")) {
            return securityValidation;
        }

        result.put("valid", true);
        return result;
    }

    /**
     * Valida la identificación del emisor
     */
    public Map<String, Object> validateIdentificacion(String identificacion) {
        Map<String, Object> result = new HashMap<>();

        if (identificacion == null || identificacion.trim().isEmpty()) {
            result.put("valid", false);
            result.put("errorCode", "400");
            result.put("errorMessage", "Identificación no puede estar vacía");
            return result;
        }

        String cleanId = identificacion.trim();

        // Validar formato (9-12 dígitos)
        if (!VALID_IDENTIFICACION_PATTERN.matcher(cleanId).matches()) {
            result.put("valid", false);
            result.put("errorCode", "400");
            result.put("errorMessage", "Identificación debe contener entre 9 y 12 dígitos");
            return result;
        }

        result.put("valid", true);
        return result;
    }

    /**
     * Valida el nombre del archivo
     */
    public Map<String, Object> validateFilename(String filename) {
        Map<String, Object> result = new HashMap<>();

        if (filename == null || filename.trim().isEmpty()) {
            result.put("valid", false);
            result.put("errorCode", "400");
            result.put("errorMessage", "Nombre de archivo no puede estar vacío");
            return result;
        }

        String cleanFilename = filename.trim();

        // Validar caracteres inválidos
        if (INVALID_CHARS_PATTERN.matcher(cleanFilename).find()) {
            result.put("valid", false);
            result.put("errorCode", "400");
            result.put("errorMessage", "Nombre de archivo contiene caracteres inválidos");
            return result;
        }

        // Validar patrón de filename
        if (!VALID_FILENAME_PATTERN.matcher(cleanFilename).matches()) {
            result.put("valid", false);
            result.put("errorCode", "400");
            result.put("errorMessage", "Nombre de archivo debe ser válido y tener extensión .xml");
            return result;
        }

        // Validar extensión
        if (!hasValidExtension(cleanFilename)) {
            result.put("valid", false);
            result.put("errorCode", "400");
            result.put("errorMessage", "Solo se permiten archivos .xml");
            return result;
        }

        result.put("valid", true);
        return result;
    }

    /**
     * Valida constrains de seguridad críticos
     */
    public Map<String, Object> validateSecurityConstraints(String identificacion, String filename) {
        Map<String, Object> result = new HashMap<>();

        // Validar path traversal attacks
        if (containsPathTraversal(identificacion) || containsPathTraversal(filename)) {
            result.put("valid", false);
            result.put("errorCode", "403");
            result.put("errorMessage", "Acceso denegado: path traversal detectado");
            log.warn("Intento de path traversal detectado - ID: {}, Filename: {}", identificacion, filename);
            return result;
        }

        // Validar caracteres sospechosos
        if (containsSuspiciousPatterns(identificacion) || containsSuspiciousPatterns(filename)) {
            result.put("valid", false);
            result.put("errorCode", "403");
            result.put("errorMessage", "Acceso denegado: caracteres sospechosos detectados");
            log.warn("Caracteres sospechosos detectados - ID: {}, Filename: {}", identificacion, filename);
            return result;
        }

        result.put("valid", true);
        return result;
    }

    // ==================== CONSTRUCCIÓN DE RUTAS SEGURAS ====================

    /**
     * Construye ruta segura del archivo
     */
    public String buildSecureFilePath(String basePath, String identificacion, String filename) {
        try {
            // Normalizar y sanitizar inputs
            String cleanBasePath = normalizePath(basePath);
            String cleanId = sanitizeInput(identificacion);
            String cleanFilename = sanitizeInput(filename);

            // Construir path usando Paths.get para normalización automática
            Path fullPath = Paths.get(cleanBasePath)
                    .resolve(cleanId)
                    .resolve(cleanFilename)
                    .normalize()
                    .toAbsolutePath();

            // Verificar que la ruta resultante esté dentro del directorio base
            Path baseDirPath = Paths.get(cleanBasePath).normalize().toAbsolutePath();
            if (!fullPath.startsWith(baseDirPath)) {
                log.error("Path traversal attempt blocked - Computed path: {}, Base: {}", fullPath, baseDirPath);
                return null;
            }

            String finalPath = fullPath.toString();
            log.debug("Ruta segura construida: {}", finalPath);
            return finalPath;

        } catch (Exception e) {
            log.error("Error construyendo ruta segura: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Valida que el archivo existe y es accesible
     */
    public Map<String, Object> validateFileAccess(String filePath) {
        Map<String, Object> result = new HashMap<>();

        if (filePath == null || filePath.trim().isEmpty()) {
            result.put("valid", false);
            result.put("errorCode", "500");
            result.put("errorMessage", "Ruta de archivo inválida");
            return result;
        }

        try {
            File file = new File(filePath);

            // Verificar existencia
            if (!file.exists()) {
                result.put("valid", false);
                result.put("errorCode", "404");
                result.put("errorMessage", "Documento no existe");
                return result;
            }

            // Verificar que es archivo (no directorio)
            if (!file.isFile()) {
                result.put("valid", false);
                result.put("errorCode", "400");
                result.put("errorMessage", "La ruta no corresponde a un archivo");
                return result;
            }

            // Verificar legibilidad
            if (!file.canRead()) {
                result.put("valid", false);
                result.put("errorCode", "403");
                result.put("errorMessage", "Archivo no legible");
                return result;
            }

            // Verificar tamaño
            long fileSize = file.length();
            if (fileSize > MAX_FILE_SIZE) {
                result.put("valid", false);
                result.put("errorCode", "413");
                result.put("errorMessage", "Archivo demasiado grande");
                return result;
            }

            result.put("valid", true);
            result.put("fileSize", fileSize);
            return result;

        } catch (Exception e) {
            log.error("Error validando acceso al archivo {}: {}", filePath, e.getMessage());
            result.put("valid", false);
            result.put("errorCode", "500");
            result.put("errorMessage", "Error interno validando archivo");
            return result;
        }
    }

    // ==================== CONSTRUCCIÓN DE RESPUESTAS ====================

    /**
     * Construye respuesta de error estándar
     */
    public Map<String, Object> buildErrorResponse(String errorCode, String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("resp", errorCode);
        response.put("error", errorMessage);
        
        log.debug("Error response construido - Código: {}, Mensaje: {}", errorCode, errorMessage);
        return response;
    }

    /**
     * Construye ResponseEntity de error con HTTP status apropiado
     */
    public ResponseEntity<?> buildErrorResponseEntity(String errorCode, String errorMessage) {
        Map<String, Object> errorResponse = buildErrorResponse(errorCode, errorMessage);
        HttpStatus httpStatus = mapErrorCodeToHttpStatus(errorCode);
        
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    /**
     * Construye respuesta de descarga exitosa
     */
    public ResponseEntity<Resource> buildDownloadResponse(String filePath, String filename) throws MalformedURLException {
        // Crear resource
        UrlResource urlResource = new UrlResource(Paths.get(filePath).toUri());

        // Configurar headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        // Determinar content type
        String contentType = determineContentType(filename);
        if (contentType != null) {
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        }

        log.debug("Download response construido para archivo: {}", filename);
        return ResponseEntity.ok()
                .headers(headers)
                .body(urlResource);
    }

    // ==================== LOGGING ESPECIALIZADO ====================

    /**
     * Registra el inicio de descarga
     */
    public void logDownloadStart(String identificacion, String filename) {
        log.info("=== INICIO DESCARGA XML ===");
        log.info("Identificación: {}, Archivo: {}", identificacion, filename);
    }

    /**
     * Registra descarga exitosa
     */
    public void logDownloadSuccess(String identificacion, String filename, long fileSize) {
        log.info("Descarga exitosa - ID: {}, Archivo: {}, Tamaño: {} bytes", identificacion, filename, fileSize);
        log.info("=== FIN DESCARGA XML ===");
    }

    /**
     * Registra errores de descarga
     */
    public void logDownloadError(String identificacion, String filename, String error) {
        log.error("Error en descarga - ID: {}, Archivo: {}, Error: {}", identificacion, filename, error);
        log.info("=== FIN DESCARGA XML (CON ERROR) ===");
    }

    /**
     * Registra intentos sospechosos
     */
    public void logSecurityEvent(String identificacion, String filename, String securityIssue) {
        log.warn("EVENTO DE SEGURIDAD - ID: {}, Archivo: {}, Problema: {}", identificacion, filename, securityIssue);
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Verifica si contiene patrones de path traversal
     */
    private boolean containsPathTraversal(String input) {
        return input != null && PATH_TRAVERSAL_PATTERN.matcher(input).find();
    }

    /**
     * Verifica si contiene patrones sospechosos
     */
    private boolean containsSuspiciousPatterns(String input) {
        if (input == null) return false;
        
        String[] suspiciousPatterns = {
            "cmd", "powershell", "bash", "sh", "/bin/", "/etc/", "windows", "system32",
            "exec", "eval", "script", "javascript:", "vbscript:", "data:"
        };
        
        String lowerInput = input.toLowerCase();
        for (String pattern : suspiciousPatterns) {
            if (lowerInput.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si tiene extensión válida
     */
    private boolean hasValidExtension(String filename) {
        if (filename == null) return false;
        
        String lowerFilename = filename.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Normaliza path removiendo elementos problemáticos
     */
    private String normalizePath(String path) {
        if (path == null) return "";
        return path.replace("\\", "/").replaceAll("/+", "/");
    }

    /**
     * Sanitiza input removiendo caracteres problemáticos
     */
    private String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim()
                .replaceAll("[\r\n\t]", "")
                .replaceAll("\\s+", " ");
    }

    /**
     * Mapea código de error a HTTP status
     */
    private HttpStatus mapErrorCodeToHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "400" -> HttpStatus.BAD_REQUEST;
            case "401" -> HttpStatus.UNAUTHORIZED;
            case "403" -> HttpStatus.FORBIDDEN;
            case "404" -> HttpStatus.NOT_FOUND;
            case "413" -> HttpStatus.PAYLOAD_TOO_LARGE;
            case "500" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    /**
     * Determina content type basado en extensión
     */
    private String determineContentType(String filename) {
        if (filename == null) return null;
        
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".xml")) {
            return "application/xml";
        }
        return "application/octet-stream";
    }

    // ==================== MÉTODOS DE UTILIDAD PÚBLICA ====================

    /**
     * Verifica si el sistema está configurado correctamente
     */
    public Map<String, Object> validateSystemConfiguration() {
        Map<String, Object> result = new HashMap<>();
        StringBuilder issues = new StringBuilder();

        // Verificar directorio base
        if (pathUploadFilesApi == null || pathUploadFilesApi.trim().isEmpty()) {
            issues.append("path.upload.files.api no configurado; ");
        } else if (!fileManagerUtil.directoryExists(pathUploadFilesApi)) {
            issues.append("Directorio base no existe: ").append(pathUploadFilesApi).append("; ");
        }

        if (issues.length() > 0) {
            result.put("valid", false);
            result.put("issues", issues.toString());
            log.warn("Problemas de configuración del sistema de descarga: {}", issues);
        } else {
            result.put("valid", true);
            log.debug("Configuración del sistema de descarga validada correctamente");
        }

        return result;
    }
}