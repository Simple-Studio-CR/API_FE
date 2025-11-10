package app.simplestudio.com.mh.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * Helper para manejo de moneda y tipo de cambio
 */
@Component
public class MonedaHelper {
    
    private static final String MONEDA_DEFAULT = "CRC";
    private static final String TIPO_CAMBIO_DEFAULT = "1.00";
    
    /**
     * Resultado con moneda y tipo de cambio
     */
    public static class MonedaResult {
        public final String moneda;
        public final String tipoCambio;
        
        public MonedaResult(String moneda, String tipoCambio) {
            this.moneda = moneda;
            this.tipoCambio = tipoCambio;
        }
    }
    
    /**
     * Obtiene la moneda y tipo de cambio del JsonNode, con valores por defecto CRC y 1.00
     * 
     * @param jsonNode JsonNode con los campos codMoneda y tipoCambio
     * @return MonedaResult con los valores de moneda y tipo de cambio
     */
    public MonedaResult obtenerMonedaYTipoCambio(JsonNode jsonNode) {
        String codMoneda = jsonNode.path("codMoneda").asText();
        
        if (codMoneda != null && !codMoneda.isEmpty()) {
            return new MonedaResult(
                codMoneda,
                jsonNode.path("tipoCambio").asText()
            );
        } else {
            return new MonedaResult(MONEDA_DEFAULT, TIPO_CAMBIO_DEFAULT);
        }
    }
    
    /**
     * Verifica si es moneda extranjera (diferente a CRC)
     * 
     * @param codMoneda Código de moneda
     * @return true si es moneda extranjera
     */
    public boolean esMonedaExtranjera(String codMoneda) {
        return codMoneda != null && !codMoneda.isEmpty() && !MONEDA_DEFAULT.equals(codMoneda);
    }
}