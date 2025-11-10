package app.simplestudio.com.controllers;

import app.simplestudio.com.mh.helpers.*;
import app.simplestudio.com.mh.helpers.CampoValidationHelper.CampoRequeridoException;
import app.simplestudio.com.mh.helpers.ClaveConsecutivoHelper.ClaveConsecutivoResult;
import app.simplestudio.com.mh.helpers.EmisorValidationHelper.ValidacionResult;
import app.simplestudio.com.mh.helpers.FacturaJsonBuilder.FacturaJsonResult;
import app.simplestudio.com.mh.helpers.XmlPathsHelper.XmlPaths;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.mh.IGeneraXml;
import app.simplestudio.com.mh.ISigner;
import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IFacturaService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api-4.4"})
@Slf4j
public class NotaDebitoNotaCreditoAceptadasController {

  @Autowired
  private IGeneraXml _generaXml;

  @Autowired
  private FuncionesService _funcionesService;

  @Autowired
  private ISigner _signer;

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
  private IFacturaService _facturaService;

  @Autowired
  private EmisorValidationHelper emisorValidationHelper;

  @Autowired
  private ConsecutivoHelper consecutivoHelper;

  @Autowired
  private ClaveConsecutivoHelper claveConsecutivoHelper;

  @Autowired
  private XmlPathsHelper xmlPathsHelper;

  @Autowired
  private CampoValidationHelper campoValidationHelper;

  @Autowired
  private ItemFacturaHelper itemFacturaHelper;

  @Autowired
  private ReceptorHelper receptorHelper;

  @Autowired
  private EmisorDataHelper emisorDataHelper;

  @Autowired
  private FacturaJsonBuilder facturaJsonBuilder;

  @Autowired
  private FacturaReferenciaHelper facturaReferenciaHelper;

  @Autowired
  private FacturaConfigurationHelper facturaConfigurationHelper;

  @CrossOrigin
  @RequestMapping(value = {"/recepcion-nd-nc"}, method = {RequestMethod.POST},
      consumes = {"application/json; charset=utf-8"}, produces = {"application/json; charset=utf-8"})
  public ResponseEntity<?> getFactura(@RequestBody String j) throws Exception {
    Map<String, Object> response = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode m = objectMapper.readTree(j);

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String fechaEmision = format.format(new Date()) + "-06:00";

    String tipoDocumento = m.path("tipoDocumento").asText();
    String situacion = m.path("situacion").asText();
    String sucursal = m.path("sucursal").asText();
    String terminal = m.path("terminal").asText();
    String codigo = m.path("codigo").asText();
    String razon = m.path("razon").asText();
    String emisor = m.path("emisor").asText();
    String claveReferencia = m.path("numero").asText();

    // Validar campos requeridos
    try {
      campoValidationHelper.validarCampoRequerido(m, "situacion", "La situación es requerida.");
      campoValidationHelper.validarCampoRequerido(m, "sucursal", "La sucursal es requerida.");
      campoValidationHelper.validarCampoRequerido(m, "terminal", "La terminal es requerida.");
    } catch (CampoRequeridoException e) {
      return campoValidationHelper.respuestaCampoRequerido(e.getMessage());
    }

    // Validar tipo de documento
    if (!tipoDocumento.equalsIgnoreCase("ND") && !tipoDocumento.equalsIgnoreCase("NC")) {
      response.put("response", 401);
      response.put("msj", "Solo se aceptan Notas de Débito y Notas de Crédito aceptadas por Hacienda.");
      return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    Long eliminarConsecutivo = 0L;

    try {
      // Validar emisor
      ValidacionResult validacion = emisorValidationHelper.validarEmisor(m);
      if (!validacion.esValido()) {
        return emisorValidationHelper.respuestaAccesoDenegado();
      }

      Emisor e = validacion.emisor;
      log.info("El emisor existe es valido");

      // Configurar ambiente
      AmbienteConfigHelper config = new AmbienteConfigHelper(e, endpointProd, endpointStag,
          tokenProd, tokenStag, pathUploadFilesApi);

      // Validar terminal
      CTerminal csu = emisorValidationHelper.validarTerminal(
          e.getId(),
          m.path("sucursal").asInt(),
          m.path("terminal").asInt()
      );
      if (csu == null) {
        return emisorValidationHelper.respuestaTerminalNoExiste();
      }

      // Obtener consecutivo
      Long consecutivoEm = consecutivoHelper.obtenerConsecutivoPorTipo(csu, tipoDocumento);

      // Obtener clave de documento de referencia
      String claveDocumentoReferencia = facturaReferenciaHelper.obtenerClaveReferencia(m);

      // Buscar factura de referencia
      Factura facturaReferencia = _facturaService.findFacturaByClave(claveDocumentoReferencia);
      if (facturaReferencia == null) {
        log.info("No se esta generando nada, la clave no existe");
        response.put("response", 400);
        response.put("msj", "La clave " + claveDocumentoReferencia + " no existe en nuestro sistema.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
      }

      // Generar clave y consecutivo
      ClaveConsecutivoResult resultado = claveConsecutivoHelper.generarClaveYConsecutivo(
          e, tipoDocumento, m, consecutivoEm
      );

      XmlPaths paths = xmlPathsHelper.generarPathsFactura(emisor, resultado.clave);

      // Crear comprobante electrónico
      ComprobantesElectronicos cms = new ComprobantesElectronicos();
      cms.setEmisor(e);
      cms.setConsecutivo(resultado.consecutivoFinal);
      cms.setTipoDocumento(tipoDocumento);
      cms.setIdentificacion(emisor);
      cms.setClave(resultado.clave);
      cms.setFechaEmision(fechaEmision);
      cms.setNameXml(paths.nameFacturaXml);
      cms.setNameXmlSign(resultado.clave + "-factura-sign");
      cms.setAmbiente(e.getAmbiente());
      cms.setEmailDistribucion(e.getEmail());
      cms.setSucursal(m.path("sucursal").asInt());
      cms.setTerminal(m.path("terminal").asInt());
      _comprobantesElectronicosService.save(cms);
      eliminarConsecutivo = cms.getId();

      // Configurar CCampoFactura
      CCampoFactura c = new CCampoFactura();
      c.setClave(resultado.clave);
      c.setCodigoActividad(e.getCodigoActividad());
      c.setConsecutivo(resultado.claveJson.path("consecutivo").asText());
      log.info("_________________________ consecutivo generando _____ " + c.getConsecutivo());
      c.setFechaEmision(fechaEmision);

      // Configurar datos del emisor
      emisorDataHelper.configurarDatosEmisor(c, e, _funcionesService);

      // Construir JSON de la factura de referencia
      FacturaJsonResult facturaJson = facturaJsonBuilder.construirJsonDesdeFactura(
          facturaReferencia, tipoDocumento
      );

      // Configurar detalle de línea
      JsonNode detalleLineaNode = objectMapper.readTree(facturaJson.detalleLinea);
      c.setDetalleFactura(facturaJson.detalleLinea);

      // Crear referencia
      String referenciaJson = facturaReferenciaHelper.crearReferenciaJson(
          claveReferencia,
          facturaReferencia.getFechaEmision(),
          codigo,
          razon
      );
      c.setReferencia(referenciaJson);
      JsonNode referenciasNode = objectMapper.readTree(referenciaJson);

      // Procesar datos de la factura desde el JSON construido
      JsonNode facturaNode = objectMapper.readTree(facturaJson.facturaCompleta);

      // Configurar receptor
      receptorHelper.configurarDatosReceptor(c, facturaNode);

      // Configurar condiciones de venta y medios de pago
      c.setCondVenta(facturaNode.path("condVenta").asText());
      c.setPlazoCredito(facturaNode.path("plazoCredito").asText());
      c.setMedioPago(facturaNode.path("medioPago").asText());
      c.setMedioPago2(facturaNode.path("medioPago2").asText());
      c.setMedioPago3(facturaNode.path("medioPago3").asText());
      c.setMedioPago4(facturaNode.path("medioPago4").asText());

      // Configurar moneda
      c.setCodMoneda(facturaNode.path("codMoneda").asText());
      c.setTipoCambio(facturaNode.path("tipoCambio").asText());

      // Configurar totales
      configurarTotales(c, facturaNode, tipoDocumento);

      c.setNumeroFactura(facturaNode.path("numeroFactura").asText());
      c.setOtros(facturaNode.path("otros").asText());

      // Generar y firmar XML
      _generaXml.generateXml(paths.fullPath, _generaXml.GeneraXml(c), paths.nameFacturaXml);
      _signer.sign(config.certificado, config.keyCertificado,
          paths.fullPath + paths.nameFacturaXml + ".xml", paths.nameOutFacturaXml + ".xml");

      // Crear nueva factura
      Factura factura = new Factura();

      // Procesar items usando el helper
      itemFacturaHelper.procesarItemsFactura(factura, detalleLineaNode, response);

      // Configurar datos básicos de la factura
      facturaConfigurationHelper.configurarDatosBasicos(
          factura, c, tipoDocumento, situacion, sucursal, terminal, emisor, _funcionesService
      );

      if (m.has("mediosPago") && m.get("mediosPago").isArray()) {
        int idx = 0;
        for (JsonNode mp : m.get("mediosPago")) {
          String tipo = mp.path("tipoMedioPago").asText(null);
          String totStr = mp.path("totalMedioPago").asText(null);
          String otros = mp.path("medioPagoOtros").asText(null);
          java.math.BigDecimal total = null;
          try { if (totStr != null && !totStr.isBlank()) total = new java.math.BigDecimal(totStr); } catch (Exception ignore) {}

          switch (idx) {
            case 0:
              if (tipo != null) c.setMedioPago(tipo);
              factura.setMedioPagoTotal1(total);
              factura.setMedioPagoOtros1(otros);
              break;
            case 1:
              if (tipo != null) c.setMedioPago2(tipo);
              factura.setMedioPagoTotal2(total);
              factura.setMedioPagoOtros2(otros);
              break;
            case 2:
              if (tipo != null) c.setMedioPago3(tipo);
              factura.setMedioPagoTotal3(total);
              factura.setMedioPagoOtros3(otros);
              break;
            case 3:
              if (tipo != null) c.setMedioPago4(tipo);
              factura.setMedioPagoTotal4(total);
              factura.setMedioPagoOtros4(otros);
              break;
          }
          idx++;
          if (idx >= 4) break;
        }
      }

      // Procesar referencias
      facturaReferenciaHelper.procesarReferenciasEnFactura(factura, referenciasNode);

      // Configurar totales
      facturaConfigurationHelper.configurarTotales(factura, c);

      _facturaService.save(factura);

      // Respuesta exitosa
      response.put("response", 200);
      response.put("clave", c.getClave());
      response.put("consecutivo", c.getConsecutivo());
      response.put("fechaEmision", c.getFechaEmision());
      response.put("fileXmlSign", paths.nameFacturaFirmada);
      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (Exception e) {
      e.printStackTrace();
      if (eliminarConsecutivo > 0) {
        _comprobantesElectronicosService.deleteById(eliminarConsecutivo);
      }
      response.put("response", 404);
      response.put("msj", e.getMessage());
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private void configurarTotales(CCampoFactura c, JsonNode facturaNode, String tipoDocumento) {
    c.setTotalServGravados(facturaNode.path("totalServGravados").asText());
    c.setTotalServExentos(facturaNode.path("totalServExentos").asText());
    if (!tipoDocumento.equalsIgnoreCase("FEE")) {
      c.setTotalServExonerado(facturaNode.path("totalServExonerado").asText());
    }
    c.setTotalMercGravadas(facturaNode.path("totalMercGravadas").asText());
    c.setTotalMercExentas(facturaNode.path("totalMercExentas").asText());
    if (!tipoDocumento.equalsIgnoreCase("FEE")) {
      c.setTotalMercExonerada(facturaNode.path("totalMercExonerada").asText());
    }
    c.setTotalGravados(facturaNode.path("totalGravados").asText());
    c.setTotalExentos(facturaNode.path("totalExentos").asText());
    if (!tipoDocumento.equalsIgnoreCase("FEE")) {
      c.setTotalExonerado(facturaNode.path("totalExonerado").asText());
    }
    c.setTotalVentas(facturaNode.path("totalVentas").asText());
    c.setTotalDescuentos(facturaNode.path("totalDescuentos").asText());
    c.setTotalVentasNeta(facturaNode.path("totalVentasNeta").asText());
    c.setTotalImp(facturaNode.path("totalImp").asText());
    if (!tipoDocumento.equalsIgnoreCase("FEE")) {
      c.setTotalIVADevuelto(facturaNode.path("totalIVADevuelto").asText());
    }
    c.setTotalOtrosCargos(facturaNode.path("totalOtrosCargos").asText());
    c.setTotalComprobante(facturaNode.path("totalComprobante").asText());
  }
}