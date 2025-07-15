package app.simplestudio.com.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.mh.IGeneraXml;
import app.simplestudio.com.mh.ISigner;
import app.simplestudio.com.mh.Sender;
import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.models.entity.ExoneracionImpuestoItemFactura;
import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.models.entity.FacturaOtrosCargos;
import app.simplestudio.com.models.entity.FacturaReferencia;
import app.simplestudio.com.models.entity.ImpuestosItemFactura;
import app.simplestudio.com.models.entity.ItemFactura;
import app.simplestudio.com.models.entity.MensajeReceptor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IFacturaService;
import app.simplestudio.com.service.IMensajeReceptorService;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@RestController
@RequestMapping({"/api-4.3"})
public class RecepcionController {
  @Autowired
  private IGeneraXml _generaXml;
  
  @Autowired
  private FuncionesService _funcionesService;
  
  @Autowired
  private ISigner _signer;
  
  @Autowired
  private Sender _sender;
  
  @Autowired
  private IEmisorService _emisorService;
  
  @Autowired
  private IMensajeReceptorService _mensajeReceptorService;
  
  @Autowired
  private IComprobantesElectronicosService _comprobantesElectronicosService;
  
  @Value("${path.upload.files.api}")
  private String pathUploadFilesApi;
  
  @Value("${endpoint.prod}")
  private String endpointProd;
  
  @Value("${endpoint.stag}")
  private String endpointStag;
  
  @Value("${token.prod}")
  private String tokenProd;
  
  @Value("${token.stag}")
  private String tokenStag;
  
  @Autowired
  public JavaMailSender emailSender;
  
  @Autowired
  private IFacturaService _facturaService;
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private String _endpoint;
  
  private String _username;
  
  private String _password;
  
  private String _urlToken;
  
  private String _clientId;
  
  private String _certificado;
  
  private String _keyCertificado;
  
  @RequestMapping(value = {"/consultar-cualquier-documento"}, method = {RequestMethod.POST}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> consultaCualquierDocumento(@RequestBody String j) throws Exception {
    Map<String, Object> response = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode m = objectMapper.readTree(j);
    try {
      String tokenAccess = m.path("tokenAccess").asText().trim();
      Emisor e = this._emisorService.findEmisorByIdentificacion(m.path("emisor").asText(), tokenAccess);
      if (e != null) {
        if (e.getAmbiente().equals("prod")) {
          this._endpoint = this.endpointProd;
          this._urlToken = this.tokenProd;
          this._clientId = "api-prod";
        } else {
          this._endpoint = this.endpointStag;
          this._urlToken = this.tokenStag;
          this._clientId = "api-stag";
        } 
        this._username = e.getUserApi();
        this._password = e.getPwApi();
        String clave = m.path("clave").asText();
        String resturApi = this._sender.consultarEstadoCualquierDocumento(this._endpoint, clave, this._username, this._password, this._urlToken, this.pathUploadFilesApi + "/" + e
            .getIdentificacion() + "/", this._clientId, e
            .getIdentificacion());
        JsonNode d = objectMapper.readTree(resturApi);
        response.put("clave", d.path("clave").asText());
        response.put("fecha", d.path("fecha").asText());
        response.put("ind-estado", d.path("ind-estado").asText());
        response.put("respuesta-xml", d.path("respuesta-xml").asText());
        return new ResponseEntity<>(response, HttpStatus.OK);
      } 
      response.put("response", "Acceso denegado.");
      return new ResponseEntity<>(response, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
    } catch (Exception e) {
      response.put("response", "Problemas con Hacienda.");
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    } 
  }
  
  @RequestMapping(value = {"/consultar-documentos-externos"}, method = {RequestMethod.POST}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> consultaDocumentoExterno(@RequestBody String j) throws IOException {
    Map<String, Object> response = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode m = objectMapper.readTree(j);
    try {
      String tokenAccess = m.path("tokenAccess").asText().trim();
      Emisor e = this._emisorService.findEmisorByIdentificacion(m.path("emisor").asText(), tokenAccess);
      if (e != null) {
        if (e.getAmbiente().equals("prod")) {
          this._endpoint = this.endpointProd;
          this._urlToken = this.tokenProd;
          this._clientId = "api-prod";
        } else {
          this._endpoint = this.endpointStag;
          this._urlToken = this.tokenStag;
          this._clientId = "api-stag";
        } 
        this._username = e.getUserApi();
        this._password = e.getPwApi();
        String clave = m.path("clave").asText();
        String resturApi = this._sender.consultarEstadoCualquierDocumento(this._endpoint, clave, this._username, this._password, this._urlToken, this.pathUploadFilesApi + "/" + e
            .getIdentificacion() + "/", this._clientId, e
            .getIdentificacion());
        JsonNode d = objectMapper.readTree(resturApi);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(new String(Base64.decodeBase64(d.path("respuesta-xml").asText()), "UTF-8")));
        Document doc = db.parse(is);
        NodeList nodes = doc.getElementsByTagName("MensajeHacienda");
        String mensajeMh = "";
        for (int i = 0; i < nodes.getLength(); i++) {
          Element element = (Element)nodes.item(i);
          NodeList name = element.getElementsByTagName("DetalleMensaje");
          Element line = (Element)name.item(0);
          mensajeMh = StringEscapeUtils.escapeJava(getCharacterDataFromElement(line));
        } 
        response.put("response-code", 200);
        response.put("clave", d.path("clave").asText());
        response.put("fecha", d.path("fecha").asText());
        response.put("ind-estado", d.path("ind-estado").asText());
        response.put("respuesta-xml", mensajeMh);
        return new ResponseEntity<>(response, HttpStatus.OK);
      } 
      response.put("response", "Acceso denegado.");
      return new ResponseEntity<>(response, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
    } catch (Exception e) {
      response.put("response", "Problemas con Hacienda.");
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    } 
  }
  
  @RequestMapping(value = {"/consultar-documentos"}, method = {RequestMethod.POST}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> consultarDocumentos(@RequestBody String j) {
    Map<String, Object> response = new HashMap<>();
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode m = objectMapper.readTree(j);
      String tokenAccess = m.path("tokenAccess").asText().trim();
      Emisor e = this._emisorService.findEmisorByIdentificacion(m.path("emisor").asText(), tokenAccess);
      if (e != null) {
        ComprobantesElectronicos ce = this._comprobantesElectronicosService.findByClaveDocumento(m.path("clave").asText());
        String nameXml = (ce.getNameXmlAcceptacion() != null && !ce.getNameXmlAcceptacion()
            .isEmpty()) ? ce.getNameXmlAcceptacion() : "";
        String indEstado = (ce.getIndEstado() != null && !ce.getIndEstado().isEmpty()) ? ce.getIndEstado() : "procesando";
        String fechaAceptacion = (ce.getFechaAceptacion() != null && !ce.getFechaAceptacion()
            .isEmpty()) ? ce.getFechaAceptacion() : "";
        response.put("response", 200);
        response.put("clave", ce.getClave());
        response.put("ind-estado", indEstado);
        response.put("xml-aceptacion", nameXml);
        response.put("fecha-aceptacion", fechaAceptacion);
        return new ResponseEntity<>(response, HttpStatus.OK);
      } 
      response.put("response", "Acceso denegado.");
      return new ResponseEntity<>(response, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
    } catch (Exception e) {
      response.put("response", "Problemas con Hacienda.");
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    } 
  }
  
  @RequestMapping(value = {"/recepcion"}, method = {RequestMethod.POST}, consumes = {"application/json; charset=utf-8"}, produces = {"application/json; charset=utf-8"})
  public ResponseEntity<?> getFactura(@RequestBody String j) throws Exception {
    Map<String, Object> response = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode m;
    m = objectMapper.readTree(j);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String fechaEmision = format.format(new Date()) + "-06:00";
    Long consecutivoCe;
    Long consecutivoEm = null;
    String tipoDocumento = m.path("tipoDocumento").asText();
    String situacion = m.path("situacion").asText();
    String sucursal = m.path("sucursal").asText();
    String terminal = m.path("terminal").asText();
    String emisor = m.path("emisor").asText();
    String numeroFactura = (m.path("numeroFactura").asText() != null) ? m.path("numeroFactura").asText() : "";
    String clave = "";
    String claveCliente = (m.path("clave").asText() != null) ? m.path("clave").asText() : "";
    String fechaEmisionCliente = (m.path("fechaEmisionCliente").asText() != null) ? m.path("fechaEmisionCliente").asText().trim() : "";
    Long consecutivoFinal = Long.parseLong("0");
    if (m.path("omitirReceptor").asText().equalsIgnoreCase("true"))
      if (tipoDocumento.equalsIgnoreCase("FE") || tipoDocumento.equalsIgnoreCase("FEC")) {
        response.put("response", 401);
        response.put("msj", "Los datos del receptor nombre y identificación (tipo y número) son requeridos para Factura Electrónica y para Factura Electrónica Compra!!!");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
      }  
    if (m.path("omitirReceptor").asText().equalsIgnoreCase("false"))
      if (m.path("receptorNombre") != null && !m.path("receptorNombre").asText().isEmpty()) {
        if (m.path("receptorTipoIdentif") != null && !m.path("receptorTipoIdentif").asText()
            .isEmpty()) {
          if (m.path("receptorNumIdentif") == null || m.path("receptorNumIdentif").asText().length() <= 0) {
            response.put("response", 401);
            response.put("msj", "El número de identificación es requerido!!!");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
          } 
        } else {
          response.put("response", 401);
          response.put("msj", "El tipo de identificación es requerido!!!");
          return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } 
      } else {
        response.put("response", 401);
        response.put("msj", "El nombre del receptor es requerido!!!");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
      }  
    Long eliminarConsecutivo = Long.parseLong("0");
    if (claveCliente.length() == 50) {
      clave = m.path("clave").asText();
      fechaEmision = m.path("fechaEmision").asText();
    } else if (situacion != null && !situacion.isEmpty()) {
      if (sucursal != null && !sucursal.isEmpty()) {
        if (terminal == null || terminal.length() <= 0) {
          response.put("response", 401);
          response.put("msj", "La terminal es requerida.");
          return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } 
      } else {
        response.put("response", 401);
        response.put("msj", "La sucursal es requerida.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
      } 
    } else {
      response.put("response", 401);
      response.put("msj", "La situación es requerida.");
      return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    } 
    try {
      String tokenAccess = m.path("tokenAccess").asText().trim();
      Emisor e = this._emisorService.findEmisorByIdentificacion(emisor, tokenAccess);
      CCampoFactura c = new CCampoFactura();
      if (e != null) {
        this.log.info("El emisor existe es valido");
        if (claveCliente.length() != 50) {
          CTerminal csu = this._emisorService.findBySecuenciaByTerminal(e.getId(), m.path("sucursal").asInt(), m.path("terminal").asInt());
          if (csu != null) {
            switch (tipoDocumento) {
              case "FE":
                consecutivoEm = csu.getConsecutivoFe();
                break;
              case "TE":
                consecutivoEm = csu.getConsecutivoTe();
                break;
              case "NC":
                consecutivoEm = csu.getConsecutivoNc();
                break;
              case "ND":
                consecutivoEm = csu.getConsecutivoNd();
                break;
              case "FEC":
                consecutivoEm = csu.getConsecutivoFEC();
                break;
              case "FEE":
                consecutivoEm = csu.getConsecutivoFEE();
                break;
            } 
          } else {
            response.put("response", 401);
            response.put("msj", "La sucursal o la terminal no existen.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
          } 
          ComprobantesElectronicos ce = this._comprobantesElectronicosService.findByEmisor(emisor, tipoDocumento.trim(), m.path("sucursal").asInt(), m.path("terminal").asInt(), e.getAmbiente());
          if (ce != null) {
            consecutivoCe = ce.getConsecutivo() + 1L;
          } else {
            consecutivoCe = Long.parseLong("1");
          } 
          consecutivoFinal = (consecutivoCe < consecutivoEm) ? consecutivoEm : consecutivoCe;
          String generaClave = this._sender.getClave(tipoDocumento, "0" + e.getTipoDeIdentificacion().getId(), emisor, m
              .path("situacion").asText(), e.getCodigoPais(), consecutivoFinal
              .toString(), this._funcionesService.getCodigoSeguridad(8), m
              .path("sucursal").asText(), m.path("terminal").asText());
          JsonNode d = objectMapper.readTree(generaClave);
          clave = d.path("clave").asText();
        } 
        String fullPath = this.pathUploadFilesApi + "/" + emisor + "/";
        String nameFacturaXml = clave + "-factura";
        String nameOutFacturaXml = this.pathUploadFilesApi + "/" + emisor + "/" + clave + "-factura-sign";
        String nameFacturaFirmada = nameFacturaXml + "-sign.xml";
        ComprobantesElectronicos cms = new ComprobantesElectronicos();
        cms.setEmisor(e);
        cms.setConsecutivo(consecutivoFinal);
        cms.setTipoDocumento(tipoDocumento);
        cms.setIdentificacion(emisor);
        cms.setClave(clave);
        cms.setFechaEmision(fechaEmision);
        cms.setNameXml(nameFacturaXml);
        cms.setNameXmlSign(clave + "-factura-sign");
        cms.setAmbiente(e.getAmbiente());
        cms.setEmailDistribucion(m.path("receptorEmail").asText());
        cms.setSucursal(m.path("sucursal").asInt());
        cms.setTerminal(m.path("terminal").asInt());
        this._comprobantesElectronicosService.save(cms);
        eliminarConsecutivo = cms.getId();
        c.setClave(clave);
        c.setCodigoActividad(e.getCodigoActividad());
        c.setConsecutivo(clave.substring(21, 41));
        c.setFechaEmision(fechaEmision);
        c.setOmitirReceptor(m.path("omitirReceptor").asText());
        if (tipoDocumento.equalsIgnoreCase("FEC")) {
          c.setEmisorNombre(m.path("receptorNombre").asText());
          c.setEmisorTipoIdentif(m.path("receptorTipoIdentif").asText());
          c.setEmisorNumIdentif(m.path("receptorNumIdentif").asText());
          if (m.path("receptorProvincia") != null) {
            c.setEmisorProv(m.path("receptorProvincia").asText());
          } else {
            response.put("response", 401);
            response.put("msj", "Campo provincia es requerida.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
          } 
          if (m.path("receptorCanton") != null) {
            c.setEmisorCanton(m.path("receptorCanton").asText());
          } else {
            response.put("response", 401);
            response.put("msj", "Campo cantón es requerido.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
          } 
          if (m.path("receptorDistrito") != null) {
            c.setEmisorDistrito(m.path("receptorDistrito").asText());
          } else {
            response.put("response", 401);
            response.put("msj", "Campo distrito es requerido.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
          } 
          if (m.path("receptorBarrio") != null)
            c.setEmisorBarrio(m.path("receptorBarrio").asText()); 
          if (e.getOtrasSenas() != null) {
            c.setEmisorOtrasSenas(m.path("receptorOtrasSenas").asText());
          } else {
            response.put("response", 401);
            response.put("msj", "Campo Otras señas es requerido.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
          } 
          c.setEmisorCodPaisTel(m.path("receptorCodPaisTel").asText());
          c.setEmisorTel(m.path("receptorTel").asText());
          c.setEmisorCodPaisFax(m.path("receptorCodPaisFax").asText());
          if (m.path("receptorFax") != null)
            c.setEmisorFax(m.path("receptorFax").asText()); 
          c.setEmisorEmail(m.path("receptorEmail").asText());
          c.setReceptorNombre(e.getNombreRazonSocial());
          c.setReceptorTipoIdentif("0" + e.getTipoDeIdentificacion().getId().toString());
          c.setReceptorNumIdentif(emisor);
          if (m.path("receptorTipoIdentif") != null && m.path("receptorTipoIdentif").asText().equals("05")) {
            c.setReceptorIdentificacionExtranjero(m.path("receptorIdentificacionExtranjero").asText());
            c.setOtrasSenasExtranjero(m.path("otrasSenasExtranjero").asText());
          } 
          if (e.getProvincia() != null && e.getCanton() != null && e.getDistrito() != null && e.getOtrasSenas() != null) {
            c.setReceptorProvincia(e.getProvincia().getId().toString());
            c.setReceptorCanton(this._funcionesService.str_pad(e.getCanton().getNumeroCanton(), 2, "0", "STR_PAD_LEFT"));
            c.setReceptorDistrito(this._funcionesService.str_pad(e.getDistrito().getNumeroDistrito(), 2, "0", "STR_PAD_LEFT"));
            if (e.getBarrio() != null)
              c.setReceptorBarrio(this._funcionesService.str_pad(e.getBarrio().getNumeroBarrio(), 2, "0", "STR_PAD_LEFT")); 
            c.setReceptorOtrasSenas(e.getOtrasSenas());
          } else {
            response.put("response", 401);
            response.put("msj", "Campo provincia, cantón, distrito y otras señas son requeridos para la Factura de Compra.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
          } 
          c.setReceptorCodPaisTel((e.getCodigoPais() != null) ? e.getCodigoPais() : "");
          c.setReceptorTel(e.getTelefono());
          c.setReceptorCodPaisFax(e.getCodigoPais());
          if (e.getFax() != null)
            c.setReceptorFax(e.getFax()); 
          c.setReceptorEmail(e.getEmail());
        } else {
          c.setEmisorNombre(e.getNombreRazonSocial());
          c.setEmisorTipoIdentif("0" + e.getTipoDeIdentificacion().getId().toString());
          c.setEmisorNumIdentif(emisor);
          if (e.getNombreComercial() != null)
            c.setNombreComercial(e.getNombreComercial()); 
          if (e.getProvincia() != null)
            c.setEmisorProv(e.getProvincia().getId().toString()); 
          if (e.getCanton() != null)
            c.setEmisorCanton(this._funcionesService.str_pad(e.getCanton().getNumeroCanton(), 2, "0", "STR_PAD_LEFT")); 
          if (e.getDistrito() != null)
            c.setEmisorDistrito(this._funcionesService.str_pad(e.getDistrito().getNumeroDistrito(), 2, "0", "STR_PAD_LEFT")); 
          if (e.getBarrio() != null)
            c.setEmisorBarrio(this._funcionesService.str_pad(e.getBarrio().getNumeroBarrio(), 2, "0", "STR_PAD_LEFT")); 
          if (e.getOtrasSenas() != null)
            c.setEmisorOtrasSenas(e.getOtrasSenas()); 
          c.setEmisorCodPaisTel(e.getCodigoPais());
          c.setEmisorTel(e.getTelefono());
          c.setEmisorCodPaisFax(e.getCodigoPais());
          if (e.getFax() != null)
            c.setEmisorFax(e.getFax()); 
          c.setEmisorEmail(e.getEmail());
          c.setOmitirReceptor(m.path("omitirReceptor").asText());
          c.setReceptorNombre(m.path("receptorNombre").asText());
          c.setReceptorTipoIdentif(m.path("receptorTipoIdentif").asText());
          c.setReceptorNumIdentif(m.path("receptorNumIdentif").asText());
          if (m.path("receptorTipoIdentif") == null || m.path("receptorTipoIdentif").asText().equals("05"));
          if (m.path("receptorProvincia").asText() != null && m.path("receptorCanton").asText() != null && m.path("receptorDistrito").asText() != null && m.path("receptorOtrasSenas").asText() != null) {
            c.setReceptorProvincia(m.path("receptorProvincia").asText());
            c.setReceptorCanton(m.path("receptorCanton").asText());
            c.setReceptorDistrito(m.path("receptorDistrito").asText());
            if (m.path("receptorBarrio").asText() != null)
              c.setReceptorBarrio(m.path("receptorBarrio").asText()); 
            c.setReceptorOtrasSenas(m.path("receptorOtrasSenas").asText());
          } 
          c.setReceptorCodPaisTel(m.path("receptorCodPaisTel").asText());
          c.setReceptorTel(m.path("receptorTel").asText());
          c.setReceptorCodPaisFax(m.path("receptorCodPaisFax").asText());
          c.setReceptorFax(m.path("receptorFax").asText());
          c.setReceptorEmail(m.path("receptorEmail").asText());
        } 
        JsonNode detalleLineaNode;
        c.setDetalleFactura(m.path("detalleLinea").toString());
        detalleLineaNode = objectMapper.readTree(m.path("detalleLinea").toString());
        JsonNode referenciasNode;
        c.setReferencia(m.path("referencias").toString());
        referenciasNode = objectMapper.readTree(m.path("referencias").toString());
        String otrosCargos = m.path("otrosCargos").toString();
        JsonNode otrosCargosNode;
        c.setOtrosCargos(otrosCargos);
        otrosCargosNode = objectMapper.readTree(otrosCargos);
        c.setCondVenta(m.path("condVenta").asText());
        c.setPlazoCredito(m.path("plazoCredito").asText());
        c.setMedioPago(m.path("medioPago").asText());
        c.setMedioPago2(m.path("medioPago2").asText());
        c.setMedioPago3(m.path("medioPago3").asText());
        c.setMedioPago4(m.path("medioPago4").asText());
        c.setCodMoneda(m.path("codMoneda").asText());
        c.setTipoCambio(m.path("tipoCambio").asText());
        String moneda;
        String tipoCambio;
        if (m.path("codMoneda") != null && !m.path("codMoneda").asText().isEmpty()) {
          moneda = m.path("codMoneda").asText();
          tipoCambio = m.path("tipoCambio").asText();
        } else {
          moneda = "CRC";
          tipoCambio = "1.00";
        } 
        c.setCodMoneda(moneda);
        c.setTipoCambio(tipoCambio);
        c.setTotalServGravados(m.path("totalServGravados").asText());
        c.setTotalServExentos(m.path("totalServExentos").asText());
        c.setTotalServExonerado(m.path("totalServExonerado").asText());
        c.setTotalMercGravadas(m.path("totalMercGravadas").asText());
        c.setTotalMercExentas(m.path("totalMercExentas").asText());
        c.setTotalMercExonerada(m.path("totalMercExonerada").asText());
        c.setTotalGravados(m.path("totalGravados").asText());
        c.setTotalExentos(m.path("totalExentos").asText());
        c.setTotalExonerado(m.path("totalExonerado").asText());
        c.setTotalVentas(m.path("totalVentas").asText());
        c.setTotalDescuentos(m.path("totalDescuentos").asText());
        c.setTotalVentasNeta(m.path("totalVentasNeta").asText());
        c.setTotalImp(m.path("totalImp").asText());
        c.setTotalIVADevuelto(m.path("totalIVADevuelto").asText());
        c.setTotalOtrosCargos(m.path("totalOtrosCargos").asText());
        c.setTotalComprobante(m.path("totalComprobante").asText());
        c.setNumeroFactura(m.path("numeroFactura").asText());
        c.setOtros(m.path("otros").asText());
        this._generaXml.generateXml(fullPath, this._generaXml.GeneraXml(c), nameFacturaXml);
        if (e.getAmbiente().equals("prod")) {
          this._endpoint = this.endpointProd;
          this._urlToken = this.tokenProd;
          this._clientId = "api";
        } else {
          this._endpoint = this.endpointStag;
          this._urlToken = this.tokenStag;
          this._clientId = "api-stag";
        } 
        this._certificado = this.pathUploadFilesApi + "/" + emisor + "/cert/" + e.getCertificado();
        this._keyCertificado = e.getPingApi();
        this._username = e.getUserApi();
        this._password = e.getPwApi();
        this._signer.sign(this._certificado, this._keyCertificado, fullPath + nameFacturaXml + ".xml", nameOutFacturaXml + ".xml");
        Factura factura = new Factura();
        Iterator<JsonNode> elements = detalleLineaNode.elements();
        String itemImpuestos;
        String codigoComercial;
        String objDescuentos;
        while (elements.hasNext()) {
          JsonNode k = elements.next();
          ItemFactura item = new ItemFactura();
          item.setNumeroLinea(k.path("numeroLinea").asInt());
          item.setPartidaArancelaria(k.path("partidaArancelaria").asText());
          item.setCodigo(k.path("codigo").asText());
          codigoComercial = k.path("codigoComercial").toString();
          if (codigoComercial != null && !codigoComercial.isEmpty()) {
            m = objectMapper.readTree(codigoComercial);
            Iterator<JsonNode> codigosComerciales = m.elements();
            int countCC = 0;
            while (codigosComerciales.hasNext()) {
              JsonNode cc = codigosComerciales.next();
              countCC++;
              switch (countCC) {
                case 1:
                  item.setCodigoComercialTipo(cc.path("tipo").asText());
                  item.setCodigoComercialCodigo(cc.path("codigo").asText());
                case 2:
                  item.setCodigoComercialTipo2(cc.path("tipo").asText());
                  item.setCodigoComercialCodigo2(cc.path("codigo").asText());
                case 3:
                  item.setCodigoComercialTipo3(cc.path("tipo").asText());
                  item.setCodigoComercialCodigo3(cc.path("codigo").asText());
                case 4:
                  item.setCodigoComercialTipo4(cc.path("tipo").asText());
                  item.setCodigoComercialCodigo4(cc.path("codigo").asText());
                case 5:
                  item.setCodigoComercialTipo5(cc.path("tipo").asText());
                  item.setCodigoComercialCodigo5(cc.path("codigo").asText());
              } 
            } 
          } 
          item.setCantidad(k.path("cantidad").asDouble());
          item.setUnidadMedida(k.path("unidadMedida").asText());
          item.setUnidadMedidaComercial(k.path("unidadMedidaComercial").asText());
          item.setDetalle(k.path("detalle").asText());
          item.setPrecioUnitario(k.path("precioUnitario").asDouble());
          item.setMontoTotal(k.path("montoTotal").asDouble());
          item.setSubTotal(k.path("subTotal").asDouble());
          objDescuentos = k.path("descuentos").toString();
          if (objDescuentos != null && !objDescuentos.isEmpty()) {
            m = objectMapper.readTree(objDescuentos);
            Iterator<JsonNode> descuentos = m.elements();
            int countCC = 0;
            while (descuentos.hasNext()) {
              JsonNode dd = descuentos.next();
              countCC++;
              switch (countCC) {
                case 1:
                  item.setMontoDescuento(dd.path("montoDescuento").asDouble());
                  item.setNaturalezaDescuento(dd.path("naturalezaDescuento").asText());
                case 2:
                  item.setMontoDescuento2(dd.path("montoDescuento").asDouble());
                  item.setNaturalezaDescuento2(dd.path("naturalezaDescuento").asText());
                case 3:
                  item.setMontoDescuento3(dd.path("montoDescuento").asDouble());
                  item.setNaturalezaDescuento3(dd.path("naturalezaDescuento").asText());
                case 4:
                  item.setMontoDescuento4(dd.path("montoDescuento").asDouble());
                  item.setNaturalezaDescuento4(dd.path("naturalezaDescuento").asText());
                case 5:
                  item.setMontoDescuento5(dd.path("montoDescuento").asDouble());
                  item.setNaturalezaDescuento5(dd.path("naturalezaDescuento").asText());
              } 
            } 
          } 
          itemImpuestos = k.path("impuestos").toString();
          if (itemImpuestos != null && !itemImpuestos.isEmpty()) {
            m = objectMapper.readTree(itemImpuestos);
            Iterator<JsonNode> impuestos = m.elements();
            while (impuestos.hasNext()) {
              ImpuestosItemFactura iif = new ImpuestosItemFactura();
              JsonNode imp = impuestos.next();
              iif.setCodigo(imp.path("codigo").asText());
              iif.setCodigoTarifa(imp.path("codigoTarifa").asText());
              iif.setFactorIva(imp.path("factorIva").asDouble());
              iif.setTarifa(imp.path("tarifa").asDouble());
              iif.setMonto(imp.path("monto").asDouble());
              iif.setMontoExportacion(imp.path("montoExportacion").asDouble());
              ExoneracionImpuestoItemFactura eiif = new ExoneracionImpuestoItemFactura();
              if (imp.path("exoneracion").path("tipoDocumento").asText() != null && !imp.path(
                  "exoneracion").path("tipoDocumento").asText().isEmpty()) {
                if (imp.path("exoneracion").path("tipoDocumento").asText() != null && !imp.path(
                    "exoneracion").path("tipoDocumento").asText().isEmpty()) {
                  eiif.setTipoDocumento(imp.path("exoneracion").path("tipoDocumento").asText());
                } else {
                  response.put("response", 401);
                  response.put("msj", "Tipo de documento de exoneración es requerido.");
                  return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                } 
                if (imp.path("exoneracion").path("numeroDocumento").asText() != null && !imp.path(
                    "exoneracion").path("numeroDocumento").asText().isEmpty()) {
                  eiif.setNumeroDocumento(imp.path("exoneracion").path("numeroDocumento").asText());
                } else {
                  response.put("response", 401);
                  response.put("msj", "Número de exoneración es requerido.");
                  return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                } 
                if (imp.path("exoneracion").path("nombreInstitucion").asText() != null && !imp.path(
                    "exoneracion").path("nombreInstitucion").asText().isEmpty()) {
                  eiif.setNombreInstitucion(imp.path("exoneracion").path("nombreInstitucion").asText());
                } else {
                  response.put("response", 401);
                  response.put("msj", "Nombre de institución exonerada es requerido.");
                  return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                } 
                if (imp.path("exoneracion").path("fechaEmision") != null && !imp.path("exoneracion")
                    .path("fechaEmision").asText().isEmpty()) {
                  eiif.setFechaEmision(imp.path("exoneracion").path("fechaEmision").asText());
                } else {
                  response.put("response", 401);
                  response.put("msj", "Fecha de emisión de exoneración es requerido.");
                  return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                } 
                if (imp.path("exoneracion").path("montoExoneracion").asText() != null && !imp.path(
                    "exoneracion").path("montoExoneracion").asText().isEmpty()) {
                  eiif.setMontoExoneracion(
                      imp.path("exoneracion").path("montoExoneracion").asDouble());
                } else {
                  response.put("response", 401);
                  response.put("msj", "Monto de impuesto de exoneración es requerido.");
                  return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                } 
                if (imp.path("exoneracion").path("porcentajeExoneracion").asText() != null && !imp.path(
                    "exoneracion").path("porcentajeExoneracion").asText().isEmpty()) {
                  eiif.setPorcentajeExoneracion(imp.path("exoneracion").path("porcentajeExoneracion").asInt());
                } else {
                  response.put("response", 401);
                  response.put("msj", "Porcentaje de exoneración es requerido.");
                  return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                } 
                iif.addItemFacturaImpuestosExoneracion(eiif);
              } 
              item.addItemFacturaImpuestos(iif);
            } 
          } 
          item.setImpuestoNeto(k.path("impuestoNeto").asDouble());
          item.setMontoTotalLinea(k.path("montoTotalLinea").asDouble());
          factura.addItemFactura(item);
        } 
        if (otrosCargos != null && !otrosCargos.isEmpty()) {
          Iterator<JsonNode> otroCargo = otrosCargosNode.elements();
          int contador = 0;
          while (otroCargo.hasNext()) {
            FacturaOtrosCargos oc = new FacturaOtrosCargos();
            JsonNode o = otroCargo.next();
            oc.setTipoDocumento(o.path("tipoDocumento").asText());
            oc.setNumeroIdentidadTercero(o.path("numeroIdentidadTercero").asText());
            oc.setNombreTercero(o.path("nombreTercero").asText());
            oc.setDetalle(o.path("detalle").asText());
            oc.setPorcentaje(o.path("porcentaje").asText());
            oc.setMontoCargo(o.path("montoCargo").asText());
            contador++;
            if (contador <= 15)
              factura.addOtrosCargos(oc); 
          } 
        } 
        factura.setTipoDocumento(tipoDocumento);
        factura.setClave(c.getClave());
        factura.setConsecutivo(c.getConsecutivo());
        factura.setCodigoActividad(c.getConsecutivo());
        factura.setCodigoActividad(c.getCodigoActividad());
        factura.setFechaEmision(c.getFechaEmision());
        factura.setEmisorNombre(c.getEmisorNombre());
        factura.setEmisorTipoIdentif(c.getEmisorTipoIdentif());
        factura.setEmisorNumIdentif(c.getEmisorNumIdentif());
        factura.setNombreComercial(c.getNombreComercial());
        factura.setEmisorProv(c.getEmisorProv());
        factura.setEmisorCanton(c.getEmisorCanton());
        factura.setEmisorDistrito(c.getEmisorDistrito());
        factura.setEmisorBarrio(c.getEmisorBarrio());
        factura.setEmisorOtrasSenas(c.getEmisorOtrasSenas());
        factura.setEmisorCodPaisTel(c.getEmisorCodPaisTel());
        factura.setEmisorTel(c.getEmisorTel());
        factura.setEmisorCodPaisFax(c.getEmisorCodPaisFax());
        factura.setEmisorFax(c.getEmisorFax());
        factura.setEmisorEmail(c.getEmisorEmail());
        factura.setIdentificacion(emisor);
        factura.setSituacion(situacion);
        factura.setTerminal(this._funcionesService.str_pad(terminal, 5, "0", "STR_PAD_LEFT"));
        factura.setSucursal(this._funcionesService.str_pad(sucursal, 3, "0", "STR_PAD_LEFT"));
        factura.setOmitirReceptor(c.getOmitirReceptor());
        factura.setReceptorNombre(c.getReceptorNombre());
        factura.setReceptorTipoIdentif(c.getReceptorTipoIdentif());
        factura.setReceptor_num_identif(c.getReceptorNumIdentif());
        factura.setReceptorProvincia(c.getReceptorProvincia());
        factura.setReceptorCanton(c.getReceptorCanton());
        factura.setReceptorDistrito(c.getReceptorDistrito());
        factura.setReceptorBarrio(c.getReceptorBarrio());
        factura.setReceptorOtrasSenas(c.getReceptorOtrasSenas());
        factura.setOtrasSenasExtranjero(c.getOtrasSenasExtranjero());
        factura.setReceptorCodPaisTel(c.getReceptorCodPaisTel());
        factura.setReceptorTel(c.getReceptorTel());
        factura.setReceptorCodPaisFax(c.getReceptorCodPaisFax());
        factura.setReceptorFax(c.getReceptorFax());
        factura.setReceptorEmail(c.getReceptorEmail());
        factura.setCondVenta(c.getCondVenta());
        factura.setPlazoCredito(c.getPlazoCredito());
        factura.setMedioPago(c.getMedioPago());
        factura.setMedioPago2(c.getMedioPago2());
        factura.setMedioPago3(c.getMedioPago3());
        factura.setMedioPago4(c.getMedioPago4());
        factura.setCodMoneda(c.getCodMoneda());
        factura.setTipoCambio(c.getTipoCambio());
        String referencias = m.path("referencias").toString();
        if (referencias != null && !referencias.isEmpty()) {
          Iterator<JsonNode> referencia = referenciasNode.elements();
          while (referencia.hasNext()) {
            FacturaReferencia fr = new FacturaReferencia();
            JsonNode re = referencia.next();
            if (re.path("numero").asText() != null && re.path("numero").asText().length() == 50) {
              fr.setTipoDoc(re.path("numero").asText().substring(29, 31));
              fr.setNumero(re.path("numero").asText());
              fr.setFechaEmision(re.path("fechaEmision").asText());
              fr.setCodigo(re.path("codigo").asText());
              fr.setRazon(re.path("razon").asText());
              factura.addReferenciaFactura(fr);
            } 
          } 
        } 
        factura.setCodMoneda(c.getCodMoneda());
        factura.setTipoCambio(c.getTipoCambio());
        factura.setTotalServGravados(c.getTotalServGravados());
        factura.setTotalServExentos(c.getTotalServExentos());
        factura.setTotalServExonerado(c.getTotalServExonerado());
        factura.setTotalMercGravadas(c.getTotalMercGravadas());
        factura.setTotalMercExentas(c.getTotalMercExentas());
        factura.setTotalMercExonerada(c.getTotalMercExonerada());
        factura.setTotalGravados(c.getTotalGravados());
        factura.setTotalExentos(c.getTotalExentos());
        factura.setTotalExonerado(c.getTotalExonerado());
        factura.setTotalVentas(c.getTotalVentas());
        factura.setTotalDescuentos(c.getTotalDescuentos());
        factura.setTotalVentaNeta(c.getTotalVentasNeta());
        factura.setTotalImp(c.getTotalImp());
        factura.setTotalIVADevuelto(c.getTotalIVADevuelto());
        factura.setTotalOtrosCargos(c.getTotalOtrosCargos());
        factura.setTotalComprobante(c.getTotalComprobante());
        factura.setOtros(c.getOtros());
        factura.setNumeroFactura(numeroFactura);
        this._facturaService.save(factura);
        response.put("response", 200);
        response.put("clave", c.getClave());
        response.put("consecutivo", c.getConsecutivo());
        response.put("fechaEmision", c.getFechaEmision());
        response.put("fileXmlSign", nameFacturaFirmada);
        return new ResponseEntity<>(response, HttpStatus.OK);
      } 
      this.log.info("El emisor no existe");
      response.put("response", 401);
      response.put("msj", "El usuario o token no éxiste.");
      return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    } catch (Exception e) {
      e.printStackTrace();
      this._comprobantesElectronicosService.deleteById(eliminarConsecutivo);
      response.put("response", 401);
      response.put("msj", "Error generado, revise bien el JSON que esta enviando.");
      return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    } finally {
      emisor = "";
      this._certificado = "";
      this._keyCertificado = "";
    } 
  }
  
  @RequestMapping(value = {"/recepcion-mr"}, method = {RequestMethod.POST}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> RecepcionMr(@RequestBody String j) throws Exception {
    Map<String, Object> response = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode m;
    m = objectMapper.readTree(j);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String fechaEmision = format.format(new Date()) + "-06:00";
    Long consecutivoCe;
    Long consecutivoEm = 0L;
    String tipoDocumento = m.path("tipoDocumento").asText();
    String emisor = m.path("emisor").asText();
    String tokenAccess = m.path("tokenAccess").asText().trim();
    Emisor e = this._emisorService.findEmisorByIdentificacion(m.path("emisor").asText(), tokenAccess);
    if (e != null) {
      this.log.info("El emisor es valido");
      if (e.getAmbiente().equals("prod")) {
        this._endpoint = this.endpointProd;
        this._urlToken = this.tokenProd;
        this._clientId = "api";
      } else {
        this._endpoint = this.endpointStag;
        this._urlToken = this.tokenStag;
        this._clientId = "api-stag";
      } 
      CTerminal csu = this._emisorService.findBySecuenciaByTerminal(e.getId(), m.path("sucursal").asInt(), m.path("terminal").asInt());
      if (csu != null) {
        switch (tipoDocumento) {
          case "CCE":
            consecutivoEm = csu.getConsecutivoCCE();
            break;
          case "CPCE":
            consecutivoEm = csu.getConsecutivoCPCE();
            break;
          case "RCE":
            consecutivoEm = csu.getConsecutivoRCE();
            break;
        } 
      } else {
        response.put("response", 401);
        response.put("msj", "La sucursal o la terminal no existen.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
      } 
      this._certificado = this.pathUploadFilesApi + "/" + emisor + "/cert/" + e.getCertificado();
      this._keyCertificado = e.getPingApi();
      this._username = e.getUserApi();
      this._password = e.getPwApi();
      CCampoFactura c = new CCampoFactura();
      ComprobantesElectronicos ce = this._comprobantesElectronicosService.findByEmisor(emisor, tipoDocumento.trim(), m.path("sucursal").asInt(), m.path("terminal").asInt(), e.getAmbiente());
      if (ce != null) {
        consecutivoCe = ce.getConsecutivo() + 1L;
        this.log.info("Consecutivo actual " + consecutivoCe);
      } else {
        consecutivoCe = Long.parseLong("1");
        this.log.info("Asigno 1 al consecutivo " + consecutivoCe);
      } 
      Long consecutivoFinal = (consecutivoCe < consecutivoEm) ? consecutivoEm : consecutivoCe;
      String generaClave = this._sender.getClave(tipoDocumento, "0" + e.getTipoDeIdentificacion().getId(), emisor, m
          .path("situacion").asText(), e.getCodigoPais(), consecutivoFinal.toString(), this._funcionesService
          .getCodigoSeguridad(8), m.path("sucursal").asText(), m.path("terminal").asText());
      JsonNode d = objectMapper.readTree(generaClave);
      String fullPath = this.pathUploadFilesApi + "/" + emisor + "/";
      String nameFacturaXml = d.path("clave").asText() + "-mr";
      String nameOutFacturaXml = this.pathUploadFilesApi + "/" + emisor + "/" + d.path("clave").asText() + "-mr-sign";
      ComprobantesElectronicos cms = new ComprobantesElectronicos();
      cms.setEmisor(e);
      cms.setConsecutivo(consecutivoFinal);
      cms.setTipoDocumento(tipoDocumento);
      cms.setIdentificacion(emisor);
      cms.setClave(d.path("clave").asText());
      cms.setFechaEmision("N/A MR");
      cms.setNameXml(nameFacturaXml);
      cms.setNameXmlSign(d.path("clave").asText() + "-mr-sign");
      cms.setAmbiente(e.getAmbiente());
      cms.setSucursal(m.path("sucursal").asInt());
      cms.setTerminal(m.path("terminal").asInt());
      this._comprobantesElectronicosService.save(cms);
      switch (tipoDocumento) {
        case "CCE":
          c.setMensaje("1");
          break;
        case "CPCE":
          c.setMensaje("2");
          break;
        case "RCE":
          c.setMensaje("3");
          break;
      } 
      c.setClave(d.path("clave").asText());
      c.setNumeroConsecutivoReceptor(d.path("consecutivo").asText());
      c.setDetalleMensaje(m.path("detalleMensaje").asText());
      c.setCodigoActividad(m.path("codigo_actividad").asText());
      c.setCondicionImpuesto(m.path("condicion_impuesto").asText());
      Double m1 = (m.path("monto_total_impuesto_acreditar") != null
          && m.path("monto_total_impuesto_acreditar").asDouble() > 0.0D) ? m.path(
          "monto_total_impuesto_acreditar").asDouble() : 0.0D;
      Double m2 = (m.path("monto_total_de_gasto_aplicable") != null
          && m.path("monto_total_de_gasto_aplicable").asDouble() > 0.0D) ? m.path(
          "monto_total_de_gasto_aplicable").asDouble() : 0.0D;
      c.setMontoTotalImpuestoAcreditar(m1.toString());
      c.setMontoTotalDeGastoAplicable(m2.toString());
      c.setNumeroCedulaEmisor(m.path("NumeroCedulaEmisor").asText());
      c.setFechaEmisionDoc(m.path("fechaEmisionDoc").asText());
      c.setMontoTotalImpuesto(m.path("montoTotalImpuesto").asText());
      c.setTotalFactura(m.path("totalFactura").asText());
      c.setClaveDocumentoEmisor(m.path("clave").asText());
      c.setNumeroCedulaReceptor(emisor);
      this._generaXml.generateXml(fullPath, this._generaXml.GeneraXml(c), nameFacturaXml);
      this._signer.sign(this._certificado, this._keyCertificado, fullPath + nameFacturaXml + ".xml", nameOutFacturaXml + ".xml");
      MensajeReceptor mr = new MensajeReceptor();
      mr.setClave(d.path("clave").asText());
      mr.setTipoDocumento(tipoDocumento);
      mr.setNumeroCedulaEmisor(m.path("NumeroCedulaEmisor").asText());
      mr.setFechaEmisionDoc(m.path("fechaEmisionDoc").asText());
      mr.setMensaje(m.path("mensaje").asText());
      mr.setDetalleMensaje(m.path("detalleMensaje").asText());
      mr.setCodigoActividad(m.path("codigo_actividad").asText());
      mr.setCondicionImpuesto(m.path("condicion_impuesto").asText());
      mr.setMontoTotalImpuestoAcreditar(m.path("monto_total_impuesto_acreditar").asText());
      mr.setMontoTotalDeGastoAplicable(m.path("monto_total_de_gasto_aplicable").asText());
      mr.setMontoTotalImpuesto(m.path("montoTotalImpuesto").asText());
      mr.setTotalFactura(m.path("totalFactura").asText());
      mr.setClaveDocumentoEmisor(m.path("clave").asText());
      mr.setNumeroCedulaReceptor(emisor);
      this._mensajeReceptorService.save(mr);
      response.put("response", 200);
      response.put("clave", c.getClave());
      response.put("consecutivo", c.getNumeroConsecutivoReceptor());
      response.put("fechaEmision", fechaEmision);
      response.put("fileXmlSign", nameFacturaXml + "-sign.xml");
      return new ResponseEntity<>(response, HttpStatus.OK);
    } 
    response.put("response", 401);
    response.put("msj", "El usuario o token no éxiste");
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }
  
  public static String getCharacterDataFromElement(Element e) {
    Node child = e.getFirstChild();
    if (child instanceof CharacterData) {
      CharacterData cd = (CharacterData)child;
      return cd.getData();
    } 
    return "";
  }
}

