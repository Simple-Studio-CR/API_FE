package app.simplestudio.com.util;

import org.springframework.stereotype.Component;

@Component
public class DocumentTypeUtil {
    
    /**
     * Convierte código de tipo documento a descripción legible
     */
    public String tipoDocumento(String td) {
        if (td == null) return "";
        
        switch (td.toUpperCase()) {
            case "FE":
                return "Factura Electrónica";
            case "ND":
                return "Nota de débito Electrónica";
            case "NC":
                return "Nota de crédito Electrónica";
            case "TE":
                return "Tiquete Electrónico";
            case "FEC":
                return "Factura Electrónica Compra";
            case "FEE":
                return "Factura Electrónica Exportación";
            default:
                return "";
        }
    }
    
    /**
     * Obtiene tipo de documento desde clave (posiciones 29-31)
     */
    public String getTipoDocumentoFromClave(String clave) {
        if (clave == null || clave.length() < 31) {
            return "";
        }
        return clave.substring(29, 31);
    }
    
    /**
     * Verifica si el tipo de documento es un Mensaje Receptor (05, 06, 07)
     */
    public boolean isMensajeReceptor(String tipoDocumento) {
        return "05".equals(tipoDocumento) || "06".equals(tipoDocumento) || "07".equals(tipoDocumento);
    }
    
    /**
     * Obtiene el consecutivo desde la clave (posiciones 21-41)
     */
    public String getConsecutivoFromClave(String clave) {
        if (clave == null || clave.length() < 41) {
            return "";
        }
        return clave.substring(21, 41);
    }
    
    /**
     * Construye nombre de archivo para respuesta de MH
     */
    public String buildResponseFileName(String clave) {
        return clave + "-respuesta-mh.xml";
    }
    
    /**
     * Construye nombre de archivo para XML firmado
     */
    public String buildSignedFileName(String clave) {
        return clave + "-factura-sign.xml";
    }
    
    /**
     * Construye nombre de archivo PDF
     */
    public String buildPdfFileName(String clave) {
        return clave + "-factura.pdf";
    }
}