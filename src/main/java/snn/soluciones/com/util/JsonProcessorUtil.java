package snn.soluciones.com.util;


import snn.soluciones.com.mh.ComprobanteElectronico;
import snn.soluciones.com.mh.MensajeReceptorMh;
import snn.soluciones.com.mh.ObligadoTributario;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component
public class JsonProcessorUtil {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XmlParserUtil xmlParserUtil;
    
    public JsonProcessorUtil(XmlParserUtil xmlParserUtil) {
        this.xmlParserUtil = xmlParserUtil;
    }
    
    /**
     * Convierte JSON string a Map
     */
    public Map<String, Object> parseJsonToMap(String jsonContent) throws Exception {
        return objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * Convierte objeto a JSON string
     */
    public String convertToJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
    
    /**
     * Procesa XML de Factura Electrónica y genera JSON
     */
    public String processFacturaElectronica(Document xml, String tipoDocumento) throws Exception {
        ComprobanteElectronico comprobante = new ComprobanteElectronico();
        ObligadoTributario emisor = new ObligadoTributario();
        ObligadoTributario receptor = new ObligadoTributario();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        
        // Mapeo de rutas XPath según tipo de documento
        String rootPath = getRootPathForDocument(tipoDocumento);
        
        // Extraer datos comunes
        comprobante.setClave(xmlParserUtil.extractNodeValue(xml, rootPath + "/Clave"));
        
        // Emisor
        emisor.setTipoIdentificacion(xmlParserUtil.extractNodeValue(xml, rootPath + "/Emisor/Identificacion/Tipo"));
        emisor.setNumeroIdentificacion(xmlParserUtil.extractNodeValue(xml, rootPath + "/Emisor/Identificacion/Numero"));
        
        // Receptor (puede no existir en algunos tipos)
        if (xmlParserUtil.hasNodeContent(xml, rootPath + "/Receptor/Identificacion/Numero")) {
            receptor.setTipoIdentificacion(xmlParserUtil.extractNodeValue(xml, rootPath + "/Receptor/Identificacion/Tipo"));
            receptor.setNumeroIdentificacion(xmlParserUtil.extractNodeValue(xml, rootPath + "/Receptor/Identificacion/Numero"));
        }
        
        comprobante.setFecha(format.format(new Date()));
        comprobante.setEmisor(emisor);
        comprobante.setReceptor(receptor);
        
        return convertToJson(comprobante);
    }
    
    /**
     * Procesa XML de Mensaje Receptor y genera JSON
     */
    public String processMensajeReceptor(Document xml, String tipoMensaje) throws Exception {
        MensajeReceptorMh mr = new MensajeReceptorMh();
        ObligadoTributario emisor = new ObligadoTributario();
        ObligadoTributario receptor = new ObligadoTributario();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        
        // Datos comunes para todos los mensajes receptor
        mr.setClave(xmlParserUtil.extractNodeValue(xml, "/MensajeReceptor/Clave"));
        mr.setFecha(xmlParserUtil.extractNodeValue(xml, "/MensajeReceptor/FechaEmisionDoc"));
        mr.setConsecutivoReceptor(xmlParserUtil.extractNodeValue(xml, "/MensajeReceptor/NumeroConsecutivoReceptor"));
        
        // Emisor y receptor (siempre cédula física tipo 01)
        emisor.setTipoIdentificacion("01");
        emisor.setNumeroIdentificacion(xmlParserUtil.extractNodeValue(xml, "/MensajeReceptor/NumeroCedulaEmisor"));
        
        receptor.setTipoIdentificacion("01");
        receptor.setNumeroIdentificacion(xmlParserUtil.extractNodeValue(xml, "/MensajeReceptor/NumeroCedulaReceptor"));
        
        mr.setFecha(format.format(new Date()));
        mr.setEmisor(emisor);
        mr.setReceptor(receptor);
        
        return convertToJson(mr);
    }
    
    /**
     * Obtiene la ruta XPath root según el tipo de documento
     */
    private String getRootPathForDocument(String tipoDocumento) {
      return switch (tipoDocumento) {
        case "FE" -> "/FacturaElectronica";
        case "ND" -> "/NotaDebitoElectronica";
        case "NC" -> "/NotaCreditoElectronica";
        case "TE" -> "/TiqueteElectronico";
        case "FEC" -> "/FacturaElectronicaCompra";
        case "FEE" -> "/FacturaElectronicaExportacion";
        default -> "/FacturaElectronica";
      };
    }
}