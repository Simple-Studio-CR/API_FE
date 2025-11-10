package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.mh.helpers.CampoValidationHelper.CampoRequeridoException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class ReceptorHelper {
    
    /**
     * Configura los datos del receptor para documentos normales
     */
    public void configurarDatosReceptor(CCampoFactura c, JsonNode m) {
        c.setReceptorNombre(m.path("receptorNombre").asText());
        c.setReceptorTipoIdentif(m.path("receptorTipoIdentif").asText());
        c.setReceptorNumIdentif(m.path("receptorNumIdentif").asText());
        
        // Si es identificación extranjera
        if ("05".equals(m.path("receptorTipoIdentif").asText())) {
            c.setReceptorIdentificacionExtranjero(m.path("receptorIdentificacionExtranjero").asText());
            c.setOtrasSenasExtranjero(m.path("otrasSenasExtranjero").asText());
        }
        
        // Datos de ubicación opcionales
        if (m.path("receptorProvincia").asText() != null && !m.path("receptorProvincia").asText().isEmpty()) {
            c.setReceptorProvincia(m.path("receptorProvincia").asText());
            c.setReceptorCanton(m.path("receptorCanton").asText());
            c.setReceptorDistrito(m.path("receptorDistrito").asText());
            c.setReceptorBarrio(m.path("receptorBarrio").asText());
            c.setReceptorOtrasSenas(m.path("receptorOtrasSenas").asText());
        }
        
        // Datos de contacto
        c.setReceptorCodPaisTel(m.path("receptorCodPaisTel").asText());
        c.setReceptorTel(m.path("receptorTel").asText());
        c.setReceptorCodPaisFax(m.path("receptorCodPaisFax").asText());
        c.setReceptorFax(m.path("receptorFax").asText());
        c.setReceptorEmail(m.path("receptorEmail").asText());
    }
    
    /**
     * Configura el receptor como emisor para Factura Electrónica de Compra (FEC)
     */
    public void configurarReceptorComoEmisor(CCampoFactura c, JsonNode m, CampoValidationHelper validator) 
            throws CampoRequeridoException {
        
        c.setEmisorNombre(m.path("receptorNombre").asText());
        c.setEmisorTipoIdentif(m.path("receptorTipoIdentif").asText());
        c.setEmisorNumIdentif(m.path("receptorNumIdentif").asText());
        
        // Validar y asignar campos requeridos para FEC
        c.setEmisorProv(validator.validarCampoRequerido(
            m, "receptorProvincia", "Campo provincia es requerida."));
        c.setEmisorCanton(validator.validarCampoRequerido(
            m, "receptorCanton", "Campo cantón es requerido."));
        c.setEmisorDistrito(validator.validarCampoRequerido(
            m, "receptorDistrito", "Campo distrito es requerido."));
        
        c.setEmisorBarrio(validator.obtenerCampoOpcional(m, "receptorBarrio"));
        c.setEmisorOtrasSenas(m.path("receptorOtrasSenas").asText());
        
        // Datos de contacto
        c.setEmisorCodPaisTel(m.path("receptorCodPaisTel").asText());
        c.setEmisorTel(m.path("receptorTel").asText());
        c.setEmisorCodPaisFax(m.path("receptorCodPaisFax").asText());
        c.setEmisorFax(validator.obtenerCampoOpcional(m, "receptorFax"));
        c.setEmisorEmail(m.path("receptorEmail").asText());
    }
}