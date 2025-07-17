package snn.soluciones.com.util;


import org.springframework.stereotype.Component;

@Component
public class DocumentTypeUtil {

    /**
     * Convierte código de tipo documento a descripción legible
     */
    public String tipoDocumento(String td) {
        if (td == null)
            return "";

      return switch (td.toUpperCase()) {
        case "FE" -> "Factura Electrónica";
        case "ND" -> "Nota de débito Electrónica";
        case "NC" -> "Nota de crédito Electrónica";
        case "TE" -> "Tiquete Electrónico";
        case "FEC" -> "Factura Electrónica Compra";
        case "FEE" -> "Factura Electrónica Exportación";
        default -> "";
      };
    }

    public String getCodigoNumerico(String td) {
        if (td == null) return "";

        return switch (td.toUpperCase()) {
            case "FE" -> "01";   // Factura Electrónica
            case "NC" -> "02";   // Nota de Crédito
            case "ND" -> "03";   // Nota de Débito
            case "TE" -> "04";   // Tiquete Electrónico
            case "CCE" -> "05";  // Confirming Contado Electrónico (si aplica)
            case "CPCE" -> "06"; // Confirming Plazo Crédito Electrónico (si aplica)
            case "RCE" -> "07";  // Rechazo Comrobante Electrónico (si aplica)
            case "FEC" -> "08";  // Factura Electrónica Compra
            case "FEE" -> "09";  // Factura Electrónica Exportación
            default -> "";
        };
    }

    /**
     * Convierte situación a código numérico para la clave
     */
    public String getCodigoSituacion(String situacion) {
        if (situacion == null) return "1";

        return switch (situacion.toLowerCase().trim()) {
            case "normal" -> "1";           // Situación normal
            case "contingencia" -> "2";     // Contingencia
            case "sin internet" -> "3";     // Sin conexión a internet
            default -> "1";                 // Default: normal
        };
    }

    /**
     * Convierte código a descripción legible
     */
    public String getDescripcionSituacion(String codigo) {
        if (codigo == null) return "Normal";

        return switch (codigo) {
            case "1" -> "Normal";
            case "2" -> "Contingencia";
            case "3" -> "Sin internet";
            default -> "Normal";
        };
    }

    /**
     * Valida si el código de situación es válido
     */
    public boolean isCodigoValido(String codigo) {
        return codigo != null && (codigo.equals("1") || codigo.equals("2") || codigo.equals("3"));
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
        return "05".equals(tipoDocumento) || "06".equals(tipoDocumento) || "07".equals(
            tipoDocumento);
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

}