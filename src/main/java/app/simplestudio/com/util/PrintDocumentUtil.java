package app.simplestudio.com.util;

import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.adapter.StorageAdapter;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad especializada para la impresión de documentos electrónicos
 * Maneja validaciones, configuración de parámetros y escritura de PDFs al response
 */
@Component
public class PrintDocumentUtil {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private DocumentTypeUtil documentTypeUtil;

    @Autowired
    private StorageAdapter storageAdapter;

    @Value("${path.upload.files.api}")
    private String pathUploadFilesApi;

    @Value("${url.qr}")
    private String urlQr;

    // ==================== VALIDACIONES ====================

    /**
     * Valida que la clave sea válida para impresión
     */
    public Map<String, Object> validatePrintKey(String clave) {
        Map<String, Object> result = new HashMap<>();

        if (clave == null || clave.trim().isEmpty()) {
            result.put("valid", false);
            result.put("error", "Clave no puede estar vacía");
            return result;
        }

        if (clave.length() != 50) {
            result.put("valid", false);
            result.put("error", "La clave debe tener exactamente 50 dígitos");
            return result;
        }

        // Validar que solo contenga números
        if (!clave.matches("\\d+")) {
            result.put("valid", false);
            result.put("error", "La clave debe contener solo números");
            return result;
        }

        result.put("valid", true);
        return result;
    }

    /**
     * Valida que el documento existe y es imprimible
     */
    public Map<String, Object> validatePrintableDocument(ComprobantesElectronicos ce) {
        Map<String, Object> result = new HashMap<>();

        if (ce == null) {
            result.put("valid", false);
            result.put("error", "Documento no encontrado");
            return result;
        }

        // Verificar que tenga identificación válida
        if (ce.getIdentificacion() == null || ce.getIdentificacion().trim().isEmpty()) {
            result.put("valid", false);
            result.put("error", "Documento sin identificación válida");
            return result;
        }

        // Verificar que tenga tipo de documento
        if (ce.getTipoDocumento() == null || ce.getTipoDocumento().trim().isEmpty()) {
            result.put("valid", false);
            result.put("error", "Documento sin tipo válido");
            return result;
        }

        result.put("valid", true);
        return result;
    }

    /**
     * Valida que el emisor existe y tiene datos completos
     */
    public Map<String, Object> validateEmisorForPrint(Emisor emisor) {
        Map<String, Object> result = new HashMap<>();

        if (emisor == null) {
            result.put("valid", false);
            result.put("error", "Emisor no encontrado");
            return result;
        }

        result.put("valid", true);
        return result;
    }

    // ==================== CONFIGURACIÓN DE PARÁMETROS ====================

    /**
     * Construye los parámetros completos para JasperReports
     */
    public Map<String, Object> buildPrintParameters(String clave, Emisor emisor, String tipoDocumento) {
        Map<String, Object> parameters = new HashMap<>();

        // Parámetros básicos
        String baseUrl = getBaseUrl();
        String logoPath = buildLogoPath(emisor.getLogoEmpresa());
        String tipoDocumentoDesc = documentTypeUtil.tipoDocumento(tipoDocumento);

        parameters.put("BASE_URL", baseUrl);
        parameters.put("BASE_URL_LOGO", logoPath);
        parameters.put("CLAVE_FACTURA", clave);
        parameters.put("TIPO_DOCUMENTO", tipoDocumentoDesc);
        parameters.put("RESOLUCION", buildResolutionText());
        parameters.put("NOTA_FACTURA", emisor.getNataFactura() != null ? emisor.getNataFactura() : "");
        parameters.put("URL_QR", buildQrUrl(clave));

        // Logging de parámetros (sin datos sensibles)
        log.debug("Parámetros de impresión configurados para clave: {}", clave);
        log.debug("Tipo documento: {}, Logo: {}", tipoDocumentoDesc, logoPath != null ? "Configurado" : "Default");

        return parameters;
    }

    /**
     * Construye la ruta del logo de la empresa
     */
    public String buildLogoPath(String logoEmpresa) {
        String logoPath;
        
        if (logoEmpresa != null && !logoEmpresa.trim().isEmpty()) {
            logoPath = pathUploadFilesApi + "logo/" + logoEmpresa.trim();

            if (storageAdapter.fileExists(logoPath)) {
                log.debug("Logo empresarial encontrado: {}", logoPath);
                return logoPath;
            } else {
                log.warn("Logo empresarial no encontrado: {}, usando default", logoPath);
            }
        }

        // Usar logo por defecto
        logoPath = pathUploadFilesApi + "logo/default.png";
        log.debug("Usando logo por defecto: {}", logoPath);
        return logoPath;
    }

    /**
     * Construye la URL del código QR
     */
    public String buildQrUrl(String clave) {
        if (urlQr != null && !urlQr.trim().isEmpty()) {
            String qrUrl = urlQr.trim() + clave;
            log.debug("URL QR construida: {}", qrUrl);
            return qrUrl;
        }
        
        log.warn("URL QR no configurada");
        return "";
    }

    /**
     * Construye el texto de resolución estándar
     */
    public String buildResolutionText() {
        return "Autorizada mediante resolución Nº DGT-R-033-2019 del 20/06/2019";
    }

    /**
     * Obtiene la URL base para recursos
     */
    public String getBaseUrl() {
        try {
            URL base = getClass().getResource("/");
            if (base != null) {
                String baseUrl = base.toString();
                log.debug("URL base obtenida: {}", baseUrl);
                return baseUrl;
            }
        } catch (Exception e) {
            log.warn("Error obteniendo URL base: {}", e.getMessage());
        }
        
        log.warn("URL base no disponible, usando string vacío");
        return "";
    }

    // ==================== MANEJO DE RESPUESTA HTTP ====================

    /**
     * Configura el response para PDF
     */
    public void configurePdfResponse(HttpServletResponse response) {
        response.setContentType("application/pdf");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        log.debug("Response configurado para PDF");
    }

    /**
     * Escribe el PDF al ServletOutputStream
     */
    public void writePdfToResponse(byte[] pdfBytes, HttpServletResponse response) throws IOException {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IOException("PDF bytes están vacíos o son nulos");
        }

        // Configurar response
        configurePdfResponse(response);
        response.setContentLength(pdfBytes.length);

        // Escribir PDF al output stream
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(pdfBytes, 0, pdfBytes.length);
            outputStream.flush();
            
            log.debug("PDF escrito al response exitosamente. Tamaño: {} bytes", pdfBytes.length);
        } catch (IOException e) {
            log.error("Error escribiendo PDF al response: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Escribe un mensaje de error al response
     */
    public void writeErrorToResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            String fullMessage = "Error generando PDF: " + errorMessage;
            outputStream.write(fullMessage.getBytes("UTF-8"));
            outputStream.flush();
            
            log.error("Error message written to response: {}", errorMessage);
        }
    }

    // ==================== LOGGING ESPECIALIZADO ====================

    /**
     * Registra el inicio del proceso de impresión
     */
    public void logPrintStart(String clave) {
        log.info("=== INICIO IMPRESIÓN DOCUMENTO ===");
        log.info("Clave: {}", clave);
    }

    /**
     * Registra el éxito de la impresión
     */
    public void logPrintSuccess(String clave, int pdfSize) {
        log.info("Impresión exitosa - Clave: {}, Tamaño PDF: {} bytes", clave, pdfSize);
        log.info("=== FIN IMPRESIÓN DOCUMENTO ===");
    }

    /**
     * Registra errores en el proceso de impresión
     */
    public void logPrintError(String clave, String error) {
        log.error("Error en impresión - Clave: {}, Error: {}", clave, error);
        log.info("=== FIN IMPRESIÓN DOCUMENTO (CON ERROR) ===");
    }

    /**
     * Registra validaciones fallidas
     */
    public void logValidationError(String clave, String validationError) {
        log.warn("Validación fallida para impresión - Clave: {}, Error: {}", clave, validationError);
    }
    // ==================== UTILIDADES AUXILIARES ====================

    /**
     * Extrae información del tipo de documento para logging
     */
    public String getDocumentTypeForLogging(String tipoDocumento) {
        String description = documentTypeUtil.tipoDocumento(tipoDocumento);
        return description != null && !description.trim().isEmpty() ? description : tipoDocumento;
    }

    /**
     * Construye información resumida del documento para logging
     */
    public String buildDocumentSummary(ComprobantesElectronicos ce, Emisor emisor) {
        if (ce == null) return "Documento: null";

      return "Documento["
          + "Clave: " + (ce.getClave() != null ? ce.getClave().substring(0, 10) + "..." : "null")
          + ", Tipo: " + getDocumentTypeForLogging(ce.getTipoDocumento())
          + ", Emisor: " + (emisor != null ? emisor.getIdentificacion() : "null")
          + "]";
    }

    /**
     * Verifica si el sistema está configurado correctamente para impresión
     */
    public Map<String, Object> validateSystemConfiguration() {
        Map<String, Object> result = new HashMap<>();
        StringBuilder issues = new StringBuilder();

        // Verificar configuración de paths
        if (pathUploadFilesApi == null || pathUploadFilesApi.trim().isEmpty()) {
            issues.append("path.upload.files.api no configurado; ");
        }

        // Verificar template de reporte
        if (getClass().getResourceAsStream("/facturas.jasper") == null) {
            issues.append("Template /facturas.jasper no encontrado; ");
        }

        if (!issues.isEmpty()) {
            result.put("valid", false);
            result.put("issues", issues.toString());
            log.warn("Problemas de configuración del sistema: {}", issues);
        } else {
            result.put("valid", true);
            log.debug("Configuración del sistema validada correctamente");
        }

        return result;
    }
}