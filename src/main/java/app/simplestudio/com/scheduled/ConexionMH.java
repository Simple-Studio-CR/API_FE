package app.simplestudio.com.scheduled;

import app.simplestudio.com.mh.Sender;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.models.entity.MensajeReceptor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IMensajeReceptorService;
import app.simplestudio.com.service.adapter.StorageAdapter;
import app.simplestudio.com.util.DocumentTypeUtil;
import app.simplestudio.com.util.EmailManagerUtil;
import app.simplestudio.com.util.EnvironmentConfigUtil;
import app.simplestudio.com.util.JsonProcessorUtil;
import app.simplestudio.com.util.ReportGeneratorUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConexionMH {

  private final Logger log = LoggerFactory.getLogger(getClass());

  // ==================== SERVICIOS ORIGINALES ====================
  @Autowired
  private IComprobantesElectronicosService _comprobantesElectronicosService;

  @Autowired
  private IEmisorService _emisorService;

  @Autowired
  private Sender _sender;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private IMensajeReceptorService _mensajeReceptorService;

  // ==================== NUEVOS UTILS ====================
  @Autowired
  private DocumentTypeUtil documentTypeUtil;

  @Autowired
  private EnvironmentConfigUtil environmentConfigUtil;

  @Autowired
  private EmailManagerUtil emailManagerUtil;

  @Autowired
  private ReportGeneratorUtil reportGeneratorUtil;

  @Autowired
  private StorageAdapter  _storageAdapter;

  @Autowired
  private JsonProcessorUtil jsonProcessorUtil;

  // ==================== CONFIGURACIÓN ORIGINAL ====================
  @Value("${path.upload.files.api}")
  private String pathUploadFilesApi;

  // Variables de instancia para ambiente actual
  private String _endpoint;
  private String _username;
  private String _password;
  private String _urlToken;
  private String _clientId;

  // ==================== MÉTODOS ORIGINALES MANTENIDOS ====================

  /**
   * FIRMA ORIGINAL MANTENIDA - Scheduled para envío de documentos
   */
//  @Scheduled(fixedDelay = 60000L)
  public void EnviarComprobantesMH() {
    try {
      log.info("Preparando el entorno para enviar los documentos a MH");
      processDocumentsSending();
      log.info("Finalizo el proceso de envío");
    } catch (Exception e) {
      log.info("Mensaje de error generado por el envío a MH: " + e.getMessage());
    }
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Scheduled para consulta de estados
   */
//  @Scheduled(fixedDelay = 120000L)
  public void ConsultaComprobantesMH() {
    log.info("Preparando el entorno para consultar los documentos a MH");
    processDocumentsStatusCheck();
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Método de envío de facturas por email
   */
  public void enviaFacturas(String tipoDocumento, String clave, String emisor, String nombreEmpresa,
      String emailTo, String emailEmpresa, String logo, String notaFactura,
      String detalleFactura1, String detalleFactura2) throws Exception {

    if (!emailManagerUtil.isValidEmail(emailTo)) {
      log.warn("Email inválido para envío: {}", emailTo);
      return;
    }

    try (Connection dbConnection = dataSource.getConnection()) {
      // Generar PDF usando el util
      byte[] pdfBytes = reportGeneratorUtil.generateInvoicePdf(
          clave, tipoDocumento, logo, notaFactura, detalleFactura1, detalleFactura2, dbConnection
      );

      // Enviar email usando el util
      emailManagerUtil.sendInvoiceEmail(
          clave, tipoDocumento, emisor, nombreEmpresa, emailTo, emailEmpresa, pdfBytes
      );

    } catch (Exception e) {
      log.error("Error enviando factura por email: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Mapeo de tipos de documento
   */
  public String tipoDocumento(String td) {
    return documentTypeUtil.tipoDocumento(td);
  }

  // ==================== MÉTODOS AUXILIARES REFACTORIZADOS ====================

  /**
   * Procesa el envío de documentos pendientes
   */
  private void processDocumentsSending() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    List<ComprobantesElectronicos> listComprobantes = _comprobantesElectronicosService.findAllForSend();

    for (ComprobantesElectronicos ce : listComprobantes) {
      try {
        processIndividualDocumentSending(ce, objectMapper);
      } catch (Exception e) {
        log.error("Error procesando documento {}: {}", ce.getClave(), e.getMessage());
      }
    }
  }

  /**
   * Procesa el envío de un documento individual
   */
  private void processIndividualDocumentSending(ComprobantesElectronicos ce, ObjectMapper objectMapper) throws Exception {
    // Configurar ambiente
    configureEnvironmentForDocument(ce);

    // Obtener emisor
    Emisor emisor = _emisorService.findEmisorOnlyIdentificacion(ce.getIdentificacion());
    _username = emisor.getUserApi();
    _password = emisor.getPwApi();

    // Verificar que existe el archivo XML
    String xmlPath = buildXmlPath(ce);
    if (!_storageAdapter.fileExists(xmlPath)) {
      log.info("El XML del documento {} no existe en: {}", ce.getClave(), xmlPath);
      return;
    }

    // Enviar documento
    String response = _sender.send(
        ce.getClave(), _endpoint, xmlPath, _username, _password,
        _urlToken, _clientId, emisor.getIdentificacion(), ce.getTipoDocumento()
    );

    // Procesar respuesta
    JsonNode responseNode = objectMapper.readTree(response);
    _comprobantesElectronicosService.updateComprobantesElectronicosByClaveAndEmisor(
        responseNode.path("resp").asText(),
        responseNode.path("headers").asText(),
        ce.getClave(),
        ce.getIdentificacion()
    );
  }

  /**
   * Procesa la consulta de estados de documentos
   */
  private void processDocumentsStatusCheck() {
    ObjectMapper objectMapper = new ObjectMapper();
    List<ComprobantesElectronicos> listComprobantes = _comprobantesElectronicosService.findAllForCheckStatus();

    for (ComprobantesElectronicos ce : listComprobantes) {
      try {
        processIndividualDocumentStatusCheck(ce, objectMapper);
      } catch (Exception e) {
        log.error("Error consultando estado de {}: {}", ce.getClave(), e.getMessage());
      }
    }
  }

  /**
   * Procesa la consulta de estado de un documento individual
   */
  private void processIndividualDocumentStatusCheck(ComprobantesElectronicos ce, ObjectMapper objectMapper) throws Exception {
    // Configurar ambiente
    configureEnvironmentForDocument(ce);

    // Configurar credenciales
    _username = ce.getEmisor().getUserApi();
    _password = ce.getEmisor().getPwApi();

    // Procesar clave según tipo de documento
    String claveToQuery = buildQueryKey(ce);

    // Consultar estado
    String pathXml = pathUploadFilesApi + ce.getIdentificacion() + "/";
    String response = _sender.consultarEstadoDocumento(
        _endpoint, claveToQuery, _username, _password, _urlToken, pathXml, _clientId, ce.getIdentificacion()
    );

    // Procesar respuesta
    processStatusResponse(ce, response, objectMapper);
  }

  /**
   * Procesa la respuesta de consulta de estado
   */
  private void processStatusResponse(ComprobantesElectronicos ce, String response, ObjectMapper objectMapper) throws Exception {
    JsonNode responseNode = objectMapper.readTree(response);
    String estadoHacienda = responseNode.path("resp").asText();

    // Procesar estado nulo
    if (estadoHacienda != null && estadoHacienda.equalsIgnoreCase("null")) {
      estadoHacienda = "";
    }

    // Calcular reconsutas
    int reconsultas = (ce.getReconsultas() != null) ? ce.getReconsultas() + 1 : 1;

    // Enviar email si está aceptado y tiene email de distribución
    if (shouldSendAcceptanceEmail(estadoHacienda, ce)) {
      sendAcceptanceEmail(ce);
    }

    // Actualizar en base de datos
    String nameXmlAcceptacion = documentTypeUtil.buildResponseFileName(ce.getClave());
    _comprobantesElectronicosService.updateComprobantesElectronicosByClaveAndEmisor(
        nameXmlAcceptacion,
        responseNode.path("fecha").asText(),
        estadoHacienda,
        responseNode.path("headers").asText(),
        reconsultas,
        ce.getClave(),
        ce.getIdentificacion()
    );

    log.info("Documentos consultados con éxito para clave: {}", ce.getClave());
  }

  /**
   * Configura el ambiente para el documento
   */
  private void configureEnvironmentForDocument(ComprobantesElectronicos ce) {
    EnvironmentConfigUtil.EnvironmentConfig config = environmentConfigUtil.configureEnvironment(ce.getAmbiente());
    _endpoint = config.getEndpoint();
    _urlToken = config.getUrlToken();
    _clientId = config.getClientId();
  }

  /**
   * Construye la ruta del archivo XML
   */
  private String buildXmlPath(ComprobantesElectronicos ce) {
    return pathUploadFilesApi + ce.getIdentificacion() + "/" + ce.getNameXmlSign() + ".xml";
  }

  /**
   * Construye la clave para consulta según tipo de documento
   */
  private String buildQueryKey(ComprobantesElectronicos ce) {
    String clave = ce.getClave();
    String tipoDocumento = documentTypeUtil.getTipoDocumentoFromClave(clave);

    if (documentTypeUtil.isMensajeReceptor(tipoDocumento)) {
      MensajeReceptor mr = _mensajeReceptorService.findByClave(clave);
      return mr.getClaveDocumentoEmisor() + "-" + documentTypeUtil.getConsecutivoFromClave(mr.getClave());
    }

    return clave;
  }

  /**
   * Determina si debe enviar email de aceptación
   */
  private boolean shouldSendAcceptanceEmail(String estadoHacienda, ComprobantesElectronicos ce) {
    return "aceptado".equalsIgnoreCase(estadoHacienda) &&
        emailManagerUtil.isValidEmail(ce.getEmailDistribucion());
  }

  /**
   * Envía email de aceptación
   */
  private void sendAcceptanceEmail(ComprobantesElectronicos ce) {
    try {
      String tipoDocumentoDesc = documentTypeUtil.tipoDocumento(ce.getTipoDocumento());

      enviaFacturas(
          tipoDocumentoDesc,
          ce.getClave(),
          ce.getIdentificacion(),
          ce.getEmisor().getNombreComercial(),
          ce.getEmailDistribucion(),
          ce.getEmisor().getEmail(),
          ce.getEmisor().getLogoEmpresa(),
          ce.getEmisor().getNataFactura(),
          ce.getEmisor().getDetalleEnFactura1(),
          ce.getEmisor().getDetalleEnFactura2()
      );

      log.info("Se envió un mail de ACEPTACIÓN a: {}", ce.getEmailDistribucion());

    } catch (Exception e) {
      log.error("Error enviando email de aceptación: {}", e.getMessage());
    }
  }
}