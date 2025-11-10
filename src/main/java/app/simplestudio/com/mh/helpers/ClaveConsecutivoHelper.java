package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.mh.Sender;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper para manejar la generación de claves y consecutivos
 */
@Component
@Slf4j
public class ClaveConsecutivoHelper {
    
    @Autowired
    private IComprobantesElectronicosService comprobantesElectronicosService;
    
    @Autowired
    private Sender sender;
    
    @Autowired
    private FuncionesService funcionesService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Resultado de la generación de clave y consecutivos
     */
    public static class ClaveConsecutivoResult {
        public final Long consecutivoCe;
        public final Long consecutivoEm;
        public final Long consecutivoFinal;
        public final String clave;
        public final JsonNode claveJson;
        
        public ClaveConsecutivoResult(Long consecutivoCe, Long consecutivoEm, 
                                    Long consecutivoFinal, String clave, JsonNode claveJson) {
            this.consecutivoCe = consecutivoCe;
            this.consecutivoEm = consecutivoEm;
            this.consecutivoFinal = consecutivoFinal;
            this.clave = clave;
            this.claveJson = claveJson;
        }
    }
    
    /**
     * Genera la clave y maneja los consecutivos
     * 
     * @param emisor Objeto emisor
     * @param emisorId Identificación del emisor
     * @param tipoDocumento Tipo de documento
     * @param sucursal Número de sucursal
     * @param terminal Número de terminal
     * @param situacion Situación del documento
     * @param consecutivoEmisor Consecutivo del emisor (de CTerminal)
     * @return ClaveConsecutivoResult con toda la información generada
     */
    public ClaveConsecutivoResult generarClaveYConsecutivo(
            Emisor emisor,
            String emisorId,
            String tipoDocumento,
            int sucursal,
            int terminal,
            String situacion,
            Long consecutivoEmisor) throws Exception {
        
        // Obtener el consecutivo del comprobante electrónico
        Long consecutivoCe;
        ComprobantesElectronicos ce = comprobantesElectronicosService.findByEmisor(
            emisorId, 
            tipoDocumento.trim(), 
            sucursal, 
            terminal, 
            emisor.getAmbiente()
        );
        
        if (ce != null) {
            consecutivoCe = ce.getConsecutivo() + 1L;
            log.info("Consecutivo actual: {}", consecutivoCe);
        } else {
            consecutivoCe = 1L;
            log.info("Asignando 1 al consecutivo");
        }
        
        // Calcular el consecutivo final
        Long consecutivoFinal = (consecutivoCe < consecutivoEmisor) ? consecutivoEmisor : consecutivoCe;
        
        // Generar la clave
        String generaClave = sender.getClave(
            tipoDocumento,
            "0" + emisor.getTipoDeIdentificacion().getId(),
            emisorId,
            situacion,
            emisor.getCodigoPais(),
            consecutivoFinal.toString(),
            funcionesService.getCodigoSeguridad(8),
            String.valueOf(sucursal),
            String.valueOf(terminal)
        );
        
        JsonNode claveJson = objectMapper.readTree(generaClave);
        String clave = claveJson.path("clave").asText();
        
        return new ClaveConsecutivoResult(
            consecutivoCe,
            consecutivoEmisor,
            consecutivoFinal,
            clave,
            claveJson
        );
    }
    
    /**
     * Versión simplificada cuando ya tienes todos los parámetros del JsonNode
     */
    public ClaveConsecutivoResult generarClaveYConsecutivo(
            Emisor emisor,
            String tipoDocumento,
            JsonNode jsonNode,
            Long consecutivoEmisor) throws Exception {
        
        return generarClaveYConsecutivo(
            emisor,
            jsonNode.path("emisor").asText(),
            tipoDocumento,
            jsonNode.path("sucursal").asInt(),
            jsonNode.path("terminal").asInt(),
            jsonNode.path("situacion").asText(),
            consecutivoEmisor
        );
    }
}