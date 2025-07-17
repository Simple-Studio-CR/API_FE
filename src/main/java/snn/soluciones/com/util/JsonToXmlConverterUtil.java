package snn.soluciones.com.util;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsonToXmlConverterUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JsonToXmlConverterUtil.class);
    
    @Autowired
    private XmlBuilderUtil xmlBuilderUtil;
    
    @Autowired
    private XmlValidationUtil xmlValidationUtil;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Convierte JSON de detalle de factura a XML
     */
    public String convertDetalleFacturaToXml(String detalleJson, String tipoDocumento) {
        if (detalleJson == null || detalleJson.trim().isEmpty()) {
            return "";
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(detalleJson);
            XmlBuilderUtil builder = xmlBuilderUtil.createDocument().openTag("DetalleServicio");
            
            Iterator<JsonNode> elementsDetalleFactura = rootNode.elements();
            while (elementsDetalleFactura.hasNext()) {
                JsonNode item = elementsDetalleFactura.next();
                processLineaDetalle(builder, item, tipoDocumento);
            }
            
            return builder.closeTag("DetalleServicio").build();
            
        } catch (Exception e) {
            log.error("Error procesando detalle de factura: {}", e.getMessage());
            return "<DetalleServicio></DetalleServicio>";
        }
    }
    
    /**
     * Procesa una línea de detalle individual
     */
    private void processLineaDetalle(XmlBuilderUtil builder, JsonNode item, String tipoDocumento) {
        builder.openTag("LineaDetalle")
               .addElement("NumeroLinea", item.path("numeroLinea").asText());
        
        // Partida arancelaria (no para tipos 01, 07, 04)
        if (!xmlValidationUtil.isFacturaEstandar(tipoDocumento)) {
            builder.addElementIf(xmlValidationUtil.isNotEmpty(item.path("partidaArancelaria").asText()),
                               "PartidaArancelaria", item.path("partidaArancelaria").asText());
        }
        
        builder.addElement("Codigo", item.path("codigo").asText());
        
        // Códigos comerciales
        String codigosComerciales = item.path("codigoComercial").toString();
        if (xmlValidationUtil.isNotEmpty(codigosComerciales)) {
            processCodigosComerciales(builder, codigosComerciales);
        }
        
        builder.addElement("Cantidad", item.path("cantidad").asText())
               .addElement("UnidadMedida", item.path("unidadMedida").asText())
               .addElementIf(xmlValidationUtil.isNotEmpty(item.path("unidadMedidaComercial").asText()),
                           "UnidadMedidaComercial", item.path("unidadMedidaComercial").asText())
               .addElementWithEscape("Detalle", item.path("detalle").asText())
               .addElement("PrecioUnitario", item.path("precioUnitario").asText())
               .addElement("MontoTotal", item.path("montoTotal").asText());
        
        // Descuentos
        String descuentos = item.path("descuentos").toString();
        if (xmlValidationUtil.isNotEmpty(descuentos)) {
            processDescuentos(builder, descuentos);
        }
        
        builder.addElement("SubTotal", item.path("subTotal").asText())
               .addElementIf(xmlValidationUtil.isNotEmpty(item.path("baseImponible").asText()),
                           "BaseImponible", item.path("baseImponible").asText());
        
        // Impuestos
        String impuestosJson = item.path("impuestos").toString();
        if (xmlValidationUtil.isNotEmpty(impuestosJson)) {
            processImpuestos(builder, impuestosJson, tipoDocumento);
        }
        
        builder.addElement("ImpuestoNeto", item.path("impuestoNeto").asText())
               .addElement("MontoTotalLinea", item.path("montoTotalLinea").asText())
               .closeTag("LineaDetalle");
    }
    
    /**
     * Procesa códigos comerciales
     */
    private void processCodigosComerciales(XmlBuilderUtil builder, String codigosJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(codigosJson);
            Iterator<JsonNode> elements = rootNode.elements();
            
            while (elements.hasNext()) {
                JsonNode codigo = elements.next();
                builder.openTag("CodigoComercial")
                       .addElement("Tipo", xmlValidationUtil.formatearCodigoComercial(codigo.path("tipo").asText()))
                       .addElementWithEscape("Codigo", codigo.path("codigo").asText())
                       .closeTag("CodigoComercial");
            }
        } catch (Exception e) {
            log.error("Error procesando códigos comerciales: {}", e.getMessage());
        }
    }
    
    /**
     * Procesa descuentos
     */
    private void processDescuentos(XmlBuilderUtil builder, String descuentosJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(descuentosJson);
            Iterator<JsonNode> elements = rootNode.elements();
            
            while (elements.hasNext()) {
                JsonNode descuento = elements.next();
                if (descuento.get("montoDescuento").asDouble() > 0.0) {
                    builder.openTag("Descuento")
                           .addElement("MontoDescuento", xmlValidationUtil.formatearMonto(descuento.get("montoDescuento").asText()))
                           .addElementWithEscape("NaturalezaDescuento", descuento.path("naturalezaDescuento").asText())
                           .closeTag("Descuento");
                }
            }
        } catch (Exception e) {
            log.error("Error procesando descuentos: {}", e.getMessage());
        }
    }
    
    /**
     * Procesa impuestos
     */
    private void processImpuestos(XmlBuilderUtil builder, String impuestosJson, String tipoDocumento) {
        try {
            JsonNode rootNode = objectMapper.readTree(impuestosJson);
            Iterator<JsonNode> elements = rootNode.elements();
            
            while (elements.hasNext()) {
                JsonNode impuesto = elements.next();
                builder.openTag("Impuesto")
                       .addElement("Codigo", xmlValidationUtil.formatearCodigoImpuesto(impuesto.path("codigo").asText()))
                       .addElement("CodigoTarifa", xmlValidationUtil.formatearCodigoTarifa(impuesto.path("codigoTarifa").asText()))
                       .addElement("Tarifa", impuesto.path("tarifa").asText())
                       .addElementIf(xmlValidationUtil.isNotEmpty(impuesto.path("factorIVA").asText()),
                                   "FactorIVA", impuesto.path("factorIVA").asText())
                       .addElement("Monto", impuesto.path("monto").asText());
                
                // Exoneración (no para tipo 09)
                if (xmlValidationUtil.requiresExoneracion(tipoDocumento)) {
                    processExoneracion(builder, impuesto.path("exoneracion"));
                }
                
                builder.closeTag("Impuesto");
            }
        } catch (Exception e) {
            log.error("Error procesando impuestos: {}", e.getMessage());
        }
    }
    
    /**
     * Procesa exoneración de impuestos
     */
    private void processExoneracion(XmlBuilderUtil builder, JsonNode exoneracion) {
        if (xmlValidationUtil.isNotEmpty(exoneracion.path("tipoDocumento").asText())) {
            builder.openTag("Exoneracion")
                   .addElement("TipoDocumento", xmlValidationUtil.formatearTipoDocumento(exoneracion.path("tipoDocumento").asText()))
                   .addElement("NumeroDocumento", exoneracion.path("numeroDocumento").asText())
                   .addElementWithEscape("NombreInstitucion", exoneracion.path("nombreInstitucion").asText())
                   .addElement("FechaEmision", exoneracion.path("fechaEmision").asText())
                   .addElement("PorcentajeExoneracion", exoneracion.path("porcentajeExoneracion").asText())
                   .addElement("MontoExoneracion", exoneracion.path("montoExoneracion").asText())
                   .closeTag("Exoneracion");
        }
    }
    
    /**
     * Convierte JSON de otros cargos a XML
     */
    public String convertOtrosCargosToXml(String otrosCargosJson) {
        if (otrosCargosJson == null || otrosCargosJson.trim().isEmpty()) {
            return "";
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(otrosCargosJson);
            XmlBuilderUtil builder = xmlBuilderUtil.createDocument();
            
            Iterator<JsonNode> elements = rootNode.elements();
            while (elements.hasNext()) {
                JsonNode cargo = elements.next();
                builder.openTag("OtrosCargos")
                       .addElement("TipoDocumento", xmlValidationUtil.formatearTipoDocumento(cargo.path("tipoDocumento").asText()))
                       .addElementIf(xmlValidationUtil.isNotEmpty(cargo.path("numeroIdentidadTercero").asText()),
                                   "NumeroIdentidadTercero", cargo.path("numeroIdentidadTercero").asText())
                       .addElementIf(xmlValidationUtil.isNotEmpty(cargo.path("nombreTercero").asText()),
                                   "NombreTercero", cargo.path("nombreTercero").asText())
                       .addElementWithEscape("Detalle", cargo.path("detalle").asText())
                       .addElementIf(xmlValidationUtil.isNotEmpty(cargo.path("porcentaje").asText()),
                                   "Porcentaje", cargo.path("porcentaje").asText())
                       .addElement("MontoCargo", cargo.path("montoCargo").asText())
                       .closeTag("OtrosCargos");
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("Error procesando otros cargos: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Convierte JSON de referencias a XML
     */
    public String convertReferenciasToXml(String referenciasJson) {
        if (referenciasJson == null || referenciasJson.trim().isEmpty()) {
            return "";
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(referenciasJson);
            XmlBuilderUtil builder = xmlBuilderUtil.createDocument();
            
            Iterator<JsonNode> elements = rootNode.elements();
            while (elements.hasNext()) {
                JsonNode referencia = elements.next();
                String numero = referencia.path("numero").asText();
                
                builder.openTag("InformacionReferencia")
                       .addElement("TipoDoc", numero.substring(29, 31)) // Extraer tipo del número
                       .addElement("Numero", numero)
                       .addElement("FechaEmision", referencia.path("fechaEmision").asText())
                       .addElement("Codigo", xmlValidationUtil.formatearTipoDocumento(referencia.path("codigo").asText()))
                       .addElementWithEscape("Razon", referencia.path("razon").asText())
                       .closeTag("InformacionReferencia");
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("Error procesando referencias: {}", e.getMessage());
            return "";
        }
    }
}