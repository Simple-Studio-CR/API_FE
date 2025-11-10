package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DocumentoConsultaHelper {
    
    /**
     * Construye la respuesta para consulta de cualquier documento
     * 
     * @param jsonResponse Respuesta de Hacienda como JsonNode
     * @return ResponseEntity con los datos formateados
     */
    public ResponseEntity<?> construirRespuestaConsulta(JsonNode jsonResponse) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("clave", jsonResponse.path("clave").asText());
        response.put("fecha", jsonResponse.path("fecha").asText());
        response.put("ind-estado", jsonResponse.path("ind-estado").asText());
        response.put("respuesta-xml", jsonResponse.path("respuesta-xml").asText());
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Construye la respuesta para consulta de comprobante electrónico local
     * 
     * @param ce Comprobante electrónico
     * @return ResponseEntity con los datos del comprobante
     */
    public ResponseEntity<?> construirRespuestaComprobante(ComprobantesElectronicos ce) {
        Map<String, Object> response = new HashMap<>();
        
        String nameXml = (ce.getNameXmlAcceptacion() != null && !ce.getNameXmlAcceptacion().isEmpty()) 
            ? ce.getNameXmlAcceptacion() : "";
        
        String indEstado = (ce.getIndEstado() != null && !ce.getIndEstado().isEmpty()) 
            ? ce.getIndEstado() : "procesando";
        
        String fechaAceptacion = (ce.getFechaAceptacion() != null && !ce.getFechaAceptacion().isEmpty()) 
            ? ce.getFechaAceptacion() : "";
        
        response.put("response", 200);
        response.put("clave", ce.getClave());
        response.put("ind-estado", indEstado);
        response.put("xml-aceptacion", nameXml);
        response.put("fecha-aceptacion", fechaAceptacion);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Construye respuesta de error estándar
     * 
     * @param mensaje Mensaje de error
     * @param status HttpStatus del error
     * @return ResponseEntity con el error
     */
    public ResponseEntity<?> construirRespuestaError(String mensaje, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("response", status.value());
        response.put("msj", mensaje);
        return new ResponseEntity<>(response, status);
    }
}