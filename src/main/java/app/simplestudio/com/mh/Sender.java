package app.simplestudio.com.mh;

import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.TokenControl;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.ITokenControlService;
import app.simplestudio.com.util.FileManagerUtil;
import app.simplestudio.com.util.HttpClientUtil;
import app.simplestudio.com.util.JsonProcessorUtil;
import app.simplestudio.com.util.XmlParserUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
public class Sender {

  @Autowired
  private ITokenControlService _tokenControlService;

  @Autowired
  private IComprobantesElectronicosService _comprobantes;

  @Autowired
  private FuncionesService _funcionesService;

  // Nuevas clases auxiliares
  @Autowired
  private HttpClientUtil httpClientUtil;

  @Autowired
  private XmlParserUtil xmlParserUtil;

  @Autowired
  private JsonProcessorUtil jsonProcessorUtil;

  @Autowired
  private FileManagerUtil fileManagerUtil;

  private int timeoutMH = 35;
  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * FIRMA ORIGINAL MANTENIDA - crearToken
   */
  private void crearToken(String username, String password, String _urlToken, String _clientId, String emisorToken, String accion, String refreshToken) throws Exception {
    try {
      // Delegar a método auxiliar
      String responseString = executeTokenCreation(username, password, _urlToken, _clientId, accion, refreshToken);
      processTokenResponse(responseString, emisorToken, _urlToken, _clientId, username, password, refreshToken);
    } catch (Exception e) {
      log.info("Mensaje generado por el clase Sender: " + e.getMessage());
    }
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - getClave
   */
  public String getClave(String tipoDocumento, String tipoCedula, String cedula, String situacion, String codigoPais, String consecutivo, String codigoSeguridad, String sucursal, String terminal) {
    return generateClaveNumerica(tipoDocumento, tipoCedula, cedula, situacion, codigoPais, consecutivo, codigoSeguridad, sucursal, terminal);
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - generateXml
   */
  public void generateXml(String path, String datosXml, String name) throws Exception {
    fileManagerUtil.saveToFile(path + name + ".xml", datosXml);
    log.info("Archivo creado con éxito");
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - getToken
   */
  private String getToken(String username, String password, String _urlToken, String _clientId, String emisorToken) throws Exception {
    return manageTokenLifecycle(username, password, _urlToken, _clientId, emisorToken);
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - send
   */
  public String send(String clave, String endpoint, String xmlPath, String username, String password, String _urlToken, String _clientId, String emisorToken, String tipoDocumento) {
    String resp = "";
    try {
      // Verificar si ya existe el comprobante
      ComprobantesElectronicos ce = _comprobantes.findByClaveDocumento(clave);

      if (ce.getResponseCodeSend() == null) {
        log.info("Enviando comprobante por primera vez");
        resp = sendDocumentFirstTime(clave, endpoint, xmlPath, username, password, _urlToken, _clientId, emisorToken, tipoDocumento);
      } else {
        resp = handleAlreadySentDocument(clave, endpoint, xmlPath, username, password, _urlToken, _clientId, emisorToken, tipoDocumento);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return resp;
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - consultarSiMhLoRecibio
   */
  public String consultarSiMhLoRecibio(String endpoint, String clave, String username, String password, String _urlToken, String _clientId, String emisorToken) {
    String estadoMh = "";
    try {
      String token = getToken(username, password, _urlToken, _clientId, emisorToken);
      String url = endpoint + clave;

      try (CloseableHttpResponse response = httpClientUtil.executeGetWithBearer(url, token)) {
        String responseString = httpClientUtil.extractResponseContent(response);
        log.info("Metodo consultarSiMhLoRecibio");

        // Procesar respuesta
        estadoMh = processConsultaResponse(responseString);
      }
    } catch (Exception e) {
      e.printStackTrace();
      estadoMh = "error";
    }
    return estadoMh;
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - consultarEstadoDocumento
   */
  public String consultarEstadoDocumento(String endpoint, String clave, String username, String password, String _urlToken, String pathUploadFilesApi, String _clientId, String emisorToken) {
    String responseString;
    try {
      String token = getToken(username, password, _urlToken, _clientId, emisorToken);
      String url = endpoint + clave;

      try (CloseableHttpResponse response = httpClientUtil.executeGetWithBearer(url, token)) {
        String rawResponse = httpClientUtil.extractResponseContent(response);

        // Procesar respuesta y guardar XML
        responseString = processDocumentStatusResponse(rawResponse, pathUploadFilesApi, response.getStatusLine().getStatusCode());
      }
    } catch (Exception e) {
      log.info("Al parecer nunca se ha enviado");
      responseString = buildErrorStatusResponse();
    }
    return responseString;
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - consultarEstadoCualquierDocumento
   */
  public String consultarEstadoCualquierDocumento(String endpoint, String clave, String username, String password, String _urlToken, String pathUploadFilesApi, String _clientId, String emisorToken) {
    String responseString;
    try {
      String token = getToken(username, password, _urlToken, _clientId, emisorToken);
      String url = endpoint + clave;

      try (CloseableHttpResponse response = httpClientUtil.executeGetWithBearer(url, token)) {
        String rawResponse = httpClientUtil.extractResponseContent(response);
        log.info("Codigo generado por MH " + response.getStatusLine().getStatusCode());
        log.info("Errores generados: " + getHeaders(response.getAllHeaders()));

        // Procesar respuesta
        responseString = processAnyDocumentResponse(rawResponse);

        long duracionQuery = calculateDuration();
        log.info("Tiempo de consulta: " + duracionQuery + " segundos");
      }
    } catch (Exception e) {
      e.printStackTrace();
      responseString = buildAnyDocumentErrorResponse();
    }
    return responseString;
  }

  // ==================== MÉTODOS AUXILIARES ====================

  /**
   * Ejecuta la creación/refresh del token
   */
  private String executeTokenCreation(String username, String password, String _urlToken, String _clientId, String accion, String refreshToken) throws Exception {
    if ("R".equals(accion)) {
      return httpClientUtil.executeRefreshTokenRequest(_urlToken, refreshToken, _clientId);
    } else {
      return httpClientUtil.executeTokenRequest(_urlToken, username, password, _clientId);
    }
  }

  /**
   * Procesa respuesta del token y actualiza BD
   */
  private void processTokenResponse(String responseString, String emisorToken, String _urlToken, String _clientId, String username, String password, String refreshToken) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> res = objectMapper.readValue(responseString, new TypeReference<Map<String, Object>>() {});

    // Manejar errores de token
    if (res.get("error") != null && res.get("error").equals("invalid_grant")) {
      log.info("Algo paso con el refreshToken entonces voy a borrar el token para que se genere uno nuevo");
      logoutMh(username, password, _urlToken, _clientId, emisorToken, "", refreshToken);
      _tokenControlService.deleteTokenByEmisor(emisorToken);
    }

    // Actualizar token en BD
    updateTokenInDatabase(res, emisorToken);
  }

  /**
   * Maneja el ciclo de vida del token
   */
  private String manageTokenLifecycle(String username, String password, String _urlToken, String _clientId, String emisorToken) throws Exception {
    TokenControl t = _tokenControlService.findByEmisor(emisorToken);

    if (t != null) {
      return handleExistingToken(t, username, password, _urlToken, _clientId, emisorToken);
    } else {
      crearToken(username, password, _urlToken, _clientId, emisorToken, "N", "");
    }

    TokenControl tF = _tokenControlService.findByEmisor(emisorToken);
    return tF != null ? tF.getAccessToken() : "Error con el Token";
  }

  /**
   * Maneja token existente verificando expiración
   */
  private String handleExistingToken(TokenControl t, String username, String password, String _urlToken, String _clientId, String emisorToken) throws Exception {
    Long minutosVidaToken = t.getHoraCreacionToken() + Long.parseLong(t.getExpiresIn()) / 60L;
    Long diferencia = minutosVidaToken - System.currentTimeMillis() / 1000L / 60L;

    if (diferencia <= 0L) {
      return handleExpiredToken(t, username, password, _urlToken, _clientId, emisorToken);
    } else {
      return handleValidToken(t, username, password, _urlToken, _clientId, emisorToken);
    }
  }

  /**
   * Maneja token expirado
   */
  private String handleExpiredToken(TokenControl t, String username, String password, String _urlToken, String _clientId, String emisorToken) throws Exception {
    Long minutosVidaRefreshToken = t.getHoraCreacionRefreshToken() + Long.parseLong(t.getRefreshExpiresIn()) / 60L;
    Long diferenciaRefresh = minutosVidaRefreshToken - System.currentTimeMillis() / 1000L / 60L;

    if (diferenciaRefresh <= 0L) {
      log.info("______________Eliminando el token viejo______________");
      _tokenControlService.deleteTokenByEmisor(emisorToken);
      log.info("______________Genero un nuevo token, el refresh token expiro______________");
      crearToken(username, password, _urlToken, _clientId, emisorToken, "N", "");
    } else {
      log.info("______________Refresco el token con el refreshToken______________");
      crearToken(username, password, _urlToken, _clientId, emisorToken, "R", t.getRefreshTokens());
    }
    return null; // Se actualiza en crearToken
  }

  /**
   * Maneja token válido
   */
  private String handleValidToken(TokenControl t, String username, String password, String _urlToken, String _clientId, String emisorToken) throws Exception {
    Long minutosVidaRefreshToken = t.getHoraCreacionRefreshToken() + Long.parseLong(t.getRefreshExpiresIn()) / 60L;
    Long diferenciaRefresh = minutosVidaRefreshToken - System.currentTimeMillis() / 1000L / 60L;

    if (diferenciaRefresh <= 0L) {
      log.info("______________Eliminando el token viejo______________");
      _tokenControlService.deleteTokenByEmisor(emisorToken);
      log.info("______________Generando un nuevo token______________");
      crearToken(username, password, _urlToken, _clientId, emisorToken, "N", "");
      return null; // Se actualiza en crearToken
    } else {
      log.info("Sigo usando el mismo token");
      return t.getAccessToken();
    }
  }

  /**
   * Actualiza token en base de datos
   */
  private void updateTokenInDatabase(Map<String, Object> res, String emisorToken) {
    TokenControl tc = new TokenControl();
    TokenControl t = _tokenControlService.findByEmisor(emisorToken);
    Long horaCreacion = System.currentTimeMillis() / 1000L / 60L;

    if (t != null) {
      _tokenControlService.updateAccessToken(
          res.get("access_token").toString(),
          res.get("expires_in").toString(),
          horaCreacion,
          t.getId()
      );
    } else {
      tc.setEmisor(emisorToken);
      tc.setAccessToken(res.get("access_token").toString());
      tc.setExpiresIn(res.get("expires_in").toString());
      tc.setRefreshTokens(res.get("refresh_token").toString());
      tc.setRefreshExpiresIn(res.get("refresh_expires_in").toString());
      tc.setHoraCreacionToken(horaCreacion);
      tc.setHoraCreacionRefreshToken(System.currentTimeMillis() / 1000L / 60L);
      _tokenControlService.save(tc);
    }
  }

  /**
   * Genera clave numérica
   */
  private String generateClaveNumerica(String tipoDocumento, String tipoCedula, String cedula, String situacion, String codigoPais, String consecutivo, String codigoSeguridad, String sucursal, String terminal) {
    SimpleDateFormat format = new SimpleDateFormat("ddMMyy");
    String fechaHoy = format.format(new Date());

    // Validaciones usando FuncionesService existente
    if (cedula == null || cedula.isEmpty()) {
      return "{\"response\":\"El valor cédula no debe ser vacío\"}";
    }
    if (!_funcionesService.isNumeric(cedula)) {
      return "{\"response\":\"El valor cédula no es numérico\"}";
    }
    if (codigoPais == null || codigoPais.isEmpty()) {
      return "{\"response\":\"El valor código de país no debe ser vacío\"}";
    }
    if (!_funcionesService.isNumeric(codigoPais)) {
      return "{\"response\":\"El valor código de país no es numérico\"}";
    }

    // Formatear sucursal y terminal usando FuncionesService
    sucursal = formatSucursalValue(sucursal);
    if (sucursal.startsWith("{\"response\"")) return sucursal;

    terminal = formatTerminalValue(terminal);
    if (terminal.startsWith("{\"response\"")) return terminal;

    // Construir clave
    String clave = codigoPais + fechaHoy +
        _funcionesService.str_pad(tipoCedula, 2, "0", "STR_PAD_LEFT") +
        _funcionesService.str_pad(cedula, 12, "0", "STR_PAD_LEFT") +
        _funcionesService.str_pad(tipoDocumento, 2, "0", "STR_PAD_LEFT") +
        sucursal + terminal +
        _funcionesService.str_pad(consecutivo, 10, "0", "STR_PAD_LEFT") +
        _funcionesService.str_pad(situacion, 1, "0", "STR_PAD_LEFT") +
        _funcionesService.str_pad(codigoSeguridad, 8, "0", "STR_PAD_LEFT");

    return "{\"response\":\"" + clave + "\"}";
  }

  /**
   * Formatea valor de sucursal
   */
  private String formatSucursalValue(String sucursal) {
    if (sucursal == null || sucursal.isEmpty()) {
      return "001";
    }
    if (_funcionesService.isNumeric(sucursal)) {
      if (sucursal.length() < 3) {
        return _funcionesService.str_pad(sucursal, 3, "0", "STR_PAD_LEFT");
      } else if (sucursal.length() > 3) {
        return "{\"response\":\"Error en sucursal el tamaño es diferente de 3 dígitos\"}";
      }
      return sucursal;
    } else {
      return "{\"response\":\"El valor sucursal no es numeral\"}";
    }
  }

  /**
   * Formatea valor de terminal
   */
  private String formatTerminalValue(String terminal) {
    if (terminal == null || terminal.isEmpty()) {
      return "00001";
    }
    if (_funcionesService.isNumeric(terminal)) {
      if (terminal.length() < 5) {
        return _funcionesService.str_pad(terminal, 5, "0", "STR_PAD_LEFT");
      } else if (terminal.length() > 5) {
        return "{\"response\":\"Error en terminal el tamaño es diferente de 5 dígitos\"}";
      }
      return terminal;
    } else {
      return "{\"response\":\"El valor terminal no es numeral\"}";
    }
  }

  /**
   * Envía documento por primera vez
   */
  private String sendDocumentFirstTime(String clave, String endpoint, String xmlPath, String username, String password, String _urlToken, String _clientId, String emisorToken, String tipoDocumento) throws Exception {
    String json = buildJsonFromXmlFile(xmlPath, tipoDocumento);
    String token = getToken(username, password, _urlToken, _clientId, emisorToken);

    try (CloseableHttpResponse response = httpClientUtil.executePostWithBearer(endpoint, json, token)) {
      int responseCode = response.getStatusLine().getStatusCode();
      String responseHeaders = getHeaders(response.getAllHeaders());
      return buildSendResponse(responseCode, responseHeaders);
    }
  }

  /**
   * Maneja documento ya enviado
   */
  private String handleAlreadySentDocument(String clave, String endpoint, String xmlPath, String username, String password, String _urlToken, String _clientId, String emisorToken, String tipoDocumento) throws Exception {
    String respuestaGet = consultarSiMhLoRecibio(endpoint, clave, username, password, _urlToken, _clientId, emisorToken);

    if (respuestaGet != null && respuestaGet.equalsIgnoreCase("error")) {
      return sendDocumentFirstTime(clave, endpoint, xmlPath, username, password, _urlToken, _clientId, emisorToken, tipoDocumento);
    } else {
      _comprobantes.updateComprobantesElectronicosByClaveAndEmisor("202", "", clave, emisorToken);
      log.info("Ya se había enviado, solo actualizo su estado");
      return buildSendResponse(202, "");
    }
  }

  /**
   * Construye JSON desde archivo XML
   */
  private String buildJsonFromXmlFile(String xmlPath, String tipoDocumento) throws Exception {
    String xmlContent = fileManagerUtil.readFromFile(xmlPath);
    return convertXmlToJsonForSending(xmlContent);
  }

  /**
   * Convierte XML a JSON para envío
   */
  private String convertXmlToJsonForSending(String xml) throws Exception {
    Document xmlDoc = xmlParserUtil.parseXmlFromString(xml);
    return processXmlByDocumentType(xmlDoc, xml);
  }

  /**
   * Procesa XML según tipo de documento
   */
  private String processXmlByDocumentType(Document xmlDoc, String xml) throws Exception {
    // Determinar tipo desde el XML
    String rootElement = xmlDoc.getDocumentElement().getNodeName();

    // Usar JsonProcessorUtil según el tipo
    switch (rootElement) {
      case "MensajeReceptor":
        return jsonProcessorUtil.processMensajeReceptor(xmlDoc, "CCE");
      default:
        // Para facturas, notas, etc.
        return jsonProcessorUtil.processFacturaElectronica(xmlDoc, getDocumentTypeFromRoot(rootElement));
    }
  }

  /**
   * Obtiene tipo de documento desde elemento root
   */
  private String getDocumentTypeFromRoot(String rootElement) {
    switch (rootElement) {
      case "FacturaElectronica": return "FE";
      case "NotaDebitoElectronica": return "ND";
      case "NotaCreditoElectronica": return "NC";
      case "TiqueteElectronico": return "TE";
      case "FacturaElectronicaCompra": return "FEC";
      case "FacturaElectronicaExportacion": return "FEE";
      default: return "FE";
    }
  }

  /**
   * Procesa respuesta de consulta simple
   */
  private String processConsultaResponse(String responseString) {
    try {
      jsonProcessorUtil.parseJsonToMap(responseString);
      return "exito";
    } catch (Exception e) {
      return "error";
    }
  }

  /**
   * Procesa respuesta de estado de documento
   */
  private String processDocumentStatusResponse(String rawResponse, String pathUploadFilesApi, int statusCode) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> res = objectMapper.readValue(rawResponse, new TypeReference<Map<String, Object>>() {});

    String respuestaXML = (String) res.get("respuesta-xml");
    respuestaXML = new String(Base64.decodeBase64(respuestaXML), "UTF-8");

    // Guardar XML de respuesta
    generateXml(pathUploadFilesApi, respuestaXML, res.get("clave") + "-respuesta-mh");

    return "{\"resp\":\"" + res.get("ind-estado") + "\",\"fecha\":\"" + res.get("fecha") + "\",\"code\":\"" + statusCode + "\"}";
  }

  /**
   * Procesa respuesta de cualquier documento
   */
  private String processAnyDocumentResponse(String responseString) {
    try {
      jsonProcessorUtil.parseJsonToMap(responseString);
      return responseString; // Respuesta exitosa directa
    } catch (Exception e) {
      return buildAnyDocumentErrorResponse();
    }
  }

  /**
   * Construye respuesta de envío
   */
  private String buildSendResponse(int responseCode, String headers) {
    return "{\"resp\":\"" + responseCode + "\",\"headers\":\"" + headers + "\"}";
  }

  /**
   * Construye respuesta de error para estado
   */
  private String buildErrorStatusResponse() {
    return "{\"resp\":\"\",\"fecha\":\"\",\"code\":\"\"}";
  }

  /**
   * Construye respuesta de error para cualquier documento
   */
  private String buildAnyDocumentErrorResponse() {
    return "{\"clave\":\"\",\"fecha\":\"\",\"ind-estado\":\"\",\"respuesta-xml\":\"\"}";
  }

  /**
   * Calcula duración (simplificado)
   */
  private long calculateDuration() {
    return System.currentTimeMillis() / 1000; // Simplificado para el ejemplo
  }

  /**
   * Extrae headers - método original mantenido
   */
  private String getHeaders(Header[] headers) {
    String resp = "";
    for (Header header : headers) {
      if (header.getName().equalsIgnoreCase("X-Error-Cause")) {
        resp = resp.concat(header.getValue());
      }
    }
    return resp;
  }

  /**
   * Logout MH - método original mantenido
   */
  private void logoutMh(String username, String password, String _urlToken, String _clientId, String emisorToken, String accion, String refreshToken) throws Exception {
    try {
      CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
      HttpPost request = new HttpPost(_urlToken + "logout");
      List<NameValuePair> urlParameters = new ArrayList<>();
      urlParameters.add(new BasicNameValuePair("refresh_token", refreshToken));
      urlParameters.add(new BasicNameValuePair("client_id", _clientId));
      urlParameters.add(new BasicNameValuePair("client_secret", ""));
      urlParameters.add(new BasicNameValuePair("scope", ""));
      urlParameters.add(new BasicNameValuePair("username", username));
      urlParameters.add(new BasicNameValuePair("password", password));
      request.addHeader("content-type", "application/x-www-form-urlencoded");
      request.setEntity(new UrlEncodedFormEntity(urlParameters));
      HttpResponse response = closeableHttpClient.execute(request);
      HttpEntity entity = response.getEntity();
      String responseString = EntityUtils.toString(entity, "UTF-8");
      log.info(" Cerrando el token del cliente: " + emisorToken + " || " + responseString);
    } catch (Exception e) {
      log.info("Error generado por el metodo logoutMh: " + e.getMessage());
    }
  }
}