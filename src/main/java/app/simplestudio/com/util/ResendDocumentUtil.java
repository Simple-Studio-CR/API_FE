package app.simplestudio.com.util;

import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.service.storage.S3FileService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utilidad especializada para el reenvío de documentos electrónicos
 * Maneja validaciones, construcción de rutas y respuestas JSON específicas para reenvío
 */
@Component
public class ResendDocumentUtil {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private DocumentTypeUtil documentTypeUtil;

    @Value("${path.upload.files.api}")
    private String pathUploadFilesApi;

    @Autowired
    private S3FileService s3FileService;

    @Value("${url.qr}")
    private String urlQr;

    // ==================== VALIDACIONES ====================

    /**
     * Valida la request completa de reenvío
     */
    public Map<String, Object> validateResendRequest(String clave, String email) {
        Map<String, Object> result = new HashMap<>();

        // Validar que ambos campos estén presentes
        if (clave == null || email == null || email.isEmpty()) {
            result.put("valid", false);
            result.put("code", 0);
            result.put("message", "La clave y el correo son requeridos!!!");
            return result;
        }

        // Validar longitud de clave
        if (!isValidClaveLength(clave)) {
            result.put("valid", false);
            result.put("code", 0);
            result.put("message", "La clave debe ser de 50 dígitos!!!");
            return result;
        }

        result.put("valid", true);
        return result;
    }

    /**
     * Valida que el documento esté en estado aceptado
     */
    public boolean isDocumentAccepted(ComprobantesElectronicos ce) {
        if (ce == null) {
            log.warn("ComprobantesElectronicos es null");
            return false;
        }

        return ce.getIndEstado() != null && "aceptado".equals(ce.getIndEstado());
    }

    /**
     * Valida que la clave tenga exactamente 50 dígitos
     */
    public boolean isValidClaveLength(String clave) {
        return clave != null && clave.length() == 50;
    }

    /**
     * Valida que el email tenga formato válido
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Regex básico para validación de email
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // ==================== CONSTRUCCIÓN DE RUTAS Y ARCHIVOS ====================

    /**
     * Construye las rutas de los archivos XML necesarios para el reenvío
     */
    public Map<String, String> buildXmlFilePaths(String identificacion, String clave) {
        Map<String, String> paths = new HashMap<>();
        
        String basePath = pathUploadFilesApi + identificacion + "/";
        
        paths.put("respuestaMh", basePath + clave + "-respuesta-mh.xml");
        paths.put("facturaSign", basePath + clave + "-factura-sign.xml");
        paths.put("basePath", basePath);
        
        log.debug("Rutas construidas para clave {}: {}", clave, paths);
        return paths;
    }

    /**
     * Valida que todos los archivos XML necesarios existan
     */
    public Map<String, Object> validateXmlFilesExist(Map<String, String> filePaths) {
        Map<String, Object> result = new HashMap<>();
        
        String respuestaMhPath = filePaths.get("respuestaMh");
        String facturaSignPath = filePaths.get("facturaSign");
        
        boolean respuestaMhExists = s3FileService.fileExists(respuestaMhPath);
        boolean facturaSignExists = s3FileService.fileExists(facturaSignPath);
        
        if (!respuestaMhExists) {
            log.warn("Archivo respuesta MH no existe: {}", respuestaMhPath);
            result.put("valid", false);
            result.put("missingFile", "respuesta-mh.xml");
            return result;
        }
        
        if (!facturaSignExists) {
            log.warn("Archivo factura firmada no existe: {}", facturaSignPath);
            result.put("valid", false);
            result.put("missingFile", "factura-sign.xml");
            return result;
        }
        
        result.put("valid", true);
        log.debug("Todos los archivos XML existen para el reenvío");
        return result;
    }

    /**
     * Construye la ruta del logo de la empresa
     */
    public String buildLogoPath(String logoEmpresa) {
        if (logoEmpresa != null && !logoEmpresa.isEmpty()) {
            return logoEmpresa;
        } else {
            return "default.png";
        }
    }

    // ==================== CONSTRUCCIÓN DE RESPUESTAS JSON ====================

    /**
     * Construye respuesta JSON de éxito
     */
    public String buildSuccessResponse(String email) {
        return buildJsonResponse(1, "Se envío un correo con la factura a: " + email);
    }

    /**
     * Construye respuesta JSON de error por documento no aceptado
     */
    public String buildDocumentNotAcceptedResponse() {
        return buildJsonResponse(3, 
            "Este documento está a la espera de la aceptación del Ministerio de Hacienda, " +
            "se podrá distribuir hasta que este aceptado.");
    }

    /**
     * Construye respuesta JSON de error por documento no encontrado
     */
    public String buildDocumentNotFoundResponse() {
        return buildJsonResponse(3, "El documento que desea reenviar no existe!!!");
    }

    /**
     * Construye respuesta JSON de error del sistema
     */
    public String buildSystemErrorResponse() {
        return buildJsonResponse(3, "Error contacte al desarrollador del sistema.");
    }

    /**
     * Construye respuesta JSON de error de validación
     */
    public String buildValidationErrorResponse(String message) {
        return buildJsonResponse(0, message);
    }

    /**
     * Construye respuesta JSON de error de email
     */
    public String buildEmailErrorResponse(String email, String errorMessage) {
        return buildJsonResponse(2, 
            "Error al intentar enviar el correo a " + email + ", error generado: " + errorMessage);
    }

    /**
     * Método base para construir respuestas JSON en formato String
     */
    public String buildJsonResponse(int responseCode, String message) {
        return String.format("{\"response\":\"%d\",\"msj\":\"%s\"}", responseCode, message);
    }

    // ==================== CONSTRUCCIÓN DE CONTENIDO EMAIL ====================

    /**
     * Construye el mensaje HTML para el email
     */
    public String buildEmailMessage(String tipoDocumento, String nombreEmpresa, String clave, String urlQr) {
        StringBuilder mensaje = new StringBuilder();
        
        mensaje.append("<html><head><meta charset='UTF-8'></head><body>");
        mensaje.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        
        // Header
        mensaje.append("<div style='background-color: #f8f9fa; padding: 20px; text-align: center;'>");
        mensaje.append("<h2 style='color: #007bff; margin: 0;'>Documento Electrónico</h2>");
        mensaje.append("</div>");
        
        // Contenido principal
        mensaje.append("<div style='padding: 20px; background-color: white;'>");
        mensaje.append("<p style='font-size: 16px;'>Estimado cliente,</p>");
        mensaje.append("<p>Se adjunta el <strong>").append(tipoDocumento).append("</strong> ");
        mensaje.append("emitido por <strong>").append(nombreEmpresa).append("</strong>.</p>");
        
        // Información del documento
        mensaje.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
        mensaje.append("<h3 style='margin-top: 0; color: #495057;'>Información del Documento:</h3>");
        mensaje.append("<p><strong>Tipo:</strong> ").append(tipoDocumento).append("</p>");
        mensaje.append("<p><strong>Clave:</strong> ").append(clave).append("</p>");
        
        // QR Code si está disponible
        if (urlQr != null && !urlQr.isEmpty()) {
            mensaje.append("<p><strong>Código QR:</strong> <a href='").append(urlQr).append(clave)
                   .append("' target='_blank'>Ver documento</a></p>");
        }
        mensaje.append("</div>");
        
        // Archivos adjuntos
        mensaje.append("<div style='background-color: #e7f3ff; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
        mensaje.append("<h4 style='margin-top: 0; color: #0056b3;'>Archivos Adjuntos:</h4>");
        mensaje.append("<ul style='margin: 10px 0;'>");
        mensaje.append("<li>📄 ").append(clave).append("-factura.pdf</li>");
        mensaje.append("<li>📋 ").append(clave).append("-factura-sign.xml</li>");
        mensaje.append("<li>✅ ").append(clave).append("-respuesta-mh.xml</li>");
        mensaje.append("</ul>");
        mensaje.append("</div>");
        
        // Footer
        mensaje.append("<div style='background-color: #f8f9fa; padding: 15px; text-align: center; margin-top: 20px;'>");
        mensaje.append("<p style='margin: 0; font-size: 14px; color: #6c757d;'>");
        mensaje.append("Este es un mensaje automático. Por favor no responda a este correo.");
        mensaje.append("</p>");
        mensaje.append("</div>");
        
        mensaje.append("</div>");
        mensaje.append("</body></html>");
        
        return mensaje.toString();
    }

    /**
     * Construye el subject del email
     */
    public String buildEmailSubject(String tipoDocumento, String nombreEmpresa, String clave) {
        return String.format("%s - %s - Clave: %s", 
            tipoDocumento, 
            nombreEmpresa != null ? nombreEmpresa : "Documento Electrónico", 
            clave);
    }

    // ==================== MÉTODOS DE LOGGING ====================

    /**
     * Registra el inicio del proceso de reenvío
     */
    public void logResendStart(String clave, String email) {
        log.info("=== INICIO REENVÍO DOCUMENTO ===");
        log.info("Clave: {}, Email destino: {}", clave, email);
    }

    /**
     * Registra el éxito del reenvío
     */
    public void logResendSuccess(String clave, String email) {
        log.info("Reenvío exitoso - Clave: {}, Email: {}", clave, email);
        log.info("=== FIN REENVÍO DOCUMENTO ===");
    }

    /**
     * Registra errores en el proceso de reenvío
     */
    public void logResendError(String clave, String email, String error) {
        log.error("Error en reenvío - Clave: {}, Email: {}, Error: {}", clave, email, error);
        log.info("=== FIN REENVÍO DOCUMENTO (CON ERROR) ===");
    }

    // ==================== MÉTODOS DE MAPEO ====================

    /**
     * Información del tipo de documento (delegado a DocumentTypeUtil)
     */
    public String getDocumentTypeDescription(String tipoDocumento) {
        return documentTypeUtil.tipoDocumento(tipoDocumento);
    }
}