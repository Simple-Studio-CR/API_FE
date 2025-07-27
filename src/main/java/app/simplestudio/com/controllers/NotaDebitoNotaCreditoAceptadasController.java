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
import app.simplestudio.com.models.entity.FacturaReferencia;
import app.simplestudio.com.models.entity.ImpuestosItemFactura;
import app.simplestudio.com.models.entity.ItemFactura;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IFacturaService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(maxAge = 3600L)
@RestController
@RequestMapping({"/api-4.3"})
public class NotaDebitoNotaCreditoAceptadasController {
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
  
  @CrossOrigin
  @RequestMapping(value = {"/recepcion-nd-nc"}, method = {RequestMethod.POST}, consumes = {"application/json; charset=utf-8"}, produces = {"application/json; charset=utf-8"})
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
    String codigo = m.path("codigo").asText();
    String razon = m.path("razon").asText();
    String emisor = m.path("emisor").asText();
    String clave = m.path("numero").asText();
    String fechaEmisionFactura;
    if (situacion != null && !situacion.isEmpty()) {
      if (sucursal != null && !sucursal.isEmpty()) {
        if (terminal != null && !terminal.isEmpty())
          try {
            String tokenAccess = m.path("tokenAccess").asText().trim();
            Emisor e = this._emisorService.findEmisorByIdentificacion(emisor, tokenAccess);
            if (e != null) {
              this.log.info("El emisor existe es valido");
              if ((tipoDocumento != null && tipoDocumento.equalsIgnoreCase("ND")) || tipoDocumento.equalsIgnoreCase("NC")) {
                String jsonFinal;
                if (e.getAmbiente().equals("prod")) {
                  this._endpoint = this.endpointProd;
                  this._urlToken = this.tokenProd;
                  this._clientId = "api";
                } else {
                  this._endpoint = this.endpointStag;
                  this._urlToken = this.tokenStag;
                  this._clientId = "api-stag";
                } 
                this._certificado = this.pathUploadFilesApi + "/" + e.getIdentificacion() + "/cert/" + e.getCertificado();
                this._keyCertificado = e.getPingApi();
                this._username = e.getUserApi();
                this._password = e.getPwApi();
                CTerminal csu = this._emisorService.findBySecuenciaByTerminal(e.getId(), m.path("sucursal").asInt(), m.path("terminal").asInt());
                if (csu != null) {
                  consecutivoEm = switch (tipoDocumento) {
                    case "NC" -> csu.getConsecutivoNc();
                    case "ND" -> csu.getConsecutivoNd();
                    default -> consecutivoEm;
                  };
                } else {
                  response.put("response", 401);
                  response.put("msj", "La sucursal o la terminal no existen.");
                  return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                } 
                CCampoFactura c = new CCampoFactura();
                ComprobantesElectronicos ce = this._comprobantesElectronicosService.findByEmisor(emisor, tipoDocumento.trim(), m.path("sucursal").asInt(), m.path("terminal").asInt(), e.getAmbiente());
                if (ce != null) {
                  consecutivoCe = ce.getConsecutivo() + 1L;
                } else {
                  consecutivoCe = Long.parseLong("1");
                } 
                Long consecutivoFinal = (consecutivoCe < consecutivoEm) ? consecutivoEm : consecutivoCe;
                String generaClave = this._sender.getClave(tipoDocumento, "0" + e.getTipoDeIdentificacion().getId(), emisor, m
                    .path("situacion").asText(), e.getCodigoPais(), consecutivoFinal
                    .toString(), this._funcionesService.getCodigoSeguridad(8), m
                    .path("sucursal").asText(), m.path("terminal").asText());
                JsonNode d = objectMapper.readTree(generaClave);
                String fullPath = this.pathUploadFilesApi + "/" + emisor + "/";
                String nameFacturaXml = d.path("clave").asText() + "-factura";
                String nameOutFacturaXml = this.pathUploadFilesApi + "/" + emisor + "/" + d.path("clave").asText() + "-factura-sign";
                String nameFacturaFirmada = nameFacturaXml + "-sign.xml";
                ComprobantesElectronicos cms = new ComprobantesElectronicos();
                cms.setEmisor(e);
                cms.setConsecutivo(consecutivoFinal);
                cms.setTipoDocumento(tipoDocumento);
                cms.setIdentificacion(emisor);
                cms.setClave(d.path("clave").asText());
                cms.setFechaEmision(fechaEmision);
                cms.setNameXml(nameFacturaXml);
                cms.setNameXmlSign(d.path("clave").asText() + "-factura-sign");
                cms.setAmbiente(e.getAmbiente());
                cms.setEmailDistribucion(e.getEmail());
                cms.setSucursal(m.path("sucursal").asInt());
                cms.setTerminal(m.path("terminal").asInt());
                this._comprobantesElectronicosService.save(cms);
                Long eliminarConsecutivo = cms.getId();
                c.setClave(d.path("clave").asText());
                c.setCodigoActividad(e.getCodigoActividad());
                c.setConsecutivo(d.path("consecutivo").asText());
                this.log.info("_________________________ consecutivo generando _____ " + d.path("consecutivo").asText());
                c.setFechaEmision(fechaEmision);
                c.setEmisorNombre(e.getNombreRazonSocial());
                c.setEmisorTipoIdentif("0" + e.getTipoDeIdentificacion().getId().toString());
                c.setEmisorNumIdentif(emisor);
                c.setNombreComercial(e.getNombreComercial());
                c.setEmisorProv(e.getProvincia().getId().toString());
                c.setEmisorCanton(this._funcionesService.str_pad(e.getCanton().getNumeroCanton(), 2, "0", "STR_PAD_LEFT"));
                c.setEmisorDistrito(this._funcionesService.str_pad(e.getDistrito().getNumeroDistrito(), 2, "0", "STR_PAD_LEFT"));
                c.setEmisorBarrio(this._funcionesService.str_pad(e.getBarrio().getNumeroBarrio(), 2, "0", "STR_PAD_LEFT"));
                c.setEmisorOtrasSenas(e.getOtrasSenas());
                c.setEmisorCodPaisTel(e.getCodigoPais());
                c.setEmisorTel(e.getTelefono());
                c.setEmisorCodPaisFax(e.getCodigoPais());
                c.setEmisorFax(e.getFax());
                c.setEmisorEmail(e.getEmail());
                JsonNode detalleLineaNode;
                String claveDocumentoReferencia = "";
                if (m.path("informacionReferencia").path("numero").asText() != null && !m.path(
                    "informacionReferencia").path("numero").asText().isEmpty()) {
                  claveDocumentoReferencia = m.path("informacionReferencia").path("numero").asText();
                } else if (m.path("numero").asText() != null && !m.path("numero").asText()
                    .isEmpty()) {
                  claveDocumentoReferencia = m.path("numero").asText();
                } 
                Factura f = this._facturaService.findFacturaByClave(claveDocumentoReferencia);
                int i = 1;
                StringBuilder tmp = new StringBuilder();
                String ftmp = "";
                StringBuilder impuestosTmp = new StringBuilder();
                if (f != null) {
                  fechaEmisionFactura = f.getFechaEmision();
                  for (ItemFactura ifa : f.getItems()) {
                    tmp.append("\"").append(i++).append("\":");
                    tmp.append("{");
                    tmp.append("\"numeroLinea\":\"").append(ifa.getNumeroLinea()).append("\",");
                    if (ifa.getPartidaArancelaria() != null && !ifa.getPartidaArancelaria()
                        .isEmpty())
                      tmp.append("\"partidaArancelaria\":\"")
                          .append(procesarTexto(ifa.getPartidaArancelaria())).append("\",");
                    tmp.append("\"codigo\":\"").append(ifa.getCodigo()).append("\",");
                    tmp.append("\"codigoComercial\":{");
                    if (ifa.getCodigoComercialTipo() != null && !ifa.getCodigoComercialTipo()
                        .isEmpty()) {
                      tmp.append("\"0\":{");
                      tmp.append("\"tipo\":\"").append(ifa.getCodigoComercialTipo()).append("\",");
                      tmp.append("\"codigo\":\"")
                          .append(procesarTexto(ifa.getCodigoComercialCodigo())).append("\"");
                      tmp.append("}");
                    } 
                    if (ifa.getCodigoComercialTipo2() != null && !ifa.getCodigoComercialTipo2()
                        .isEmpty()) {
                      tmp.append(",\"1\":{");
                      tmp.append("\"tipo\":\"").append(ifa.getCodigoComercialTipo2()).append("\",");
                      tmp.append("\"codigo\":\"")
                          .append(procesarTexto(ifa.getCodigoComercialCodigo2())).append("\"");
                      tmp.append("}");
                    } 
                    if (ifa.getCodigoComercialTipo3() != null && !ifa.getCodigoComercialTipo3()
                        .isEmpty()) {
                      tmp.append(",\"2\":{");
                      tmp.append("\"tipo\":\"").append(ifa.getCodigoComercialTipo3()).append("\",");
                      tmp.append("\"codigo\":\"")
                          .append(procesarTexto(ifa.getCodigoComercialCodigo3())).append("\"");
                      tmp.append("}");
                    } 
                    if (ifa.getCodigoComercialTipo4() != null && !ifa.getCodigoComercialTipo4()
                        .isEmpty()) {
                      tmp.append(",\"3\":{");
                      tmp.append("\"tipo\":\"").append(ifa.getCodigoComercialTipo4()).append("\",");
                      tmp.append("\"codigo\":\"")
                          .append(procesarTexto(ifa.getCodigoComercialCodigo4())).append("\"");
                      tmp.append("}");
                    } 
                    if (ifa.getCodigoComercialTipo5() != null && !ifa.getCodigoComercialTipo5()
                        .isEmpty()) {
                      tmp.append(",\"4\":{");
                      tmp.append("\"tipo\":\"").append(ifa.getCodigoComercialTipo5()).append("\",");
                      tmp.append("\"codigo\":\"")
                          .append(procesarTexto(ifa.getCodigoComercialCodigo5())).append("\"");
                      tmp.append("}");
                    } 
                    tmp.append("},");
                    tmp.append("\"cantidad\":\"").append(ifa.getCantidad()).append("\",");
                    tmp.append("\"unidadMedida\":\"").append(ifa.getUnidadMedida()).append("\",");
                    tmp.append("\"unidadMedidaComercial\":\"")
                        .append(procesarTexto(ifa.getUnidadMedidaComercial())).append("\",");
                    tmp.append("\"detalle\":\"").append(procesarTexto(ifa.getDetalle()))
                        .append("\",");
                    tmp.append("\"precioUnitario\":\"").append(ifa.getPrecioUnitario())
                        .append("\",");
                    tmp.append("\"montoTotal\":\"").append(ifa.getMontoTotal()).append("\",");
                    tmp.append("\"descuentos\":{  ");
                    if (ifa.getMontoDescuento() != null && !ifa.getMontoDescuento().toString()
                        .isEmpty()) {
                      tmp.append("\"0\":{");
                      tmp.append("\"montoDescuento\":\"").append(ifa.getMontoDescuento())
                          .append("\",");
                      tmp.append("\"naturalezaDescuento\":\"")
                          .append(procesarTexto(ifa.getNaturalezaDescuento())).append("\"");
                      tmp.append("}");
                    } 
                    if (ifa.getMontoDescuento2() != null && !ifa.getMontoDescuento2().toString()
                        .isEmpty()) {
                      tmp.append(",\"1\":{");
                      tmp.append("\"montoDescuento\":\"").append(ifa.getMontoDescuento2())
                          .append("\",");
                      tmp.append("\"naturalezaDescuento\":\"")
                          .append(procesarTexto(ifa.getNaturalezaDescuento2())).append("\"");
                      tmp.append("}");
                    } 
                    if (ifa.getMontoDescuento3() != null && !ifa.getMontoDescuento3().toString()
                        .isEmpty()) {
                      tmp.append(",\"2\":{");
                      tmp.append("\"montoDescuento\":\"").append(ifa.getMontoDescuento3())
                          .append("\",");
                      tmp.append("\"naturalezaDescuento\":\"")
                          .append(procesarTexto(ifa.getNaturalezaDescuento3())).append("\"");
                      tmp.append("}");
                    } 
                    if (ifa.getMontoDescuento4() != null && !ifa.getMontoDescuento4().toString()
                        .isEmpty()) {
                      tmp.append(",\"3\":{");
                      tmp.append("\"montoDescuento\":\"").append(ifa.getMontoDescuento4())
                          .append("\",");
                      tmp.append("\"naturalezaDescuento\":\"")
                          .append(procesarTexto(ifa.getNaturalezaDescuento4())).append("\"");
                      tmp.append("}");
                    } 
                    if (ifa.getMontoDescuento5() != null && !ifa.getMontoDescuento5().toString()
                        .isEmpty()) {
                      tmp.append(",\"4\":{");
                      tmp.append("\"montoDescuento\":\"").append(ifa.getMontoDescuento5())
                          .append("\",");
                      tmp.append("\"naturalezaDescuento\":\"")
                          .append(procesarTexto(ifa.getNaturalezaDescuento5())).append("\"");
                      tmp.append("}");
                    } 
                    tmp.append("},");
                    tmp.append("\"subTotal\":\"").append(ifa.getSubTotal()).append("\",");
                    int q = 0;
                    String coma;
                    int totalLineasImpuesto = ifa.getImpuestosItemFactura().size();
                    impuestosTmp.append("\"impuestos\":{");
                    for (ImpuestosItemFactura ifi : ifa.getImpuestosItemFactura()) {
                      if (q + 1 == totalLineasImpuesto) {
                        coma = "";
                      } else {
                        coma = ",";
                      } 
                      impuestosTmp.append("\"").append(q++).append("\":{");
                      impuestosTmp.append("\"codigo\":\"").append(ifi.getCodigo()).append("\",");
                      impuestosTmp.append("\"tarifa\":\"").append(ifi.getTarifa()).append("\",");
                      impuestosTmp.append("\"codigoTarifa\":\"").append(ifi.getCodigoTarifa())
                          .append("\",");
                      impuestosTmp.append("\"monto\":\"").append(ifi.getMonto()).append("\"");
                      if (ifi.getImpuestoNeto() != null && !ifi.getImpuestoNeto().toString()
                          .isEmpty())
                        impuestosTmp.append(",\"impuestoNeto\":\"").append(ifi.getImpuestoNeto())
                            .append("\"");
                      for (ExoneracionImpuestoItemFactura ifie : ifi.getExoneracionImpuestoItemFactura()) {
                        impuestosTmp.append(",\"exoneracion\":{");
                        impuestosTmp.append("\"tipoDocumento\":\"").append(ifie.getTipoDocumento())
                            .append("\",");
                        impuestosTmp.append("\"numeroDocumento\":\"")
                            .append(ifie.getNumeroDocumento()).append("\",");
                        impuestosTmp.append("\"nombreInstitucion\":\"")
                            .append(procesarTexto(ifie.getNombreInstitucion())).append("\",");
                        impuestosTmp.append("\"fechaEmision\":\"").append(ifie.getFechaEmision())
                            .append("\",");
                        impuestosTmp.append("\"montoExoneracion\":\"")
                            .append(ifie.getMontoExoneracion()).append("\",");
                        impuestosTmp.append("\"porcentajeExoneracion\":\"")
                            .append(ifie.getPorcentajeExoneracion()).append("\"");
                        impuestosTmp.append("}");
                      } 
                      impuestosTmp.append("}").append(coma);
                    } 
                    impuestosTmp.append("},");
                    tmp.append(impuestosTmp);
                    tmp.append("\"impuestoNeto\":\"").append(ifa.getImpuestoNeto()).append("\",");
                    tmp.append("\"montoTotalLinea\":\"").append(ifa.getMontoTotalLinea())
                        .append("\"");
                    tmp.append("},");
                  } 
                  this.log.info("Sucursal: " + f.getSucursal() + " Terminal: " + f.getTerminal());
                  ftmp = ftmp + "{";
                  ftmp = ftmp + "\"situacion\":\"" + f.getSituacion() + "\",";
                  ftmp = ftmp + "\"sucursal\":\"" + f.getSucursal() + "\",";
                  ftmp = ftmp + "\"terminal\":\"" + f.getTerminal() + "\",";
                  ftmp = ftmp + "\"omitirReceptor\":\"" + procesarTexto(f.getOmitirReceptor()) + "\",";
                  ftmp = ftmp + "\"receptorNombre\":\"" + procesarTexto(f.getReceptorNombre()) + "\",";
                  ftmp = ftmp + "\"receptorTipoIdentif\":\"" + f.getReceptorTipoIdentif() + "\",";
                  ftmp = ftmp + "\"receptorNumIdentif\": \"" + f.getReceptor_num_identif() + "\",";
                  if (!tipoDocumento.equalsIgnoreCase("FEE")) {
                    ftmp = ftmp + "\"receptorProvincia\":\"" + f.getReceptorProvincia() + "\",";
                    ftmp = ftmp + "\"receptorCanton\":\"" + f.getReceptorCanton() + "\",";
                    ftmp = ftmp + "\"receptorDistrito\":\"" + f.getReceptorDistrito() + "\",";
                    ftmp = ftmp + "\"receptorBarrio\":\"" + f.getReceptorBarrio() + "\",";
                    ftmp = ftmp + "\"receptorOtrasSenas\":\"" + f.getReceptorOtrasSenas() + "\",";
                  } 
                  ftmp = ftmp + "\"receptorCodPaisTel\":\"" + f.getReceptorCodPaisTel() + "\",";
                  ftmp = ftmp + "\"receptorTel\":\"" + f.getReceptorTel() + "\",";
                  ftmp = ftmp + "\"receptorCodPaisFax\":\"" + f.getReceptorCodPaisFax() + "\",";
                  ftmp = ftmp + "\"receptorFax\":\"" + f.getReceptorFax() + "\",";
                  ftmp = ftmp + "\"receptorEmail\":\"" + f.getReceptorEmail() + "\",";
                  ftmp = ftmp + "\"condVenta\":\"" + f.getCondVenta() + "\",";
                  ftmp = ftmp + "\"plazoCredito\":\"" + f.getPlazoCredito() + "\",";
                  ftmp = ftmp + "\"medioPago\":\"" + f.getMedioPago() + "\",";
                  ftmp = ftmp + "\"medioPago2\":\"" + f.getMedioPago2() + "\",";
                  ftmp = ftmp + "\"medioPago3\":\"" + f.getMedioPago3() + "\",";
                  ftmp = ftmp + "\"medioPago4\":\"" + f.getMedioPago4() + "\",";
                  ftmp = ftmp + "\"codMoneda\":\"" + f.getCodMoneda() + "\",";
                  ftmp = ftmp + "\"tipoCambio\":\"" + f.getTipoCambio() + "\",";
                  ftmp = ftmp + "\"totalServGravados\":\"" + f.getTotalServGravados() + "\",";
                  ftmp = ftmp + "\"totalServExentos\":\"" + f.getTotalServExentos() + "\",";
                  if (!tipoDocumento.equalsIgnoreCase("FEE"))
                    ftmp = ftmp + "\"totalServExonerado\":\"" + f.getTotalServExonerado() + "\","; 
                  ftmp = ftmp + "\"totalMercGravadas\":\"" + f.getTotalMercGravadas() + "\",";
                  ftmp = ftmp + "\"totalMercExentas\":\"" + f.getTotalMercExentas() + "\",";
                  if (!tipoDocumento.equalsIgnoreCase("FEE"))
                    ftmp = ftmp + "\"totalMercExonerada\":\"" + f.getTotalMercExonerada() + "\","; 
                  ftmp = ftmp + "\"totalGravados\":\"" + f.getTotalGravados() + "\",";
                  ftmp = ftmp + "\"totalExentos\":\"" + f.getTotalExentos() + "\",";
                  if (!tipoDocumento.equalsIgnoreCase("FEE"))
                    ftmp = ftmp + "\"totalExonerado\":\"" + f.getTotalExonerado() + "\","; 
                  ftmp = ftmp + "\"totalVentas\":\"" + f.getTotalVentas() + "\",";
                  ftmp = ftmp + "\"totalDescuentos\":\"" + f.getTotalDescuentos() + "\",";
                  ftmp = ftmp + "\"totalVentasNeta\":\"" + f.getTotalVentas() + "\",";
                  ftmp = ftmp + "\"totalImp\":\"" + f.getTotalImp() + "\",";
                  if (!tipoDocumento.equalsIgnoreCase("FEE"))
                    ftmp = ftmp + "\"totalIVADevuelto\":\"" + f.getTotalIVADevuelto() + "\","; 
                  ftmp = ftmp + "\"totalOtrosCargos\":\"" + f.getTotalOtrosCargos() + "\",";
                  ftmp = ftmp + "\"totalComprobante\":\"" + f.getTotalComprobante() + "\",";
                  ftmp = ftmp + "\"otros\":\"" + procesarTexto(f.getOtros()) + "\",";
                  ftmp = ftmp + "\"numeroFactura\":\"" + f.getNumeroFactura() + "\"";
                  this.log.info("obteniendo el número de factura ______________ " + f.getNumeroFactura());
                  ftmp = ftmp + "}";
                  jsonFinal = "{\"detalleLinea\":{" + tmp.substring(0, tmp.length() - 1) + "}}";
                  this.log.info("___________________detalleLinea____________________" + jsonFinal);
                } else {
                  this._comprobantesElectronicosService.deleteById(eliminarConsecutivo);
                  this.log.info("No se esta generando nada, la clave no éxiste");
                  tmp = new StringBuilder();
                  ftmp = "";
                  response.put("response", 400);
                  response.put("msj", "La clave " + claveDocumentoReferencia + " no éxiste en nuestro sistema.");
                  return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                } 
                m = objectMapper.readTree(jsonFinal);
                c.setDetalleFactura(m.path("detalleLinea").toString());
                detalleLineaNode = objectMapper.readTree(m.path("detalleLinea").toString());
                JsonNode referenciasNode;
                String jm = "";
                jm = jm + "{";
                jm = jm + "\"0\":{";
                jm = jm + "\"numero\":\"" + clave + "\",";
                jm = jm + "\"fechaEmision\":\"" + fechaEmisionFactura + "\",";
                jm = jm + "\"codigo\":\"" + codigo + "\",";
                jm = jm + "\"razon\":\"" + razon + "\"";
                jm = jm + "}";
                jm = jm + "}";
                c.setReferencia(jm);
                referenciasNode = objectMapper.readTree(jm);
                m = objectMapper.readTree(ftmp);
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
                c.setCodMoneda(m.path("codMoneda").asText());
                c.setTipoCambio(m.path("tipoCambio").asText());
                c.setTotalServGravados(m.path("totalServGravados").asText());
                c.setTotalServExentos(m.path("totalServExentos").asText());
                if (!tipoDocumento.equalsIgnoreCase("FEE"))
                  c.setTotalServExonerado(m.path("totalServExonerado").asText()); 
                c.setTotalMercGravadas(m.path("totalMercGravadas").asText());
                c.setTotalMercExentas(m.path("totalMercExentas").asText());
                if (!tipoDocumento.equalsIgnoreCase("FEE"))
                  c.setTotalMercExonerada(m.path("totalMercExonerada").asText()); 
                c.setTotalGravados(m.path("totalGravados").asText());
                c.setTotalExentos(m.path("totalExentos").asText());
                if (!tipoDocumento.equalsIgnoreCase("FEE"))
                  c.setTotalExonerado(m.path("totalExonerado").asText()); 
                c.setTotalVentas(m.path("totalVentas").asText());
                c.setTotalDescuentos(m.path("totalDescuentos").asText());
                c.setTotalVentasNeta(m.path("totalVentasNeta").asText());
                c.setTotalImp(m.path("totalImp").asText());
                if (!tipoDocumento.equalsIgnoreCase("FEE"))
                  c.setTotalIVADevuelto(m.path("totalIVADevuelto").asText()); 
                c.setTotalOtrosCargos(m.path("totalOtrosCargos").asText());
                c.setTotalComprobante(m.path("totalComprobante").asText());
                c.setNumeroFactura(m.path("numeroFactura").asText());
                c.setOtros(m.path("otros").asText());
                this._generaXml.generateXml(fullPath, this._generaXml.GeneraXml(c), nameFacturaXml);
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
                          item.setMontoDescuento4(k.path("montoDescuento").asDouble());
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
                        if (imp.path("exoneracion").path("tipoDocumento").asText() != null && !imp
                            .path("exoneracion").path("tipoDocumento").asText().isEmpty()) {
                          eiif.setTipoDocumento(imp.path("exoneracion").path("tipoDocumento").asText());
                        } else {
                          response.put("response", 401);
                          response.put("msj", "Tipo de documento de exoneración es requerido.");
                          return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                        } 
                        if (imp.path("exoneracion").path("numeroDocumento").asText() != null && !imp
                            .path("exoneracion").path("numeroDocumento").asText().isEmpty()) {
                          eiif.setNumeroDocumento(imp.path("exoneracion").path("numeroDocumento").asText());
                        } else {
                          response.put("response", 401);
                          response.put("msj", "Número de exoneración es requerido.");
                          return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                        } 
                        if (imp.path("exoneracion").path("nombreInstitucion").asText() != null && !imp
                            .path("exoneracion").path("nombreInstitucion").asText().isEmpty()) {
                          eiif.setNombreInstitucion(imp
                              .path("exoneracion").path("nombreInstitucion").asText());
                        } else {
                          response.put("response", 401);
                          response.put("msj", "Nombre de institución exonerada es requerido.");
                          return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                        } 
                        if (imp.path("exoneracion").path("fechaEmision").asText() != null && !imp
                            .path("exoneracion").path("fechaEmision").asText().isEmpty()) {
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
                          response.put("msj", "Porcentaje de compra de exoneración es requerido.");
                          return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                        } 
                        iif.addItemFacturaImpuestosExoneracion(eiif);
                      } 
                      item.addItemFacturaImpuestos(iif);
                    } 
                  } 
                  if (k.path("montoTotalLinea") != null && k.path("montoTotalLinea").asDouble() > 0.0D)
                    item.setMontoTotalLinea(k.path("montoTotalLinea").asDouble());
                  item.setImpuestoNeto(k.path("impuestoNeto").asDouble());
                  factura.addItemFactura(item);
                } 
                factura.setTipoDocumento(tipoDocumento);
                factura.setClave(c.getClave());
                factura.setConsecutivo(c.getConsecutivo());
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
                factura.setIdentificacion(c.getEmisorNumIdentif());
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
                factura.setNumeroFactura(c.getNumeroFactura());
                this._facturaService.save(factura);
                response.put("response", 200);
                response.put("clave", c.getClave());
                response.put("consecutivo", c.getConsecutivo());
                response.put("fechaEmision", c.getFechaEmision());
                response.put("fileXmlSign", nameFacturaFirmada);
                return new ResponseEntity<>(response, HttpStatus.OK);
              } 
              response.put("response", 401);
              response.put("msj", "Solo se aceptan Notas de Débito y Notas de Crédito aceptadas por Hacienda.");
              return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            } 
            response.put("response", 401);
            response.put("msj", "El usuario o token no éxiste.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
          } catch (Exception e) {
            e.printStackTrace();
            response.put("response", 404);
            response.put("msj", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
          }  
        response.put("response", 401);
        response.put("msj", "La terminal es requerida.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
      } 
      response.put("response", 401);
      response.put("msj", "La sucursal es requerida.");
      return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    } 
    response.put("response", 401);
    response.put("msj", "La situación es requerida.");
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  public String procesarTexto(String j) {
    String r;
    r = StringEscapeUtils.escapeJava(j);
    return r;
  }
}

