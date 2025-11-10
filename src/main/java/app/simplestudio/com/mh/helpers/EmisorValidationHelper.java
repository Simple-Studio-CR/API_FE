package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.IEmisorService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper para validar emisor y token de acceso
 */
@Component
public class EmisorValidationHelper {

    @Autowired
    private IEmisorService emisorService;

    /**
     * Valida el emisor y token de acceso desde un JsonNode
     *
     * @param jsonNode JsonNode con los campos tokenAccess y emisor
     * @return ValidacionResult con el emisor validado o null si no es válido
     */
    public ValidacionResult validarEmisor(JsonNode jsonNode) {
        String tokenAccess = jsonNode.path("tokenAccess").asText().trim();
        String emisorId = jsonNode.path("emisor").asText();

        Emisor emisor = emisorService.findEmisorByIdentificacion(emisorId, tokenAccess);

        return new ValidacionResult(emisor, emisorId);
    }

    /**
     * Crea una respuesta de error estándar para acceso denegado
     *
     * @return ResponseEntity con el error de acceso denegado
     */
    public ResponseEntity<?> respuestaAccesoDenegado() {
        Map<String, Object> response = new HashMap<>();
        response.put("response", "Acceso denegado.");
        return new ResponseEntity<>(response, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
    }

    /**
     * Crea una respuesta de error estándar para acceso denegado con mensaje personalizado
     *
     * @param mensaje Mensaje de error personalizado
     * @return ResponseEntity con el error
     */
    public ResponseEntity<?> respuestaError401(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("response", 401);
        response.put("msj", mensaje);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Crea una respuesta de error estándar para problemas con Hacienda
     *
     * @return ResponseEntity con el error de Hacienda
     */
    public ResponseEntity<?> respuestaProblemasHacienda() {
        Map<String, Object> response = new HashMap<>();
        response.put("response", "Problemas con Hacienda.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Valida que exista la terminal para el emisor
     *
     * @param emisorId ID del emisor
     * @param sucursal Número de sucursal
     * @param terminal Número de terminal
     * @return CTerminal si existe, null si no
     */
    public CTerminal validarTerminal(Long emisorId, int sucursal, int terminal) {
        return emisorService.findBySecuenciaByTerminal(emisorId, sucursal, terminal);
    }

    /**
     * Crea una respuesta de error para terminal no existente
     *
     * @return ResponseEntity con el error
     */
    public ResponseEntity<?> respuestaTerminalNoExiste() {
        Map<String, Object> response = new HashMap<>();
        response.put("response", 401);
        response.put("msj", "La sucursal o la terminal no existen.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Clase para encapsular el resultado de la validación
     */
    public static class ValidacionResult {
        public final Emisor emisor;
        public final String emisorId;

        public ValidacionResult(Emisor emisor, String emisorId) {
            this.emisor = emisor;
            this.emisorId = emisorId;
        }

        public boolean esValido() {
            return emisor != null;
        }
    }
}