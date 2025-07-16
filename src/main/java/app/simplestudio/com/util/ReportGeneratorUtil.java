package app.simplestudio.com.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReportGeneratorUtil {
    
    private static final Logger log = LoggerFactory.getLogger(ReportGeneratorUtil.class);
    
    @Value("${path.upload.files.api}")
    private String pathUploadFilesApi;
    
    @Value("${url.qr}")
    private String urlQr;
    
    /**
     * Parámetros para generar reporte de factura
     */
    public static class ReportParameters {
        private String baseUrl;
        private String logoPath;
        private String clave;
        private String tipoDocumento;
        private String resolucion;
        private String notaFactura;
        private String urlQr;
        private String detalleFactura1;
        private String detalleFactura2;
        
        // Constructor builder pattern
        public static ReportParameters builder() {
            return new ReportParameters();
        }
        
        public ReportParameters baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
        
        public ReportParameters logoPath(String logoPath) {
            this.logoPath = logoPath;
            return this;
        }
        
        public ReportParameters clave(String clave) {
            this.clave = clave;
            return this;
        }
        
        public ReportParameters tipoDocumento(String tipoDocumento) {
            this.tipoDocumento = tipoDocumento;
            return this;
        }
        
        public ReportParameters resolucion(String resolucion) {
            this.resolucion = resolucion;
            return this;
        }
        
        public ReportParameters notaFactura(String notaFactura) {
            this.notaFactura = notaFactura;
            return this;
        }
        
        public ReportParameters urlQr(String urlQr) {
            this.urlQr = urlQr;
            return this;
        }
        
        public ReportParameters detalleFactura1(String detalleFactura1) {
            this.detalleFactura1 = detalleFactura1;
            return this;
        }
        
        public ReportParameters detalleFactura2(String detalleFactura2) {
            this.detalleFactura2 = detalleFactura2;
            return this;
        }
        
        public Map<String, Object> build() {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("BASE_URL", baseUrl);
            parameters.put("BASE_URL_LOGO", logoPath);
            parameters.put("CLAVE_FACTURA", clave);
            parameters.put("TIPO_DOCUMENTO", tipoDocumento);
            parameters.put("RESOLUCION", resolucion);
            parameters.put("NOTA_FACTURA", notaFactura);
            parameters.put("URL_QR", urlQr);
            parameters.put("DETALLE_FACTURA1", detalleFactura1);
            parameters.put("DETALLE_FACTURA2", detalleFactura2);
            return parameters;
        }
    }
    
    /**
     * Prepara parámetros base para el reporte
     */
    public ReportParameters prepareBaseParameters(String clave, String tipoDocumento, String logoEmpresa) {
        String logoPath = buildLogoPath(logoEmpresa);
        String baseUrl = getBaseUrl();
        String qrUrl = urlQr + clave;
        
        return ReportParameters.builder()
            .baseUrl(baseUrl)
            .logoPath(logoPath)
            .clave(clave)
            .tipoDocumento(tipoDocumento)
            .urlQr(qrUrl)
            .resolucion("Autorizada mediante resolución Nº DGT-R-033-2019 del 20/06/2019");
    }
    
    /**
     * Genera PDF desde template Jasper
     */
    public byte[] generatePdfReport(String templatePath, Map<String, Object> parameters, Connection dbConnection) throws JRException, IOException {
        InputStream reportFile = null;
        try {
            reportFile = getClass().getResourceAsStream(templatePath);
            if (reportFile == null) {
                throw new IOException("Template no encontrado: " + templatePath);
            }
            
            byte[] pdfBytes = JasperRunManager.runReportToPdf(reportFile, parameters, dbConnection);
            
            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new JRException("El reporte generado está vacío");
            }
            
            log.info("PDF generado exitosamente. Tamaño: {} bytes", pdfBytes.length);
            return pdfBytes;
            
        } catch (JRException e) {
            log.error("Error generando reporte Jasper: {}", e.getMessage());
            throw e;
        } finally {
            if (reportFile != null) {
                try {
                    reportFile.close();
                } catch (IOException e) {
                    log.warn("Error cerrando template file: {}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * Genera PDF de factura con parámetros completos
     */
    public byte[] generateInvoicePdf(String clave, String tipoDocumento, String logoEmpresa, 
                                   String notaFactura, String detalleFactura1, String detalleFactura2,
                                   Connection dbConnection) throws JRException, IOException {
        
        Map<String, Object> parameters = prepareBaseParameters(clave, tipoDocumento, logoEmpresa)
            .notaFactura(notaFactura)
            .detalleFactura1(detalleFactura1)
            .detalleFactura2(detalleFactura2)
            .build();
        
        return generatePdfReport("/facturas.jasper", parameters, dbConnection);
    }
    
    /**
     * Construye ruta del logo
     */
    private String buildLogoPath(String logoEmpresa) {
        if (logoEmpresa != null && !logoEmpresa.trim().isEmpty()) {
            return logoEmpresa;
        } else {
            return "default.png";
        }
    }
    
    /**
     * Obtiene URL base para recursos
     */
    private String getBaseUrl() {
        try {
            URL base = getClass().getResource("/");
            return base != null ? base.toString() : "";
        } catch (Exception e) {
            log.warn("No se pudo obtener URL base: {}", e.getMessage());
            return "";
        }
    }
}