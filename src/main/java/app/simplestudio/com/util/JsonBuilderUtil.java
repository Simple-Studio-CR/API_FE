package app.simplestudio.com.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

// JsonBuilderUtil.java
@Component
public class JsonBuilderUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * REEMPLAZA LA CONCATENACIÓN MANUAL DE JSON PARA REFERENCIAS
     * Lógica original:
     * jm = jm + "{";
     * jm = jm + "\"0\":{";
     * jm = jm + "\"numero\":\"" + clave + "\",";
     * etc...
     */
    public String buildReferenciaJson(JsonNode requestNode) {
        ObjectNode referenciaNode = objectMapper.createObjectNode();
        ObjectNode referencia0 = objectMapper.createObjectNode();

        // Extraer datos de referencia del request
        String clave = "";
        String fechaEmisionFactura = "";
        String codigo = requestNode.path("informacionReferencia").path("codigo").asText();
        String razon = requestNode.path("informacionReferencia").path("razon").asText();

        if (requestNode.path("informacionReferencia").path("numero").asText() != null &&
            !requestNode.path("informacionReferencia").path("numero").asText().isEmpty()) {
            clave = requestNode.path("informacionReferencia").path("numero").asText();
        } else if (requestNode.path("numero").asText() != null &&
            !requestNode.path("numero").asText().isEmpty()) {
            clave = requestNode.path("numero").asText();
        }

        // Construir el JSON de referencia usando ObjectMapper
        referencia0.put("numero", clave);
        referencia0.put("fechaEmision", fechaEmisionFactura);
        referencia0.put("codigo", codigo);
        referencia0.put("razon", razon);

        referenciaNode.set("0", referencia0);

        return referenciaNode.toString();
    }

    /**
     * REEMPLAZA LA CONCATENACIÓN MANUAL DE JSON PARA DETALLE DE LÍNEA
     * Extrae la lógica del bucle gigante que construye el JSON de items
     */
    public String buildDetalleLineaJson(JsonNode requestNode) {
        // Esta función requiere acceso a la factura de referencia para extraer los items
        // Por ahora retornar JSON básico, pero la lógica completa está en el método
        // donde se hace el bucle sobre f.getItems()

        ObjectNode detalleNode = objectMapper.createObjectNode();
        // Aquí iría la lógica completa de construcción del detalle

        return detalleNode.toString();
    }
}