package app.simplestudio.com.controllers;

import app.simplestudio.com.dto.ConsecutiveCalculationResult;
import app.simplestudio.com.dto.EmisorValidationResult;
import app.simplestudio.com.dto.TerminalValidationResult;
import app.simplestudio.com.dto.ValidationResult;
import app.simplestudio.com.mh.IGeneraXml;
import app.simplestudio.com.mh.ISigner;
import app.simplestudio.com.mh.Sender;
import app.simplestudio.com.util.*;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.mh.CCampoFactura;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(maxAge = 3600L)
@RestController
@RequestMapping({"/api-4.3"})
public class NotaDebitoNotaCreditoAceptadasController {

  // ==================== SERVICIOS ORIGINALES MANTENIDOS ====================
  @Autowired private IGeneraXml _generaXml;
  @Autowired private ISigner _signer;
  @Autowired private Sender _sender;

  // ==================== NUEVOS UTILS PARA REFACTORIZACIÓN ====================
  @Autowired private NotaDebitoCreditoProcessingUtil processingUtil;
  @Autowired private NotaDebitoCreditoValidationUtil validationUtil;
  @Autowired private NotaDebitoCreditoMapperUtil mapperUtil;
  @Autowired private JsonBuilderUtil jsonBuilderUtil;
  @Autowired private ResponseBuilderUtil responseBuilderUtil;

  // ==================== CONFIGURACIÓN ORIGINAL MANTENIDA ====================
  @Value("${path.upload.files.api}") private String pathUploadFilesApi;
  @Value("${endpoint.prod}") private String endpointProd;
  @Value("${endpoint.stag}") private String endpointStag;
  @Value("${token.prod}") private String tokenProd;
  @Value("${token.stag}") private String tokenStag;

  private final Logger log = LoggerFactory.getLogger(getClass());

  // ==================== VARIABLES ORIGINALES MANTENIDAS ====================
  private String _endpoint;
  private String _username;
  private String _password;
  private String _urlToken;
  private String _clientId;
  private String _certificado;
  private String _keyCertificado;

  // ==================== MÉTODO ORIGINAL - SIN CAMBIOS DE FIRMA ====================
  @RequestMapping(value = {"/nota-debito-credito"}, method = {RequestMethod.POST},
      consumes = {"application/json; charset=utf-8"},
      produces = {"application/json; charset=utf-8"})
  public ResponseEntity<?> getNotaDebitoCredito(@RequestBody String facturaJson) {

    Map<String, Object> response;

    try {
      log.info("=== INICIO PROCESAMIENTO NOTA DÉBITO/CRÉDITO ===");

      // 1. VALIDACIÓN INICIAL DEL JSON
      ValidationResult initialValidation = validationUtil.validateInitialRequest(facturaJson);
      if (!initialValidation.isValid()) {
        return new ResponseEntity<>(initialValidation.getErrorResponse(),
            HttpStatus.valueOf(initialValidation.getHttpCode()));
      }

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode requestNode = objectMapper.readTree(facturaJson);

      // 2. VALIDACIÓN DE CAMPOS REQUERIDOS
      ValidationResult fieldsValidation = validationUtil.validateRequiredFields(requestNode);
      if (!fieldsValidation.isValid()) {
        return new ResponseEntity<>(fieldsValidation.getErrorResponse(),
            HttpStatus.valueOf(fieldsValidation.getHttpCode()));
      }

      // 3. VALIDACIÓN Y OBTENCIÓN DEL EMISOR
      EmisorValidationResult emisorValidation = processingUtil.validateAndGetEmisor(requestNode);
      if (!emisorValidation.isValid()) {
        return new ResponseEntity<>(emisorValidation.getErrorResponse(),
            HttpStatus.valueOf(emisorValidation.getHttpCode()));
      }

      Emisor emisor = emisorValidation.getEmisor();
      String tipoDocumento = requestNode.path("tipoDocumento").asText();

      // 4. VALIDACIÓN DE TIPO DE DOCUMENTO
      if (!validationUtil.isValidDocumentType(tipoDocumento)) {
        response = responseBuilderUtil.buildErrorResponse(401,
            "Solo se aceptan Notas de Débito y Notas de Crédito aceptadas por Hacienda.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
      }

      // 5. CONFIGURACIÓN DE AMBIENTE Y ENDPOINTS
      processingUtil.configureEnvironment(emisor, this);

      // 6. VALIDACIÓN Y OBTENCIÓN DE TERMINAL
      TerminalValidationResult terminalValidation = processingUtil.validateAndGetTerminal(
          emisor, requestNode);
      if (!terminalValidation.isValid()) {
        return new ResponseEntity<>(terminalValidation.getErrorResponse(),
            HttpStatus.valueOf(terminalValidation.getHttpCode()));
      }

      CTerminal terminal = terminalValidation.getTerminal();

      // 7. CÁLCULO DE CONSECUTIVOS
      ConsecutiveCalculationResult consecutiveResult = processingUtil.calculateConsecutives(
          emisor, tipoDocumento, requestNode, terminal);

      // 8. CONSTRUCCIÓN DEL OBJETO CCampoFactura
      CCampoFactura campoFactura = mapperUtil.buildCCampoFactura(
          requestNode, emisor, consecutiveResult, tipoDocumento);

      // 9. CONSTRUCCIÓN DE JSON PARA REFERENCIAS (Reemplazar concatenación manual)
      String referenciaJson = jsonBuilderUtil.buildReferenciaJson(requestNode);
      campoFactura.setReferencia(referenciaJson);

      // 10. MAPEO DE RECEPTOR
      mapperUtil.mapReceptorData(requestNode, campoFactura);

      // 11. CONSTRUCCIÓN DE DETALLES DE LÍNEA
      String detalleLineaJson = jsonBuilderUtil.buildDetalleLineaJson(requestNode);
      campoFactura.setDetalleFactura(detalleLineaJson);

      // 12. CÁLCULO DE TOTALES
      processingUtil.calculateTotals(campoFactura, requestNode);

      // 13. GENERACIÓN DE XML (SERVICIOS ORIGINALES)
      String xmlContent = _generaXml.GeneraXml(campoFactura);

      // 14. ALMACENAMIENTO DE ARCHIVOS Y FIRMA
      String nameFacturaFirmada = processingUtil.saveAndSignXmlFiles(
          campoFactura, xmlContent, _certificado, _keyCertificado, _generaXml, _signer);

      // 15. ENVÍO A HACIENDA (SERVICIOS ORIGINALES)
      String responseHacienda = _sender.send(
          campoFactura.getClave(), _endpoint,
          pathUploadFilesApi + emisor.getIdentificacion() + "/" + nameFacturaFirmada,
          _username, _password, _urlToken, _clientId, emisor.getIdentificacion(),
          tipoDocumento);

      // 16. PERSISTENCIA EN BASE DE DATOS
      processingUtil.saveToDatabase(campoFactura, emisor, nameFacturaFirmada);

      // 17. CONSTRUCCIÓN DE RESPUESTA EXITOSA
      response = responseBuilderUtil.buildSuccessResponse(
          campoFactura.getClave(),
          campoFactura.getConsecutivo(),
          campoFactura.getFechaEmision(),
          nameFacturaFirmada);

      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (Exception e) {
      log.error("Error procesando nota débito/crédito: {}", e.getMessage(), e);
      response = responseBuilderUtil.buildErrorResponse(404, e.getMessage());
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // ==================== GETTERS/SETTERS PARA CONFIGURACIÓN DE AMBIENTE ====================
  public String getEndpointProd() { return endpointProd; }
  public String getEndpointStag() { return endpointStag; }
  public String getTokenProd() { return tokenProd; }
  public String getTokenStag() { return tokenStag; }
  public String getPathUploadFilesApi() { return pathUploadFilesApi; }

  public void set_endpoint(String endpoint) { this._endpoint = endpoint; }
  public void set_urlToken(String urlToken) { this._urlToken = urlToken; }
  public void set_clientId(String clientId) { this._clientId = clientId; }
  public void set_certificado(String certificado) { this._certificado = certificado; }
  public void set_keyCertificado(String keyCertificado) { this._keyCertificado = keyCertificado; }
  public void set_username(String username) { this._username = username; }
  public void set_password(String password) { this._password = password; }
}