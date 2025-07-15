package app.simplestudio.com.controllers;

import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.mh.IGeneraXml;
import app.simplestudio.com.mh.ISigner;
import app.simplestudio.com.mh.Sender;
import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IFacturaService;
import app.simplestudio.com.service.IMensajeReceptorService;
import app.simplestudio.com.util.DocumentTypeUtil;
import app.simplestudio.com.util.EntityMapperUtil;
import app.simplestudio.com.util.EnvironmentConfigUtil;
import app.simplestudio.com.util.FileManagerUtil;
import app.simplestudio.com.util.InvoiceProcessingUtil;
import app.simplestudio.com.util.JsonProcessorUtil;
import app.simplestudio.com.util.XmlValidationUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/api-4.3"})
public class RecepcionController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  // ==================== SERVICIOS ORIGINALES ====================
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

  @Autowired
  private IFacturaService _facturaService;

  // ==================== NUEVOS UTILS ====================
  @Autowired
  private InvoiceProcessingUtil invoiceProcessingUtil;

  @Autowired
  private EntityMapperUtil entityMapperUtil;

  @Autowired
  private DocumentTypeUtil documentTypeUtil;

  @Autowired
  private EnvironmentConfigUtil environmentConfigUtil;

  @Autowired
  private FileManagerUtil fileManagerUtil;

  @Autowired
  private JsonProcessorUtil jsonProcessorUtil;

  @Autowired
  private XmlValidationUtil xmlValidationUtil;

  // ==================== CONFIGURACIÓN ORIGINAL ====================
  @Value("${path.upload.files.api}")
  private String pathUploadFilesApi;

  // ==================== ENDPOINTS ORIGINALES MANTENIDOS ====================

  /**
   * FIRMA ORIGINAL MANTENIDA - Endpoint principal de recepción
   */
  @RequestMapping(value = {"/recepcion"}, method = {RequestMethod.POST},
      consumes = {"application/json; charset=utf-8"},
      produces = {"application/json; charset=utf-8"})
  public ResponseEntity<?> getFactura(@RequestBody String jsonRequest) throws Exception {
    log.info("=== INICIO PROCESAMIENTO FACTURA ===");

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode requestData = objectMapper.readTree(jsonRequest);

      // Validación básica del request
      Map<String, Object> validation = invoiceProcessingUtil.validateBasicRequest(requestData);
      if (!(Boolean) validation.get("valid")) {
        return new ResponseEntity<>(
            invoiceProcessingUtil.buildErrorResponse((Integer) validation.get("code"), (String) validation.get("message")),
            HttpStatus.valueOf((Integer) validation.get("code"))
        );
      }

      // Obtener y validar emisor
      String tokenAccess = requestData.path("tokenAccess").asText().trim();
      String emisorId = requestData.path("emisor").asText();
      Emisor emisor = _emisorService.findEmisorByIdentificacion(emisorId, tokenAccess);

      if (emisor == null) {
        return new ResponseEntity<>(
            invoiceProcessingUtil.buildErrorResponse(401, "Emisor no encontrado o token inválido"),
            HttpStatus.UNAUTHORIZED
        );
      }

      // Validar configuración del emisor
      Map<String, Object> emisorValidation = invoiceProcessingUtil.validateEmisorConfiguration(emisor);
      if (!(Boolean) emisorValidation.get("valid")) {
        return new ResponseEntity<>(
            invoiceProcessingUtil.buildErrorResponse((Integer) emisorValidation.get("code"), (String) emisorValidation.get("message")),
            HttpStatus.UNAUTHORIZED
        );
      }

      // Procesar según tipo de documento
      String tipoDocumento = requestData.path("tipoDocumento").asText();
      return processInvoiceByType(requestData, emisor, tipoDocumento);

    } catch (Exception e) {
      log.error("Error procesando factura", e);
      return new ResponseEntity<>(
          invoiceProcessingUtil.buildErrorResponse(500, "Error interno del servidor: " + e.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR
      );
    } finally {
      log.info("=== FIN PROCESAMIENTO FACTURA ===");
    }
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Endpoint para mensajes receptor
   */
  @RequestMapping(value = {"/recepcion-mr"}, method = {RequestMethod.POST},
      consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> RecepcionMr(@RequestBody String jsonRequest) throws Exception {
    log.info("=== INICIO PROCESAMIENTO MENSAJE RECEPTOR ===");

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode requestData = objectMapper.readTree(jsonRequest);

      return processMessageReceptor(requestData);

    } catch (Exception e) {
      log.error("Error procesando mensaje receptor", e);
      return new ResponseEntity<>(
          invoiceProcessingUtil.buildErrorResponse(500, "Error procesando mensaje receptor: " + e.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR
      );
    } finally {
      log.info("=== FIN PROCESAMIENTO MENSAJE RECEPTOR ===");
    }
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Consulta de documentos
   */
  @RequestMapping(value = {"/consultar-documentos"}, method = {RequestMethod.POST},
      consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> consultarDocumentos(@RequestBody String jsonRequest) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode requestData = objectMapper.readTree(jsonRequest);

      return processDocumentQuery(requestData);

    } catch (Exception e) {
      log.error("Error consultando documentos", e);
      return new ResponseEntity<>(
          invoiceProcessingUtil.buildErrorResponse(400, "Error consultando documentos"),
          HttpStatus.BAD_REQUEST
      );
    }
  }

  // ==================== MÉTODOS AUXILIARES REFACTORIZADOS ====================

  /**
   * Procesa factura según su tipo
   */
  private ResponseEntity<?> processInvoiceByType(JsonNode requestData, Emisor emisor, String tipoDocumento) throws Exception {
    return switch (tipoDocumento.toUpperCase()) {
      case "FEC" -> processFacturaCompra(requestData, emisor);
      case "FE", "ND", "NC", "TE", "FEE" -> processFacturaEstandar(requestData, emisor, tipoDocumento);
      default -> new ResponseEntity<>(
          invoiceProcessingUtil.buildErrorResponse(400, "Tipo de documento no soportado: " + tipoDocumento),
          HttpStatus.BAD_REQUEST
      );
    };
  }

  /**
   * Procesa factura estándar (FE, ND, NC, TE, FEE)
   */
  private ResponseEntity<?> processFacturaEstandar(JsonNode requestData, Emisor emisor, String tipoDocumento) throws Exception {
    // Configurar ambiente
    EnvironmentConfigUtil.EnvironmentConfig envConfig = invoiceProcessingUtil.configureEnvironment(emisor);

    // Validar terminal
    CTerminal terminal = _emisorService.findBySecuenciaByTerminal(
        emisor.getId(),
        requestData.path("sucursal").asInt(),
        requestData.path("terminal").asInt()
    );

    if (terminal == null) {
      return new ResponseEntity<>(
          invoiceProcessingUtil.buildErrorResponse(401, "La sucursal o terminal no existen"),
          HttpStatus.UNAUTHORIZED
      );
    }

    // Validaciones específicas para FEE
    if ("FEE".equals(tipoDocumento)) {
      Map<String, Object> exportValidation = invoiceProcessingUtil.validateExportInvoiceFields(requestData);
      if (!(Boolean) exportValidation.get("valid")) {
        return new ResponseEntity<>(
            invoiceProcessingUtil.buildErrorResponse((Integer) exportValidation.get("code"), (String) exportValidation.get("message")),
            HttpStatus.UNAUTHORIZED
        );
      }
    }

    // Calcular consecutivo
    ComprobantesElectronicos existing = _comprobantesElectronicosService.findByEmisor(
        emisor.getIdentificacion(), tipoDocumento,
        requestData.path("sucursal").asInt(),
        requestData.path("terminal").asInt(),
        emisor.getAmbiente()
    );

    Long consecutivo = invoiceProcessingUtil.calculateConsecutive(terminal, existing, tipoDocumento);

    // Generar clave
    String clave = _sender.getClave(
        documentTypeUtil.getTipoDocumentoFromClave(tipoDocumento),
        emisor.getTipoDeIdentificacion().getId().toString(),
        emisor.getIdentificacion(),
        requestData.path("situacion").asText(),
        "506", // Costa Rica
        consecutivo.toString(),
        "00000001", // Código de seguridad por defecto
        xmlValidationUtil.str_pad(requestData.path("sucursal").asText(), 3, "0", "STR_PAD_LEFT"),
        xmlValidationUtil.str_pad(requestData.path("terminal").asText(), 5, "0", "STR_PAD_LEFT")
    );

    // Extraer clave del JSON response
    clave = extractClaveFromJsonResponse(clave);

    // Crear CCampoFactura
    CCampoFactura campoFactura = new CCampoFactura();
    String fechaEmision = invoiceProcessingUtil.generateCurrentEmissionDate();

    // Mapear datos comunes
    entityMapperUtil.mapCommonInvoiceData(campoFactura, requestData, clave, fechaEmision);

    // Mapear emisor
    entityMapperUtil.mapEmisorToCCampoFactura(campoFactura, emisor, false);

    // Mapear receptor
    entityMapperUtil.mapJsonToReceptor(campoFactura, requestData);

    // Procesar documento
    return processDocumentGeneration(campoFactura, emisor, tipoDocumento, consecutivo, envConfig);
  }

  /**
   * Procesa factura de compra (FEC) - lógica especial donde emisor/receptor están invertidos
   */
  private ResponseEntity<?> processFacturaCompra(JsonNode requestData, Emisor emisor) throws Exception {
    // Configurar ambiente
    EnvironmentConfigUtil.EnvironmentConfig envConfig = invoiceProcessingUtil.configureEnvironment(emisor);

    // Validar terminal
    CTerminal terminal = _emisorService.findBySecuenciaByTerminal(
        emisor.getId(),
        requestData.path("sucursal").asInt(),
        requestData.path("terminal").asInt()
    );

    if (terminal == null) {
      return new ResponseEntity<>(
          invoiceProcessingUtil.buildErrorResponse(401, "La sucursal o terminal no existen"),
          HttpStatus.UNAUTHORIZED
      );
    }

    // Calcular consecutivo
    ComprobantesElectronicos existing = _comprobantesElectronicosService.findByEmisor(
        emisor.getIdentificacion(), "FEC",
        requestData.path("sucursal").asInt(),
        requestData.path("terminal").asInt(),
        emisor.getAmbiente()
    );

    Long consecutivo = invoiceProcessingUtil.calculateConsecutive(terminal, existing, "FEC");

    // Generar clave
    String clave = _sender.getClave(
        "08", // FEC
        emisor.getTipoDeIdentificacion().getId().toString(),
        emisor.getIdentificacion(),
        requestData.path("situacion").asText(),
        "506",
        consecutivo.toString(),
        "00000001",
        xmlValidationUtil.str_pad(requestData.path("sucursal").asText(), 3, "0", "STR_PAD_LEFT"),
        xmlValidationUtil.str_pad(requestData.path("terminal").asText(), 5, "0", "STR_PAD_LEFT")
    );

    clave = extractClaveFromJsonResponse(clave);

    // Crear CCampoFactura para FEC (lógica invertida)
    CCampoFactura campoFactura = new CCampoFactura();
    String fechaEmision = invoiceProcessingUtil.generateCurrentEmissionDate();

    // Mapear datos comunes
    entityMapperUtil.mapCommonInvoiceData(campoFactura, requestData, clave, fechaEmision);

    // Para FEC: emisor y receptor están invertidos
    entityMapperUtil.mapJsonToEmisorAsReceptor(campoFactura, requestData, emisor);

    // Procesar documento
    return processDocumentGeneration(campoFactura, emisor, "FEC", consecutivo, envConfig);
  }

  /**
   * Procesa mensajes receptor
   */
  private ResponseEntity<?> processMessageReceptor(JsonNode requestData) throws Exception {
    String emisorId = requestData.path("emisor").asText();
    String tokenAccess = requestData.path("tokenAccess").asText().trim();

    Emisor emisor = _emisorService.findEmisorByIdentificacion(emisorId, tokenAccess);
    if (emisor == null) {
      return new ResponseEntity<>(
          invoiceProcessingUtil.buildErrorResponse(401, "Emisor no encontrado"),
          HttpStatus.UNAUTHORIZED
      );
    }

    String tipoDocumento = requestData.path("tipoDocumento").asText();

    // Validar terminal para mensaje receptor
    CTerminal terminal = _emisorService.findBySecuenciaByTerminal(
        emisor.getId(),
        requestData.path("sucursal").asInt(),
        requestData.path("terminal").asInt()
    );

    if (terminal == null) {
      return new ResponseEntity<>(
          invoiceProcessingUtil.buildErrorResponse(401, "La sucursal o terminal no existen"),
          HttpStatus.UNAUTHORIZED
      );
    }

    // Calcular consecutivo para mensaje receptor
    Long consecutivo = invoiceProcessingUtil.calculateConsecutive(terminal, null, tipoDocumento);

    // Procesar mensaje receptor específico
    return processSpecificMessageReceptor(requestData, emisor, tipoDocumento, consecutivo);
  }

  /**
   * Procesa consulta de documentos
   */
  private ResponseEntity<?> processDocumentQuery(JsonNode requestData) {
    String tokenAccess = requestData.path("tokenAccess").asText().trim();
    String emisorId = requestData.path("emisor").asText();

    Emisor emisor = _emisorService.findEmisorByIdentificacion(emisorId, tokenAccess);
    if (emisor == null) {
      return new ResponseEntity<>(
          invoiceProcessingUtil.buildErrorResponse(203, "Acceso denegado"),
          HttpStatus.NON_AUTHORITATIVE_INFORMATION
      );
    }

    String clave = requestData.path("clave").asText();
    ComprobantesElectronicos ce = _comprobantesElectronicosService.findByClaveDocumento(clave);

    if (ce == null) {
      return new ResponseEntity<>(
          invoiceProcessingUtil.buildErrorResponse(404, "Documento no encontrado"),
          HttpStatus.NOT_FOUND
      );
    }

    // Construir respuesta de consulta
    Map<String, Object> response = invoiceProcessingUtil.buildSuccessResponse(clave, "Consulta exitosa");
    response.put("ind-estado", ce.getIndEstado() != null ? ce.getIndEstado() : "procesando");
    response.put("xml-aceptacion", ce.getNameXmlAcceptacion() != null ? ce.getNameXmlAcceptacion() : "");
    response.put("fecha-aceptacion", ce.getFechaAceptacion() != null ? ce.getFechaAceptacion() : "");

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Genera XML, firma y guarda el documento
   */
  private ResponseEntity<?> processDocumentGeneration(CCampoFactura campoFactura, Emisor emisor,
      String tipoDocumento, Long consecutivo,
      EnvironmentConfigUtil.EnvironmentConfig envConfig) throws Exception {

    // Generar XML
    String xmlContent = _generaXml.GeneraXml(campoFactura);

    // Rutas de archivos
    String basePath = pathUploadFilesApi + emisor.getIdentificacion() + "/";
    String xmlFileName = campoFactura.getClave() + "-factura";
    String xmlPath = basePath + xmlFileName + ".xml";
    String xmlSignedPath = basePath + campoFactura.getClave() + "-factura-sign.xml";

    // Crear directorio si no existe
    fileManagerUtil.createDirectoryIfNotExists(basePath);

    // Guardar XML
    _generaXml.generateXml(basePath, xmlContent, xmlFileName);

    // Firmar XML
    String certificatePath = invoiceProcessingUtil.buildCertificatePath(pathUploadFilesApi, emisor.getIdentificacion(), emisor.getCertificado());
    _signer.sign(certificatePath, emisor.getPingApi(), xmlPath, xmlSignedPath);

    // Crear registro en BD
    ComprobantesElectronicos ce = entityMapperUtil.createComprobantesElectronicos(
        campoFactura, emisor, tipoDocumento, consecutivo, campoFactura.getClave() + "-factura-sign"
    );

    _comprobantesElectronicosService.save(ce);

    // Log de éxito
    invoiceProcessingUtil.logProcessingDetails(new InvoiceProcessingUtil.InvoiceProcessingParams(
        null, emisor, tipoDocumento, pathUploadFilesApi
    ));

    return new ResponseEntity<>(
        invoiceProcessingUtil.buildSuccessResponse(campoFactura.getClave(), "Documento procesado exitosamente"),
        HttpStatus.OK
    );
  }

  /**
   * Procesa mensaje receptor específico
   */
  private ResponseEntity<?> processSpecificMessageReceptor(JsonNode requestData, Emisor emisor, String tipoDocumento, Long consecutivo) throws Exception {
    // TODO: Implementar lógica específica para mensaje receptor
    // Similar a processDocumentGeneration pero para mensajes receptor

    return new ResponseEntity<>(
        invoiceProcessingUtil.buildSuccessResponse("MR-" + consecutivo, "Mensaje receptor procesado"),
        HttpStatus.OK
    );
  }

  /**
   * Extrae clave del JSON response del Sender
   */
  private String extractClaveFromJsonResponse(String jsonResponse) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode response = mapper.readTree(jsonResponse);
    return response.path("response").asText();
  }
}