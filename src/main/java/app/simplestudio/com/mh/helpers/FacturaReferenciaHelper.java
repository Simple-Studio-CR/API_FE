package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.models.entity.FacturaReferencia;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import java.util.Iterator;

@Component
public class FacturaReferenciaHelper {

    /**
     * Obtiene la clave de referencia del JSON
     */
    public String obtenerClaveReferencia(JsonNode jsonNode) {
        String claveReferencia = jsonNode.path("informacionReferencia").path("numero").asText();

        if (claveReferencia == null || claveReferencia.isEmpty()) {
            claveReferencia = jsonNode.path("numero").asText();
        }

        return claveReferencia;
    }

    /**
     * Crea el JSON de referencia para NC/ND
     */
    public String crearReferenciaJson(String clave, String fechaEmision, String codigo, String razon) {
        StringBuilder jm = new StringBuilder();
        jm.append("{");
        jm.append("\"0\":{");
        jm.append("\"numero\":\"").append(clave).append("\",");
        jm.append("\"fechaEmision\":\"").append(fechaEmision).append("\",");
        jm.append("\"codigo\":\"").append(codigo).append("\",");
        jm.append("\"razon\":\"").append(razon).append("\"");
        jm.append("}");
        jm.append("}");

        return jm.toString();
    }

    /**
     * Procesa referencias y las agrega a la factura
     * Método genérico que puede ser usado por cualquier controlador
     */
    public void procesarReferenciasEnFactura(Factura factura, JsonNode referenciasNode) {
        Iterator<JsonNode> referencia = referenciasNode.elements();
        while (referencia.hasNext()) {
            JsonNode re = referencia.next();
            if (re.path("numero").asText() != null && re.path("numero").asText().length() == 50) {
                FacturaReferencia fr = new FacturaReferencia();
                fr.setTipoDoc(re.path("numero").asText().substring(29, 31));
                fr.setNumero(re.path("numero").asText());
                fr.setFechaEmision(re.path("fechaEmision").asText());
                fr.setCodigo(re.path("codigo").asText());
                fr.setRazon(re.path("razon").asText());
                factura.addReferenciaFactura(fr);
            }
        }
    }
}