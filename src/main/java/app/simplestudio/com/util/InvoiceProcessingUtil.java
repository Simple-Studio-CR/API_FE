package app.simplestudio.com.util;

import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import com.fasterxml.jackson.databind.JsonNode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceProcessingUtil {
    
    private static final Logger log = LoggerFactory.getLogger(InvoiceProcessingUtil.class);
    
    @Autowired
    private DocumentTypeUtil documentTypeUtil;
    
    @Autowired
    private EnvironmentConfigUtil environmentConfigUtil;
    
    @Autowired
    private XmlValidationUtil xmlValidationUtil;
    
    /**
     * Resultado del procesamiento de factura
     */
    public static class InvoiceProcessingResult {
        private boolean success;
        private String message;
        private int responseCode;
        private CCampoFactura campoFactura;
        private ComprobantesElectronicos comprobante;
        private String xmlPath;
        private String clave;
        
        public InvoiceProcessingResult(boolean success, String message, int responseCode) {
            this.success = success;
            this.message = message;
            this.responseCode = responseCode;
        }
        
        // Getters y Setters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getResponseCode() { return responseCode; }
        public CCampoFactura getCampoFactura() { return campoFactura; }
        public ComprobantesElectronicos getComprobante() { return comprobante; }
        public String getXmlPath() { return xmlPath; }
        public String getClave() { return clave; }
        
        public void setCampoFactura(CCampoFactura campoFactura) { this.campoFactura = campoFactura; }
        public void setComprobante(ComprobantesElectronicos comprobante) { this.comprobante = comprobante; }
        public void setXmlPath(String xmlPath) { this.xmlPath = xmlPath; }
        public void setClave(String clave) { this.clave = clave; }
    }
    
    /**
     * Parámetros de procesamiento de factura
     */
    public static class InvoiceProcessingParams {
        private JsonNode requestData;
        private Emisor emisor;
        private String tipoDocumento;
        private String pathUploadFiles;
        private CTerminal terminal;
        
        public InvoiceProcessingParams(JsonNode requestData, Emisor emisor, String tipoDocumento, String pathUploadFiles) {
            this.requestData = requestData;
            this.emisor = emisor;
            this.tipoDocumento = tipoDocumento;
            this.pathUploadFiles = pathUploadFiles;
        }
        
        // Getters y Setters
        public JsonNode getRequestData() { return requestData; }
        public Emisor getEmisor() { return emisor; }
        public String getTipoDocumento() { return tipoDocumento; }
        public String getPathUploadFiles() { return pathUploadFiles; }
        public CTerminal getTerminal() { return terminal; }
        public void setTerminal(CTerminal terminal) { this.terminal = terminal; }
    }
    
    /**
     * Valida los datos básicos de la solicitud
     */
    public Map<String, Object> validateBasicRequest(JsonNode requestData) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        
        // Validar tipo de documento
        String tipoDocumento = requestData.path("tipoDocumento").asText();
        if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
            validation.put("valid", false);
            validation.put("message", "Tipo de documento es requerido");
            validation.put("code", 400);
            return validation;
        }
        
        // Validar situación
        String situacion = requestData.path("situacion").asText();
        if (situacion == null || situacion.trim().isEmpty()) {
            validation.put("valid", false);
            validation.put("message", "Situación es requerida");
            validation.put("code", 400);
            return validation;
        }
        
        // Validar sucursal
        if (!requestData.has("sucursal")) {
            validation.put("valid", false);
            validation.put("message", "Sucursal es requerida");
            validation.put("code", 400);
            return validation;
        }
        
        // Validar terminal
        if (!requestData.has("terminal")) {
            validation.put("valid", false);
            validation.put("message", "Terminal es requerido");
            validation.put("code", 400);
            return validation;
        }
        
        // Validar emisor
        String emisor = requestData.path("emisor").asText();
        if (emisor == null || emisor.trim().isEmpty()) {
            validation.put("valid", false);
            validation.put("message", "Emisor es requerido");
            validation.put("code", 400);
            return validation;
        }
        
        log.debug("Validación básica exitosa para tipo: {}", tipoDocumento);
        return validation;
    }
    
    /**
     * Configura el ambiente según el emisor
     */
    public EnvironmentConfigUtil.EnvironmentConfig configureEnvironment(Emisor emisor) {
        return environmentConfigUtil.configureEnvironment(emisor.getAmbiente());
    }
    
    /**
     * Genera la fecha de emisión actual
     */
    public String generateCurrentEmissionDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return format.format(new Date()) + "-06:00";
    }
    
    /**
     * Calcula el consecutivo basado en el terminal y comprobante existente
     */
    public Long calculateConsecutive(CTerminal terminal, ComprobantesElectronicos existing, String tipoDocumento) {
        Long consecutivoTerminal = getConsecutivoFromTerminal(terminal, tipoDocumento);
        Long consecutivoExistente = (existing != null) ? existing.getConsecutivo() + 1L : 1L;
        
        return Math.max(consecutivoExistente, consecutivoTerminal);
    }
    
    /**
     * Obtiene el consecutivo del terminal según el tipo de documento
     */
    private Long getConsecutivoFromTerminal(CTerminal terminal, String tipoDocumento) {
        if (terminal == null) return 1L;
        
        return switch (tipoDocumento.toUpperCase()) {
            case "FE" -> terminal.getConsecutivoFe();
            case "ND" -> terminal.getConsecutivoNd();
            case "NC" -> terminal.getConsecutivoNc();
            case "TE" -> terminal.getConsecutivoTe();
            case "FEC" -> terminal.getConsecutivoFEC();
            case "FEE" -> terminal.getConsecutivoFEE();
            case "CCE" -> terminal.getConsecutivoCCE();
            case "CPCE" -> terminal.getConsecutivoCPCE();
            case "RCE" -> terminal.getConsecutivoRCE();
            default -> 1L;
        };
    }
    
    /**
     * Construye la ruta del certificado
     */
    public String buildCertificatePath(String pathUploadFiles, String identificacion, String certificado) {
        return pathUploadFiles + "/" + identificacion + "/cert/" + certificado;
    }
    
    /**
     * Construye la ruta del XML
     */
    public String buildXmlPath(String pathUploadFiles, String identificacion, String nombreArchivo) {
        return pathUploadFiles + identificacion + "/" + nombreArchivo + ".xml";
    }
    
    /**
     * Valida si el emisor tiene configuración completa
     */
    public Map<String, Object> validateEmisorConfiguration(Emisor emisor) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        
        if (emisor.getUserApi() == null || emisor.getUserApi().trim().isEmpty()) {
            validation.put("valid", false);
            validation.put("message", "Usuario API no configurado para el emisor");
            validation.put("code", 401);
            return validation;
        }
        
        if (emisor.getPwApi() == null || emisor.getPwApi().trim().isEmpty()) {
            validation.put("valid", false);
            validation.put("message", "Password API no configurado para el emisor");
            validation.put("code", 401);
            return validation;
        }
        
        if (emisor.getCertificado() == null || emisor.getCertificado().trim().isEmpty()) {
            validation.put("valid", false);
            validation.put("message", "Certificado no configurado para el emisor");
            validation.put("code", 401);
            return validation;
        }
        
        return validation;
    }
    
    /**
     * Construye respuesta de error estándar
     */
    public Map<String, Object> buildErrorResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("response", code);
        response.put("msj", message);
        log.warn("Error response: {} - {}", code, message);
        return response;
    }
    
    /**
     * Construye respuesta de éxito estándar
     */
    public Map<String, Object> buildSuccessResponse(String clave, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("response", 200);
        response.put("clave", clave);
        response.put("msj", message);
        log.info("Success response: {}", clave);
        return response;
    }
    
    /**
     * Valida campos específicos para factura de exportación
     */
    public Map<String, Object> validateExportInvoiceFields(JsonNode requestData) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        
        // Para FEE se requieren campos específicos del receptor
        if (!requestData.has("receptorNombre") || requestData.path("receptorNombre").asText().trim().isEmpty()) {
            validation.put("valid", false);
            validation.put("message", "Nombre del receptor es requerido para Factura de Exportación");
            validation.put("code", 401);
            return validation;
        }
        
        // Validar campos de ubicación específicos para FEE
        String[] requiredFields = {"receptorProvincia", "receptorCanton", "receptorDistrito", "receptorOtrasSenas"};
        for (String field : requiredFields) {
            if (!requestData.has(field) || requestData.path(field).asText().trim().isEmpty()) {
                validation.put("valid", false);
                validation.put("message", "Campo " + field + " es requerido para Factura de Exportación");
                validation.put("code", 401);
                return validation;
            }
        }
        
        return validation;
    }
    
    /**
     * Procesa texto para evitar caracteres especiales
     */
    public String processText(String input) {
        return xmlValidationUtil.procesarTexto(input);
    }
    
    /**
     * Valida formato de número de factura
     */
    public boolean isValidInvoiceNumber(String numeroFactura) {
        return numeroFactura != null && 
               !numeroFactura.trim().isEmpty() && 
               numeroFactura.length() == 50;
    }
    
    /**
     * Log detallado del procesamiento
     */
    public void logProcessingDetails(InvoiceProcessingParams params) {
        log.info("=== PROCESAMIENTO DE FACTURA ===");
        log.info("Tipo: {}", params.getTipoDocumento());
        log.info("Emisor: {}", params.getEmisor().getIdentificacion());
        log.info("Ambiente: {}", params.getEmisor().getAmbiente());
        log.info("Terminal configurado: {}", params.getTerminal() != null);
        log.info("================================");
    }
}