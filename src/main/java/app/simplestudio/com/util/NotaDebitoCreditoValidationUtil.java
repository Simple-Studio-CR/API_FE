package app.simplestudio.com.util;

import app.simplestudio.com.dto.ValidationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

// NotaDebitoCreditoValidationUtil.java
@Component
public class NotaDebitoCreditoValidationUtil {

    public ValidationResult validateInitialRequest(String facturaJson) {
        ValidationResult result = new ValidationResult();

        if (facturaJson == null || facturaJson.trim().isEmpty()) {
            result.setValid(false);
            result.setHttpCode(400);
            result.setErrorResponse(buildErrorResponse(400, "JSON de factura es requerido"));
            return result;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(facturaJson);
            result.setValid(true);
            return result;
        } catch (Exception e) {
            result.setValid(false);
            result.setHttpCode(400);
            result.setErrorResponse(buildErrorResponse(400, "JSON inválido: " + e.getMessage()));
            return result;
        }
    }

    public ValidationResult validateRequiredFields(JsonNode requestNode) {
        ValidationResult result = new ValidationResult();

        // Validar situación
        String situacion = requestNode.path("situacion").asText();
        if (situacion == null || situacion.trim().isEmpty()) {
            result.setValid(false);
            result.setHttpCode(401);
            result.setErrorResponse(buildErrorResponse(401, "La situación es requerida."));
            return result;
        }

        // Validar sucursal
        if (!requestNode.has("sucursal") || requestNode.path("sucursal").asText().trim().isEmpty()) {
            result.setValid(false);
            result.setHttpCode(401);
            result.setErrorResponse(buildErrorResponse(401, "La sucursal es requerida."));
            return result;
        }

        // Validar terminal
        if (!requestNode.has("terminal") || requestNode.path("terminal").asText().trim().isEmpty()) {
            result.setValid(false);
            result.setHttpCode(401);
            result.setErrorResponse(buildErrorResponse(401, "La terminal es requerida."));
            return result;
        }

        result.setValid(true);
        return result;
    }

    public boolean isValidDocumentType(String tipoDocumento) {
        return "ND".equalsIgnoreCase(tipoDocumento) || "NC".equalsIgnoreCase(tipoDocumento);
    }

    private Map<String, Object> buildErrorResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("response", code);
        response.put("msj", message);
        return response;
    }
}