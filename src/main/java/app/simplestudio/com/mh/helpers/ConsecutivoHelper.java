package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.models.entity.CTerminal;
import org.springframework.stereotype.Component;

/**
 * Helper para obtener consecutivos según el tipo de documento
 */
@Component
public class ConsecutivoHelper {
    
    /**
     * Obtiene el consecutivo correspondiente según el tipo de documento
     * 
     * @param cTerminal Terminal con los consecutivos
     * @param tipoDocumento Tipo de documento (FE, TE, NC, ND, FEC, FEE, CCE, CPCE, RCE)
     * @return El consecutivo correspondiente o 0L si no se encuentra el tipo
     */
    public Long obtenerConsecutivoPorTipo(CTerminal cTerminal, String tipoDocumento) {
        if (cTerminal == null || tipoDocumento == null) {
            return 0L;
        }
        
        return switch (tipoDocumento.toUpperCase()) {
            case "FE" -> cTerminal.getConsecutivoFe();
            case "TE" -> cTerminal.getConsecutivoTe();
            case "NC" -> cTerminal.getConsecutivoNc();
            case "ND" -> cTerminal.getConsecutivoNd();
            case "FEC" -> cTerminal.getConsecutivoFEC();
            case "FEE" -> cTerminal.getConsecutivoFEE();
            case "CCE" -> cTerminal.getConsecutivoCCE();
            case "CPCE" -> cTerminal.getConsecutivoCPCE();
            case "RCE" -> cTerminal.getConsecutivoRCE();
            default -> 0L;
        };
    }
    
    /**
     * Obtiene el código de mensaje para tipos de documento MR (Mensaje Receptor)
     * 
     * @param tipoDocumento Tipo de documento (CCE, CPCE, RCE)
     * @return El código de mensaje correspondiente o null si no aplica
     */
    public String obtenerCodigoMensajeMR(String tipoDocumento) {
        if (tipoDocumento == null) {
            return null;
        }
        
        return switch (tipoDocumento.toUpperCase()) {
            case "CCE" -> "1";
            case "CPCE" -> "2";
            case "RCE" -> "3";
            default -> null;
        };
    }
}