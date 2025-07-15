package app.simplestudio.com.util;

import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityMapperUtil {
    
    private static final Logger log = LoggerFactory.getLogger(EntityMapperUtil.class);
    
    @Autowired
    private XmlValidationUtil xmlValidationUtil;
    
    @Autowired
    private DocumentTypeUtil documentTypeUtil;
    
    /**
     * Mapea datos del emisor a CCampoFactura
     */
    public void mapEmisorToCCampoFactura(CCampoFactura c, Emisor emisor, boolean isReversedRole) {
        if (isReversedRole) {
            // Para facturas donde el emisor y receptor están invertidos (como FEC)
            mapEmisorAsReceptor(c, emisor);
        } else {
            // Mapeo normal del emisor
            mapEmisorAsEmisor(c, emisor);
        }
    }
    
    /**
     * Mapea emisor como emisor normal
     */
    private void mapEmisorAsEmisor(CCampoFactura c, Emisor emisor) {
        c.setEmisorNombre(emisor.getNombreRazonSocial());
        c.setEmisorTipoIdentif(formatTipoIdentificacion(emisor.getTipoDeIdentificacion().getId()));
        c.setEmisorNumIdentif(emisor.getIdentificacion());
        
        if (emisor.getNombreComercial() != null) {
            c.setNombreComercial(emisor.getNombreComercial());
        }
        
        // Ubicación del emisor
        mapUbicacionEmisor(c, emisor);
        
        // Contacto del emisor
        mapContactoEmisor(c, emisor);
    }
    
    /**
     * Mapea emisor como receptor (para casos especiales como FEC)
     */
    private void mapEmisorAsReceptor(CCampoFactura c, Emisor emisor) {
        c.setReceptorNombre(emisor.getNombreRazonSocial());
        c.setReceptorTipoIdentif(formatTipoIdentificacion(emisor.getTipoDeIdentificacion().getId()));
        c.setReceptorNumIdentif(emisor.getIdentificacion());
        
        // Ubicación del receptor
        mapUbicacionReceptor(c, emisor);
        
        // Contacto del receptor
        mapContactoReceptor(c, emisor);
    }
    
    /**
     * Mapea datos del request JSON al receptor
     */
    public void mapJsonToReceptor(CCampoFactura c, JsonNode requestData) {
        c.setOmitirReceptor(requestData.path("omitirReceptor").asText());
        c.setReceptorNombre(requestData.path("receptorNombre").asText());
        c.setReceptorTipoIdentif(requestData.path("receptorTipoIdentif").asText());
        c.setReceptorNumIdentif(requestData.path("receptorNumIdentif").asText());
        
        // Validar si es identificación extranjera
        if (xmlValidationUtil.isIdentificacionExtranjera(requestData.path("receptorTipoIdentif").asText())) {
            c.setReceptorIdentificacionExtranjero(requestData.path("receptorIdentificacionExtranjero").asText());
            c.setOtrasSenasExtranjero(requestData.path("otrasSenasExtranjero").asText());
        }
        
        // Ubicación del receptor (si no es extranjero)
        if (hasCompleteUbicacion(requestData, "receptor")) {
            c.setReceptorProvincia(requestData.path("receptorProvincia").asText());
            c.setReceptorCanton(requestData.path("receptorCanton").asText());
            c.setReceptorDistrito(requestData.path("receptorDistrito").asText());
            
            if (requestData.has("receptorBarrio")) {
                c.setReceptorBarrio(requestData.path("receptorBarrio").asText());
            }
            
            c.setReceptorOtrasSenas(requestData.path("receptorOtrasSenas").asText());
        }
        
        // Contacto del receptor
        c.setReceptorCodPaisTel(requestData.path("receptorCodPaisTel").asText());
        c.setReceptorTel(requestData.path("receptorTel").asText());
        c.setReceptorCodPaisFax(requestData.path("receptorCodPaisFax").asText());
        c.setReceptorFax(requestData.path("receptorFax").asText());
        c.setReceptorEmail(requestData.path("receptorEmail").asText());
    }
    
    /**
     * Mapea datos del request JSON al emisor como receptor (para FEC)
     */
    public void mapJsonToEmisorAsReceptor(CCampoFactura c, JsonNode requestData, Emisor emisor) {
        // Usar datos del request para el emisor
        c.setEmisorNombre(requestData.path("receptorNombre").asText());
        c.setEmisorTipoIdentif(requestData.path("receptorTipoIdentif").asText());
        c.setEmisorNumIdentif(requestData.path("receptorNumIdentif").asText());
        
        // Ubicación del emisor desde request
        if (hasCompleteUbicacion(requestData, "receptor")) {
            c.setEmisorProv(requestData.path("receptorProvincia").asText());
            c.setEmisorCanton(requestData.path("receptorCanton").asText());
            c.setEmisorDistrito(requestData.path("receptorDistrito").asText());
            
            if (requestData.has("receptorBarrio")) {
                c.setEmisorBarrio(requestData.path("receptorBarrio").asText());
            }
            
            c.setEmisorOtrasSenas(requestData.path("receptorOtrasSenas").asText());
        }
        
        // Contacto del emisor desde request
        c.setEmisorCodPaisTel(requestData.path("receptorCodPaisTel").asText());
        c.setEmisorTel(requestData.path("receptorTel").asText());
        c.setEmisorCodPaisFax(requestData.path("receptorCodPaisFax").asText());
        c.setEmisorFax(requestData.path("receptorFax").asText());
        c.setEmisorEmail(requestData.path("receptorEmail").asText());
        
        // Mapear entidad emisor como receptor
        mapEmisorAsReceptor(c, emisor);
    }
    
    /**
     * Mapea datos comunes de la factura
     */
    public void mapCommonInvoiceData(CCampoFactura c, JsonNode requestData, String clave, String fechaEmision) {
        c.setClave(clave);
        c.setFechaEmision(fechaEmision);
        c.setConsecutivo(documentTypeUtil.getConsecutivoFromClave(clave));
        
        // Mapear campos comunes
        c.setCodigoActividad(requestData.path("codigoActividad").asText());
        c.setCondVenta(requestData.path("condVenta").asText());
        c.setPlazoCredito(requestData.path("plazoCredito").asText());
        
        // Medios de pago
        c.setMedioPago(requestData.path("medioPago").asText());
        c.setMedioPago2(requestData.path("medioPago2").asText());
        c.setMedioPago3(requestData.path("medioPago3").asText());
        c.setMedioPago4(requestData.path("medioPago4").asText());
        
        // Moneda
        c.setCodMoneda(requestData.path("codMoneda").asText());
        c.setTipoCambio(requestData.path("tipoCambio").asText());
        
        // Totales
        mapTotales(c, requestData);
        
        // Otros datos
        c.setOtros(requestData.path("otros").asText());
        c.setDetalleFactura(requestData.path("detalleLinea").toString());
        c.setOtrosCargos(requestData.path("otrosCargos").toString());
        c.setReferencia(requestData.path("referencia").toString());
    }
    
    /**
     * Mapea totales de la factura
     */
    private void mapTotales(CCampoFactura c, JsonNode requestData) {
        c.setTotalServGravados(requestData.path("totalServGravados").asText());
        c.setTotalServExentos(requestData.path("totalServExentos").asText());
        c.setTotalServExonerado(requestData.path("totalServExonerado").asText());
        c.setTotalMercGravadas(requestData.path("totalMercGravadas").asText());
        c.setTotalMercExentas(requestData.path("totalMercExentas").asText());
        c.setTotalMercExonerada(requestData.path("totalMercExonerada").asText());
        c.setTotalGravados(requestData.path("totalGravados").asText());
        c.setTotalExentos(requestData.path("totalExentos").asText());
        c.setTotalExonerado(requestData.path("totalExonerado").asText());
        c.setTotalVentas(requestData.path("totalVentas").asText());
        c.setTotalDescuentos(requestData.path("totalDescuentos").asText());
        c.setTotalVentasNeta(requestData.path("totalVentasNeta").asText());
        c.setTotalImp(requestData.path("totalImp").asText());
        c.setTotalIVADevuelto(requestData.path("totalIVADevuelto").asText());
        c.setTotalOtrosCargos(requestData.path("totalOtrosCargos").asText());
        c.setTotalComprobante(requestData.path("totalComprobante").asText());
    }
    
    /**
     * Mapea ubicación del emisor
     */
    private void mapUbicacionEmisor(CCampoFactura c, Emisor emisor) {
        if (emisor.getProvincia() != null) {
            c.setEmisorProv(emisor.getProvincia().getId().toString());
        }
        if (emisor.getCanton() != null) {
            c.setEmisorCanton(xmlValidationUtil.formatearCodigoUbicacion(emisor.getCanton().getNumeroCanton()));
        }
        if (emisor.getDistrito() != null) {
            c.setEmisorDistrito(xmlValidationUtil.formatearCodigoUbicacion(emisor.getDistrito().getNumeroDistrito()));
        }
        if (emisor.getBarrio() != null) {
            c.setEmisorBarrio(xmlValidationUtil.formatearCodigoUbicacion(emisor.getBarrio().getNumeroBarrio()));
        }
        if (emisor.getOtrasSenas() != null) {
            c.setEmisorOtrasSenas(emisor.getOtrasSenas());
        }
    }
    
    /**
     * Mapea ubicación del receptor desde emisor
     */
    private void mapUbicacionReceptor(CCampoFactura c, Emisor emisor) {
        if (emisor.getProvincia() != null) {
            c.setReceptorProvincia(emisor.getProvincia().getId().toString());
        }
        if (emisor.getCanton() != null) {
            c.setReceptorCanton(xmlValidationUtil.formatearCodigoUbicacion(emisor.getCanton().getNumeroCanton()));
        }
        if (emisor.getDistrito() != null) {
            c.setReceptorDistrito(xmlValidationUtil.formatearCodigoUbicacion(emisor.getDistrito().getNumeroDistrito()));
        }
        if (emisor.getBarrio() != null) {
            c.setReceptorBarrio(xmlValidationUtil.formatearCodigoUbicacion(emisor.getBarrio().getNumeroBarrio()));
        }
        if (emisor.getOtrasSenas() != null) {
            c.setReceptorOtrasSenas(emisor.getOtrasSenas());
        }
    }
    
    /**
     * Mapea contacto del emisor
     */
    private void mapContactoEmisor(CCampoFactura c, Emisor emisor) {
        c.setEmisorCodPaisTel(emisor.getCodigoPais() != null ? emisor.getCodigoPais() : "");
        c.setEmisorTel(emisor.getTelefono());
        c.setEmisorCodPaisFax(emisor.getCodigoPais());
        if (emisor.getFax() != null) {
            c.setEmisorFax(emisor.getFax());
        }
        c.setEmisorEmail(emisor.getEmail());
    }
    
    /**
     * Mapea contacto del receptor desde emisor
     */
    private void mapContactoReceptor(CCampoFactura c, Emisor emisor) {
        c.setReceptorCodPaisTel(emisor.getCodigoPais() != null ? emisor.getCodigoPais() : "");
        c.setReceptorTel(emisor.getTelefono());
        c.setReceptorCodPaisFax(emisor.getCodigoPais());
        if (emisor.getFax() != null) {
            c.setReceptorFax(emisor.getFax());
        }
        c.setReceptorEmail(emisor.getEmail());
    }
    
    /**
     * Formatea tipo de identificación
     */
    private String formatTipoIdentificacion(Long tipoId) {
        return xmlValidationUtil.formatearTipoIdentificacion(tipoId.toString());
    }
    
    /**
     * Verifica si tiene ubicación completa
     */
    private boolean hasCompleteUbicacion(JsonNode data, String prefix) {
        return data.has(prefix + "Provincia") && 
               data.has(prefix + "Canton") && 
               data.has(prefix + "Distrito") && 
               data.has(prefix + "OtrasSenas");
    }
    
    /**
     * Crea ComprobantesElectronicos desde CCampoFactura
     */
    public ComprobantesElectronicos createComprobantesElectronicos(CCampoFactura c, Emisor emisor, String tipoDocumento, 
                                                                  Long consecutivo, String xmlFileName) {
        ComprobantesElectronicos ce = new ComprobantesElectronicos();
        
        ce.setClave(c.getClave());
        ce.setIdentificacion(emisor.getIdentificacion());
        ce.setTipoDocumento(tipoDocumento);
        ce.setAmbiente(emisor.getAmbiente());
        ce.setConsecutivo(consecutivo);
        ce.setNameXmlSign(xmlFileName);
        ce.setEmailDistribucion(c.getReceptorEmail());
        ce.setEmisor(emisor);
        
        return ce;
    }
    
    /**
     * Log de mapeo para debugging
     */
    public void logMappingDetails(CCampoFactura c) {
        log.debug("=== MAPEO COMPLETADO ===");
        log.debug("Clave: {}", c.getClave());
        log.debug("Emisor: {}", c.getEmisorNombre());
        log.debug("Receptor: {}", c.getReceptorNombre());
        log.debug("Total: {}", c.getTotalComprobante());
        log.debug("=========================");
    }
}