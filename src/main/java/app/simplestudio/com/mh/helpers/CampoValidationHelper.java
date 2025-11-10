package app.simplestudio.com.mh.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper para validación de campos requeridos
 */
@Component
public class CampoValidationHelper {
    
    /**
     * Valida que un campo no sea null ni vacío
     * 
     * @param jsonNode JsonNode principal
     * @param campo Nombre del campo a validar
     * @return true si el campo es válido (no null y no vacío)
     */
    public boolean esCampoValido(JsonNode jsonNode, String campo) {
        JsonNode node = jsonNode.path(campo);
        return node != null && !node.asText().isEmpty();
    }
    
    /**
     * Valida un campo y retorna su valor si es válido
     * 
     * @param jsonNode JsonNode principal
     * @param campo Nombre del campo
     * @param mensajeError Mensaje de error si el campo no es válido
     * @return El valor del campo
     * @throws CampoRequeridoException si el campo no es válido
     */
    public String validarCampoRequerido(JsonNode jsonNode, String campo, String mensajeError) 
            throws CampoRequeridoException {
        if (!esCampoValido(jsonNode, campo)) {
            throw new CampoRequeridoException(mensajeError);
        }
        return jsonNode.path(campo).asText();
    }
    
    /**
     * Valida un campo opcional y retorna su valor o null
     * 
     * @param jsonNode JsonNode principal
     * @param campo Nombre del campo
     * @return El valor del campo o null si no existe o está vacío
     */
    public String obtenerCampoOpcional(JsonNode jsonNode, String campo) {
        if (esCampoValido(jsonNode, campo)) {
            return jsonNode.path(campo).asText();
        }
        return null;
    }
    
    /**
     * Valida un campo opcional y retorna su valor o un valor por defecto
     * 
     * @param jsonNode JsonNode principal
     * @param campo Nombre del campo
     * @param valorDefecto Valor por defecto si el campo no existe o está vacío
     * @return El valor del campo o el valor por defecto
     */
    public String obtenerCampoConDefecto(JsonNode jsonNode, String campo, String valorDefecto) {
        if (esCampoValido(jsonNode, campo)) {
            return jsonNode.path(campo).asText();
        }
        return valorDefecto;
    }
    
    /**
     * Crea una respuesta de error 401 para campo requerido
     * 
     * @param mensaje Mensaje de error
     * @return ResponseEntity con el error
     */
    public ResponseEntity<?> respuestaCampoRequerido(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("response", 401);
        response.put("msj", mensaje);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Excepción personalizada para campos requeridos
     */
    public static class CampoRequeridoException extends Exception {
        public CampoRequeridoException(String mensaje) {
            super(mensaje);
        }
    }
}