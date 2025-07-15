package app.simplestudio.com.mh;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.TokenControl;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.ITokenControlService;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


@Service
public class Sender {
  @Autowired
  private ITokenControlService _tokenControlService;
  
  @Autowired
  private IComprobantesElectronicosService _comprobantes;
  
  @Autowired
  private FuncionesService _funcionesService;
  
  private int timeoutMH = 35;
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private void crearToken(String username, String password, String _urlToken, String _clientId, String emisorToken, String accion, String refreshToken) throws Exception {
    String responseString = null;
    CloseableHttpClient client = null;
    CloseableHttpResponse response = null;
    try {
      SSLContext sslContext = (new SSLContextBuilder()).loadTrustMaterial(null, (certificate, authType) -> true).build();
      client = HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier((HostnameVerifier)new NoopHostnameVerifier()).build();
      HttpPost httpPost = new HttpPost(_urlToken + "token");
      List<NameValuePair> urlParameters = new ArrayList<>();
      if (accion.equals("R")) {
        urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
        urlParameters.add(new BasicNameValuePair("refresh_token", refreshToken));
        this.log.info("Refrescando token...");
      } else {
        urlParameters.add(new BasicNameValuePair("grant_type", "password"));
        this.log.info("Generando un nuevo token...");
      } 
      urlParameters.add(new BasicNameValuePair("client_id", _clientId));
      urlParameters.add(new BasicNameValuePair("client_secret", ""));
      urlParameters.add(new BasicNameValuePair("scope", ""));
      urlParameters.add(new BasicNameValuePair("username", username));
      urlParameters.add(new BasicNameValuePair("password", password));
      httpPost.addHeader("content-type", "application/x-www-form-urlencoded");
      httpPost.setEntity((HttpEntity)new UrlEncodedFormEntity(urlParameters));
      Long startTime = Long.valueOf(System.currentTimeMillis());
      RequestConfig config = RequestConfig.custom().setConnectTimeout(this.timeoutMH * 1000).setConnectionRequestTimeout(this.timeoutMH * 1000).setSocketTimeout(this.timeoutMH * 1000).build();
      httpPost.setConfig(config);
      response = client.execute((HttpUriRequest)httpPost);
      HttpEntity entity2 = response.getEntity();
      responseString = EntityUtils.toString(entity2, "UTF-8");
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> res = (Map<String, Object>)objectMapper.readValue(responseString, new TypeReference<Map<String, Object>>() {
          
          });
      Long actualMenosInicio = Long.valueOf(System.currentTimeMillis() - startTime.longValue());
      Double duracionQuery = Double.valueOf(Double.valueOf(actualMenosInicio + "").doubleValue() / 1000.0D);
      this.log.info("Duració generando token: " + duracionQuery);
      TokenControl tc = new TokenControl();
      TokenControl t = this._tokenControlService.findByEmisor(emisorToken);
      if (res.get("error") != null && res.get("error").equals("invalid_grant")) {
        this.log.info("Algo paso con el refreshToken entonces voy a borrar el token para que se genere uno nuevo");
        logoutMh(username, password, _urlToken, _clientId, emisorToken, "", refreshToken);
        this._tokenControlService.deleteTokenByEmisor(emisorToken);
      } 
      Long horaCreacion = Long.valueOf(System.currentTimeMillis() / 1000L / 60L);
      if (t != null) {
        this._tokenControlService.updateAccessToken(res.get("access_token").toString(), res.get("expires_in").toString(), horaCreacion, t.getId());
      } else {
        tc.setEmisor(emisorToken);
        tc.setAccessToken(res.get("access_token").toString());
        tc.setExpiresIn(res.get("expires_in").toString());
        tc.setRefreshTokens(res.get("refresh_token").toString());
        tc.setRefreshExpiresIn(res.get("refresh_expires_in").toString());
        tc.setHoraCreacionToken(horaCreacion);
        tc.setHoraCreacionRefreshToken(Long.valueOf(System.currentTimeMillis() / 1000L / 60L));
        this._tokenControlService.save(tc);
      } 
    } catch (Exception e) {
      this.log.info("Mensaje generado por el clase Sender: " + e.getMessage());
    } 
  }
  
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
      request.setEntity((HttpEntity)new UrlEncodedFormEntity(urlParameters));
      HttpResponse response = closeableHttpClient.execute((HttpUriRequest)request);
      HttpEntity entity = response.getEntity();
      String responseString = EntityUtils.toString(entity, "UTF-8");
      this.log.info(" Cerrando el token del cliente: " + emisorToken + " || " + responseString);
    } catch (Exception e) {
      this.log.info("Error generado por el metodo logoutMh: " + e.getMessage());
    } 
  }
  
  private String getToken(String username, String password, String _urlToken, String _clientId, String emisorToken) throws Exception {
    String resp = null;
    TokenControl t = this._tokenControlService.findByEmisor(emisorToken);
    if (t != null) {
      Long minutosVidaToken = Long.valueOf(t.getHoraCreacionToken().longValue() + Long.valueOf(t.getExpiresIn()).longValue() / 60L);
      this.log.info("Vida del accessToken " + minutosVidaToken);
      Long diferencia = Long.valueOf(minutosVidaToken.longValue() - System.currentTimeMillis() / 1000L / 60L);
      this.log.info("Diferiencia: " + diferencia);
      if (diferencia.longValue() <= 0L) {
        minutosVidaToken = Long.valueOf(t.getHoraCreacionRefreshToken().longValue() + Long.valueOf(t.getRefreshExpiresIn()).longValue() / 60L);
        diferencia = Long.valueOf(minutosVidaToken.longValue() - System.currentTimeMillis() / 1000L / 60L);
        this.log.info("Diferencia del refreshToken: " + diferencia);
        if (diferencia.longValue() <= 0L) {
          this.log.info("______________Eliminando el token viejo______________");
          this._tokenControlService.deleteTokenByEmisor(emisorToken);
          this.log.info("______________Genero un nuevo token, el refresh token expiro______________");
          crearToken(username, password, _urlToken, _clientId, emisorToken, "N", "");
        } else {
          this.log.info("______________Refresco el token con el refreshToken______________");
          crearToken(username, password, _urlToken, _clientId, emisorToken, "R", t.getRefreshTokens());
        } 
      } else {
        minutosVidaToken = Long.valueOf(t.getHoraCreacionRefreshToken().longValue() + Long.valueOf(t.getRefreshExpiresIn()).longValue() / 60L);
        diferencia = Long.valueOf(minutosVidaToken.longValue() - System.currentTimeMillis() / 1000L / 60L);
        if (diferencia.longValue() <= 0L) {
          this.log.info("______________Eliminando el token viejo______________");
          this._tokenControlService.deleteTokenByEmisor(emisorToken);
          this.log.info("______________Generando un nuevo token______________");
          crearToken(username, password, _urlToken, _clientId, emisorToken, "N", "");
        } else {
          this.log.info("Sigo usando el mismo token");
        } 
      } 
    } else {
      crearToken(username, password, _urlToken, _clientId, emisorToken, "N", "");
    } 
    TokenControl tF = this._tokenControlService.findByEmisor(emisorToken);
    if (tF != null) {
      resp = tF.getAccessToken();
    } else {
      resp = "Error con el Token";
    } 
    return resp;
  }
  
  public String send(String clave, String endpoint, String xmlPath, String username, String password, String _urlToken, String _clientId, String emisorToken, String tipoDocumento) {
    String resp = "";
    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      ObjectMapper objectMapper = new ObjectMapper();
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
      File file = new File(xmlPath);
      byte[] bytes = FileUtils.readFileToString(file, "UTF-8").getBytes("UTF-8");
      String base64 = Base64.encodeBase64String(bytes);
      ComprobanteElectronico comprobanteElectronico = new ComprobanteElectronico();
      MensajeReceptorMh mr = new MensajeReceptorMh();
      comprobanteElectronico.setComprobanteXml(base64);
      mr.setComprobanteXml(base64);
      ObligadoTributario receptor = new ObligadoTributario();
      ObligadoTributario emisor = new ObligadoTributario();
      Document xml = XmlHelper.getDocument(xmlPath);
      NodeList nodes = null, nodeReceptor = null;
      String json = "";
      switch (tipoDocumento) {
        case "FE":
          nodes = (NodeList)xPath.evaluate("/FacturaElectronica/Clave", xml.getDocumentElement(), XPathConstants.NODESET);
          comprobanteElectronico.setClave(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/FacturaElectronica/FechaEmision", xml.getDocumentElement(), XPathConstants.NODESET);
          nodes = (NodeList)xPath.evaluate("/FacturaElectronica/Emisor/Identificacion/Tipo", xml
              .getDocumentElement(), XPathConstants.NODESET);
          emisor.setTipoIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/FacturaElectronica/Emisor/Identificacion/Numero", xml
              .getDocumentElement(), XPathConstants.NODESET);
          emisor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodeReceptor = (NodeList)xPath.evaluate("/FacturaElectronica/Receptor/Identificacion/Tipo", xml
              .getDocumentElement(), XPathConstants.NODESET);
          nodeReceptor = (NodeList)xPath.evaluate("/FacturaElectronica/Receptor/Identificacion/Numero", xml
              .getDocumentElement(), XPathConstants.NODESET);
          if (nodeReceptor != null && !nodeReceptor.equals("") && nodeReceptor.getLength() > 5) {
            receptor.setTipoIdentificacion(nodeReceptor.item(0).getTextContent());
            receptor.setNumeroIdentificacion(nodeReceptor.item(0).getTextContent());
          } 
          comprobanteElectronico.setFecha(format.format(new Date()));
          comprobanteElectronico.setReceptor(receptor);
          comprobanteElectronico.setEmisor(emisor);
          json = objectMapper.writeValueAsString(comprobanteElectronico);
          break;
        case "ND":
          nodes = (NodeList)xPath.evaluate("/NotaDebitoElectronica/Clave", xml.getDocumentElement(), XPathConstants.NODESET);
          comprobanteElectronico.setClave(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/NotaDebitoElectronica/FechaEmision", xml.getDocumentElement(), XPathConstants.NODESET);
          nodes = (NodeList)xPath.evaluate("/NotaDebitoElectronica/Emisor/Identificacion/Tipo", xml
              .getDocumentElement(), XPathConstants.NODESET);
          emisor.setTipoIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/NotaDebitoElectronica/Emisor/Identificacion/Numero", xml
              .getDocumentElement(), XPathConstants.NODESET);
          emisor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodeReceptor = (NodeList)xPath.evaluate("/NotaDebitoElectronica/Receptor/Identificacion/Tipo", xml
              .getDocumentElement(), XPathConstants.NODESET);
          nodeReceptor = (NodeList)xPath.evaluate("/NotaDebitoElectronica/Receptor/Identificacion/Numero", xml
              .getDocumentElement(), XPathConstants.NODESET);
          if (nodeReceptor != null && !nodeReceptor.equals("") && nodeReceptor.getLength() > 5) {
            receptor.setTipoIdentificacion(nodeReceptor.item(0).getTextContent());
            receptor.setNumeroIdentificacion(nodeReceptor.item(0).getTextContent());
          } 
          comprobanteElectronico.setFecha(format.format(new Date()));
          comprobanteElectronico.setEmisor(emisor);
          json = objectMapper.writeValueAsString(comprobanteElectronico);
          break;
        case "NC":
          nodes = (NodeList)xPath.evaluate("/NotaCreditoElectronica/Clave", xml.getDocumentElement(), XPathConstants.NODESET);
          comprobanteElectronico.setClave(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/NotaCreditoElectronica/FechaEmision", xml.getDocumentElement(), XPathConstants.NODESET);
          nodes = (NodeList)xPath.evaluate("/NotaCreditoElectronica/Emisor/Identificacion/Tipo", xml
              .getDocumentElement(), XPathConstants.NODESET);
          emisor.setTipoIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/NotaCreditoElectronica/Emisor/Identificacion/Numero", xml
              .getDocumentElement(), XPathConstants.NODESET);
          emisor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodeReceptor = (NodeList)xPath.evaluate("/NotaCreditoElectronica/Receptor/Identificacion/Tipo", xml
              .getDocumentElement(), XPathConstants.NODESET);
          nodeReceptor = (NodeList)xPath.evaluate("/NotaCreditoElectronica/Receptor/Identificacion/Numero", xml
              .getDocumentElement(), XPathConstants.NODESET);
          if (nodeReceptor != null && !nodeReceptor.equals("") && nodeReceptor.getLength() > 5) {
            receptor.setTipoIdentificacion(nodeReceptor.item(0).getTextContent());
            receptor.setNumeroIdentificacion(nodeReceptor.item(0).getTextContent());
          } 
          comprobanteElectronico.setFecha(format.format(new Date()));
          comprobanteElectronico.setEmisor(emisor);
          json = objectMapper.writeValueAsString(comprobanteElectronico);
          break;
        case "TE":
          nodes = (NodeList)xPath.evaluate("/TiqueteElectronico/Clave", xml.getDocumentElement(), XPathConstants.NODESET);
          comprobanteElectronico.setClave(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/TiqueteElectronico/FechaEmision", xml.getDocumentElement(), XPathConstants.NODESET);
          nodes = (NodeList)xPath.evaluate("/TiqueteElectronico/Emisor/Identificacion/Tipo", xml.getDocumentElement(), XPathConstants.NODESET);
          emisor.setTipoIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/TiqueteElectronico/Emisor/Identificacion/Numero", xml
              .getDocumentElement(), XPathConstants.NODESET);
          emisor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodeReceptor = (NodeList)xPath.evaluate("/TiqueteElectronico/Receptor/Identificacion/Tipo", xml
              .getDocumentElement(), XPathConstants.NODESET);
          nodeReceptor = (NodeList)xPath.evaluate("/TiqueteElectronico/Receptor/Identificacion/Numero", xml
              .getDocumentElement(), XPathConstants.NODESET);
          if (nodeReceptor != null && !nodeReceptor.equals("") && nodeReceptor.getLength() > 5) {
            receptor.setTipoIdentificacion(nodeReceptor.item(0).getTextContent());
            receptor.setNumeroIdentificacion(nodeReceptor.item(0).getTextContent());
          } 
          comprobanteElectronico.setFecha(format.format(new Date()));
          comprobanteElectronico.setEmisor(emisor);
          json = objectMapper.writeValueAsString(comprobanteElectronico);
          break;
        case "FEC":
          nodes = (NodeList)xPath.evaluate("/FacturaElectronicaCompra/Clave", xml.getDocumentElement(), XPathConstants.NODESET);
          comprobanteElectronico.setClave(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/FacturaElectronicaCompra/FechaEmision", xml.getDocumentElement(), XPathConstants.NODESET);
          nodes = (NodeList)xPath.evaluate("/FacturaElectronicaCompra/Receptor/Identificacion/Tipo", xml.getDocumentElement(), XPathConstants.NODESET);
          emisor.setTipoIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/FacturaElectronicaCompra/Receptor/Identificacion/Numero", xml.getDocumentElement(), XPathConstants.NODESET);
          emisor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodeReceptor = (NodeList)xPath.evaluate("/FacturaElectronicaCompra/Emisor/Identificacion/Tipo", xml.getDocumentElement(), XPathConstants.NODESET);
          nodeReceptor = (NodeList)xPath.evaluate("/FacturaElectronicaCompra/Emisor/Identificacion/Numero", xml.getDocumentElement(), XPathConstants.NODESET);
          if (nodeReceptor != null && !nodeReceptor.equals("") && nodeReceptor.getLength() > 5) {
            receptor.setTipoIdentificacion(nodeReceptor.item(0).getTextContent());
            receptor.setNumeroIdentificacion(nodeReceptor.item(0).getTextContent());
          } 
          comprobanteElectronico.setFecha(format.format(new Date()));
          comprobanteElectronico.setReceptor(receptor);
          comprobanteElectronico.setEmisor(emisor);
          json = objectMapper.writeValueAsString(comprobanteElectronico);
          break;
        case "FEE":
          nodes = (NodeList)xPath.evaluate("/FacturaElectronicaExportacion/Clave", xml.getDocumentElement(), XPathConstants.NODESET);
          comprobanteElectronico.setClave(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/FacturaElectronicaExportacion/FechaEmision", xml.getDocumentElement(), XPathConstants.NODESET);
          nodes = (NodeList)xPath.evaluate("/FacturaElectronicaExportacion/Emisor/Identificacion/Tipo", xml.getDocumentElement(), XPathConstants.NODESET);
          emisor.setTipoIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/FacturaElectronicaExportacion/Emisor/Identificacion/Numero", xml.getDocumentElement(), XPathConstants.NODESET);
          emisor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodeReceptor = (NodeList)xPath.evaluate("/FacturaElectronicaExportacion/Receptor/Identificacion/Tipo", xml.getDocumentElement(), XPathConstants.NODESET);
          nodeReceptor = (NodeList)xPath.evaluate("/FacturaElectronicaExportacion/Receptor/Identificacion/Numero", xml.getDocumentElement(), XPathConstants.NODESET);
          if (nodeReceptor != null && !nodeReceptor.equals("") && nodeReceptor.getLength() > 5) {
            receptor.setTipoIdentificacion(nodeReceptor.item(0).getTextContent());
            receptor.setNumeroIdentificacion(nodeReceptor.item(0).getTextContent());
          } 
          comprobanteElectronico.setFecha(format.format(new Date()));
          comprobanteElectronico.setReceptor(receptor);
          comprobanteElectronico.setEmisor(emisor);
          json = objectMapper.writeValueAsString(comprobanteElectronico);
          break;
        case "CCE":
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/Clave", xml.getDocumentElement(), XPathConstants.NODESET);
          mr.setClave(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/NumeroCedulaEmisor", xml.getDocumentElement(), XPathConstants.NODESET);
          emisor.setTipoIdentificacion("01");
          emisor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/FechaEmisionDoc", xml.getDocumentElement(), XPathConstants.NODESET);
          mr.setFecha(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/NumeroCedulaReceptor", xml.getDocumentElement(), XPathConstants.NODESET);
          receptor.setTipoIdentificacion("01");
          receptor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/NumeroConsecutivoReceptor", xml
              .getDocumentElement(), XPathConstants.NODESET);
          mr.setConsecutivoReceptor(nodes.item(0).getTextContent());
          mr.setFecha(format.format(new Date()));
          mr.setEmisor(emisor);
          mr.setReceptor(receptor);
          json = objectMapper.writeValueAsString(mr);
          break;
        case "CPCE":
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/Clave", xml.getDocumentElement(), XPathConstants.NODESET);
          mr.setClave(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/NumeroCedulaEmisor", xml.getDocumentElement(), XPathConstants.NODESET);
          emisor.setTipoIdentificacion("01");
          emisor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/FechaEmisionDoc", xml.getDocumentElement(), XPathConstants.NODESET);
          mr.setFecha(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/NumeroCedulaReceptor", xml.getDocumentElement(), XPathConstants.NODESET);
          receptor.setTipoIdentificacion("01");
          receptor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/NumeroConsecutivoReceptor", xml
              .getDocumentElement(), XPathConstants.NODESET);
          mr.setConsecutivoReceptor(nodes.item(0).getTextContent());
          mr.setFecha(format.format(new Date()));
          mr.setEmisor(emisor);
          mr.setReceptor(receptor);
          json = objectMapper.writeValueAsString(mr);
          break;
        case "RCE":
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/Clave", xml.getDocumentElement(), XPathConstants.NODESET);
          mr.setClave(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/NumeroCedulaEmisor", xml.getDocumentElement(), XPathConstants.NODESET);
          emisor.setTipoIdentificacion("01");
          emisor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/FechaEmisionDoc", xml.getDocumentElement(), XPathConstants.NODESET);
          mr.setFecha(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/NumeroCedulaReceptor", xml.getDocumentElement(), XPathConstants.NODESET);
          receptor.setTipoIdentificacion("01");
          receptor.setNumeroIdentificacion(nodes.item(0).getTextContent());
          nodes = (NodeList)xPath.evaluate("/MensajeReceptor/NumeroConsecutivoReceptor", xml
              .getDocumentElement(), XPathConstants.NODESET);
          mr.setConsecutivoReceptor(nodes.item(0).getTextContent());
          mr.setFecha(format.format(new Date()));
          mr.setEmisor(emisor);
          mr.setReceptor(receptor);
          json = objectMapper.writeValueAsString(mr);
          break;
      } 
      ComprobantesElectronicos ce = this._comprobantes.findByClaveDocumento(clave);
      int responseCode = 0;
      String respuestaGet = "";
      String responseHeaders = "";
      if (ce.getResponseCodeSend() == null) {
        this.log.info("Enviando comprobante por primera vez");
        String token = getToken(username, password, _urlToken, _clientId, emisorToken);
        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(endpoint);
        StringEntity params = new StringEntity(json);
        request.addHeader("content-type", "application/javascript");
        request.addHeader("Authorization", "bearer " + token);
        request.setEntity((HttpEntity)params);
        RequestConfig config = RequestConfig.custom().setConnectTimeout(this.timeoutMH * 1000).setConnectionRequestTimeout(this.timeoutMH * 1000).setSocketTimeout(this.timeoutMH * 1000).build();
        request.setConfig(config);
        HttpResponse response = closeableHttpClient.execute((HttpUriRequest)request);
        responseCode = response.getStatusLine().getStatusCode();
        responseHeaders = getHeaders(response.getAllHeaders());
      } else {
        respuestaGet = consultarSiMhLoRecibio(endpoint, clave, username, password, _urlToken, _clientId, emisorToken);
        if (respuestaGet != null && respuestaGet.equalsIgnoreCase("error")) {
          String token = getToken(username, password, _urlToken, _clientId, emisorToken);
          CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
          HttpPost request = new HttpPost(endpoint);
          StringEntity params = new StringEntity(json);
          request.addHeader("content-type", "application/javascript");
          request.addHeader("Authorization", "bearer " + token);
          request.setEntity((HttpEntity)params);
          RequestConfig config = RequestConfig.custom().setConnectTimeout(this.timeoutMH * 1000).setConnectionRequestTimeout(this.timeoutMH * 1000).setSocketTimeout(this.timeoutMH * 1000).build();
          request.setConfig(config);
          HttpResponse response = closeableHttpClient.execute((HttpUriRequest)request);
          responseCode = response.getStatusLine().getStatusCode();
          responseHeaders = getHeaders(response.getAllHeaders());
          this.log.info("Enví el comprobante por que al parecer no se habí enviado");
        } else {
          this._comprobantes.updateComprobantesElectronicosByClaveAndEmisor("202", "", clave, emisorToken);
          responseCode = 202;
          responseHeaders = "";
          this.log.info("Ya se habí enviado, solo actualizo su estado");
        } 
      } 
      resp = resp + "{";
      if (responseCode == 202) {
        resp = resp + "\"resp\":\"202\",";
        resp = resp + "\"headers\":\"" + responseHeaders + "\"";
      } else {
        resp = resp + "\"resp\":\"" + responseCode + "\",";
        resp = resp + "\"headers\":\"" + responseHeaders + "\"";
      } 
      resp = resp + "}";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return resp;
  }
  
  public String consultarSiMhLoRecibio(String endpoint, String clave, String username, String password, String _urlToken, String _clientId, String emisorToken) {
    String responseString = null;
    CloseableHttpClient client = null;
    CloseableHttpResponse response = null;
    String estadoMh = "";
    try {
      String url = endpoint + clave;
      String token = getToken(username, password, _urlToken, _clientId, emisorToken);
      SSLContext sslContext = (new SSLContextBuilder()).loadTrustMaterial(null, (certificate, authType) -> true).build();
      client = HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier((HostnameVerifier)new NoopHostnameVerifier()).build();
      HttpGet httpGet = new HttpGet(url);
      httpGet.addHeader("Authorization", "bearer " + token);
      response = client.execute((HttpUriRequest)httpGet);
      this.log.info("Metodo consultarSiMhLoRecibio");
      HttpEntity entity2 = response.getEntity();
      responseString = EntityUtils.toString(entity2, "UTF-8");
      try {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> res = (Map<String, Object>)objectMapper.readValue(responseString, new TypeReference<Map<String, Object>>() {
            
            });
        estadoMh = "exito";
      } catch (Exception e) {
        estadoMh = "error";
      } 
      this.log.info("Respuesta de la petició: " + estadoMh);
    } catch (Exception e) {
      e.printStackTrace();
      responseString = "{";
      responseString = responseString + "\"resp\":\"\",";
      responseString = responseString + "\"code\":\"" + response.getStatusLine().getStatusCode() + "\"";
      responseString = responseString + "}";
    } finally {
      if (response != null)
        try {
          client.close();
        } catch (Exception exception) {} 
    } 
    return estadoMh;
  }
  
  private void printHeaders(Header[] headers) {
    for (Header header : headers)
      System.out.println(header.getName() + ": " + header.getValue()); 
  }
  
  public String consultarEstadoCualquierDocumento(String endpoint, String clave, String username, String password, String _urlToken, String pathUploadFilesApi, String _clientId, String emisorToken) {
    String responseString = null;
    CloseableHttpClient client = null;
    CloseableHttpResponse response = null;
    try {
      String url = endpoint + clave;
      String token = getToken(username, password, _urlToken, _clientId, emisorToken);
      SSLContext sslContext = (new SSLContextBuilder()).loadTrustMaterial(null, (certificate, authType) -> true).build();
      client = HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier((HostnameVerifier)new NoopHostnameVerifier()).build();
      HttpGet httpGet = new HttpGet(url);
      httpGet.addHeader("Authorization", "bearer " + token);
      long startTime = System.currentTimeMillis();
      response = client.execute((HttpUriRequest)httpGet);
      HttpEntity entity2 = response.getEntity();
      responseString = EntityUtils.toString(entity2, "UTF-8");
      this.log.info("Codigo generado por MH " + response.getStatusLine().getStatusCode());
      this.log.info("Errores generados: " + getHeaders(response.getAllHeaders()));
      String estadoMh = "";
      try {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> res = (Map<String, Object>)objectMapper.readValue(responseString, new TypeReference<Map<String, Object>>() {
            
            });
        estadoMh = "ok";
      } catch (Exception e) {
        estadoMh = "error";
      } 
      this.log.info("Respuesta de la petició metodo consultarEstadoCualquierDocumento: " + estadoMh);
      long durationResta = System.currentTimeMillis() - startTime;
      Double duracionQuery = Double.valueOf(Double.valueOf(durationResta + "").doubleValue() / 1000.0D);
      this.log.info("Tiempo de consulta: " + duracionQuery + " segundos");
    } catch (Exception e) {
      e.printStackTrace();
      responseString = "{";
      responseString = responseString + "\"clave\":\"\",";
      responseString = responseString + "\"fecha\":\"\",";
      responseString = responseString + "\"ind-estado\":\"\",";
      responseString = responseString + "\"respuesta-xml\":\"" + response.getStatusLine().getStatusCode() + "\"";
      responseString = responseString + "}";
    } finally {
      if (client != null)
        try {
          client.close();
        } catch (Exception exception) {} 
      if (response != null)
        try {
          response.close();
        } catch (Exception exception) {} 
    } 
    return responseString;
  }
  
  public String consultarEstadoDocumento(String endpoint, String clave, String username, String password, String _urlToken, String pathUploadFilesApi, String _clientId, String emisorToken) {
    String respuestaXML = null;
    String responseString = null;
    CloseableHttpClient client = null;
    CloseableHttpResponse response = null;
    try {
      String url = endpoint + clave;
      String token = getToken(username, password, _urlToken, _clientId, emisorToken);
      SSLContext sslContext = (new SSLContextBuilder()).loadTrustMaterial(null, (certificate, authType) -> true).build();
      client = HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier((HostnameVerifier)new NoopHostnameVerifier()).build();
      HttpGet httpGet = new HttpGet(url);
      httpGet.addHeader("Authorization", "bearer " + token);
      RequestConfig config = RequestConfig.custom().setConnectTimeout(this.timeoutMH * 1000).setConnectionRequestTimeout(this.timeoutMH * 1000).setSocketTimeout(this.timeoutMH * 1000).build();
      httpGet.setConfig(config);
      response = client.execute((HttpUriRequest)httpGet);
      HttpEntity entity2 = response.getEntity();
      responseString = EntityUtils.toString(entity2, "UTF-8");
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> res = (Map<String, Object>)objectMapper.readValue(responseString, new TypeReference<Map<String, Object>>() {
          
          });
      respuestaXML = (String)res.get("respuesta-xml");
      respuestaXML = new String(Base64.decodeBase64(respuestaXML), "UTF-8");
      generateXml(pathUploadFilesApi, respuestaXML, (String)res.get("clave") + "-respuesta-mh");
      responseString = "{";
      responseString = responseString + "\"resp\":\"" + (String)res.get("ind-estado") + "\",";
      responseString = responseString + "\"fecha\":\"" + (String)res.get("fecha") + "\",";
      responseString = responseString + "\"code\":\"" + response.getStatusLine().getStatusCode() + "\"";
      responseString = responseString + "}";
    } catch (Exception e) {
      this.log.info("Al parecer nunca se ha enviado");
      responseString = "{";
      responseString = responseString + "\"resp\":\"\",";
      responseString = responseString + "\"fecha\":\"\",";
      responseString = responseString + "\"code\":\"" + response.getStatusLine().getStatusCode() + "\"";
      responseString = responseString + "}";
    } finally {
      if (response != null)
        try {
          client.close();
        } catch (Exception exception) {} 
    } 
    return responseString;
  }
  
  public void generateXml(String path, String datosXml, String name) throws Exception {
    BufferedWriter bw;
    File archivo = new File(path + name + ".xml");
    if (archivo.exists()) {
      bw = new BufferedWriter(new FileWriter(archivo));
      bw.write(datosXml);
      System.out.println("Archivo creado con éito");
    } else {
      bw = new BufferedWriter(new FileWriter(archivo));
      bw.write(datosXml);
      System.out.println("Archivo creado con éito");
    } 
    bw.close();
  }
  
  private String getHeaders(Header[] headers) {
    String resp = "";
    for (Header header : headers) {
      if (header.getName().equalsIgnoreCase("X-Error-Cause"))
        resp = resp.concat(header.getValue()); 
    } 
    return resp;
  }
  
  public String getClave(String tipoDocumento, String tipoCedula, String cedula, String situacion, String codigoPais, String consecutivo, String codigoSeguridad, String sucursal, String terminal) {
    String response = null;
    SimpleDateFormat format = new SimpleDateFormat("ddMMyy");
    String fechaHoy = format.format(new Date());
    if (cedula != null && cedula.length() == 0)
      return "{\"response\":\"El valor céula no debe ser vacio\"}"; 
    if (!this._funcionesService.isNumeric(cedula))
      return "{\"response\":\"El valor céula no es numéico\"}"; 
    if (codigoPais != null && codigoPais.length() == 0)
      return "{\"response\":\"El valor cóigo de paí no debe ser vacio\"}"; 
    if (!this._funcionesService.isNumeric(codigoPais))
      return "{\"response\":\"El valor cóigo de paí no es núerico\"}"; 
    if (sucursal != null && sucursal.length() == 0) {
      sucursal = "001";
    } else if (this._funcionesService.isNumeric(sucursal)) {
      if (sucursal.length() < 3) {
        sucursal = this._funcionesService.str_pad(sucursal, 3, "0", "STR_PAD_LEFT");
      } else if (sucursal.length() > 3 && !sucursal.equals("0")) {
        return "{\"response\":\"Error en sucursal el tamañ es diferente de 3 digitos\"}";
      } 
    } else {
      return "{\"response\":\"El valor sucursal no es numeral\"}";
    } 
    if (terminal != null && terminal.length() == 0) {
      terminal = "00001";
    } else if (this._funcionesService.isNumeric(terminal)) {
      if (terminal.length() < 5) {
        terminal = this._funcionesService.str_pad(terminal, 5, "0", "STR_PAD_LEFT");
      } else if (terminal.length() != 5 && !terminal.equals("0")) {
        return "{\"response\":\"Error en la terminal, el tamañ es diferente de 5 digitos\"}";
      } 
    } else {
      return "{\"response\":\"El valor terminal no es numeral\"}";
    } 
    if (consecutivo != null && consecutivo.length() == 0)
      return "{\"response\":\"El consecutivo no puede ser vacio\"}"; 
    if (consecutivo.length() < 10) {
      consecutivo = this._funcionesService.str_pad(consecutivo, 10, "0", "STR_PAD_LEFT");
    } else if (consecutivo.length() != 10 && !consecutivo.equals("0")) {
      return "{\"response\":\"Error en consecutivo, el tamañ del consecutivo es diferente de 10 digitos\"}";
    } 
    if (codigoSeguridad != null && codigoSeguridad.length() == 0)
      return "{\"response\":\"El codigo de seguridad no puede ser vacio\"}"; 
    if (codigoSeguridad.length() < 8) {
      codigoSeguridad = this._funcionesService.str_pad(codigoSeguridad, 8, "0", "STR_PAD_LEFT");
    } else if (codigoSeguridad.length() != 8 && !codigoSeguridad.equals("0")) {
      return "{\"response\":\"Error en codigo Seguridad, el tamañ codigo de seguridad es diferente de 8 digitos\"}";
    } 
    String[] tipos = { "FE", "ND", "NC", "TE", "CCE", "CPCE", "RCE", "FEE", "FEC" };
    if (ArrayUtils.contains((Object[])tipos, tipoDocumento)) {
      switch (tipoDocumento) {
        case "FE":
          tipoDocumento = "01";
          break;
        case "ND":
          tipoDocumento = "02";
          break;
        case "NC":
          tipoDocumento = "03";
          break;
        case "TE":
          tipoDocumento = "04";
          break;
        case "CCE":
          tipoDocumento = "05";
          break;
        case "CPCE":
          tipoDocumento = "06";
          break;
        case "RCE":
          tipoDocumento = "07";
          break;
        case "FEC":
          tipoDocumento = "08";
          break;
        case "FEE":
          tipoDocumento = "09";
          break;
      } 
    } else {
      return "{\"response\":\"No se encuentra tipo de documento\"}";
    } 
    String consecutivoFinal = sucursal + terminal + tipoDocumento + consecutivo;
    String identificacion = null;
    String[] cedulas = { "fisico", "juridico", "dimex", "nite", "01", "02", "03", "04" };
    if (ArrayUtils.contains((Object[])cedulas, tipoCedula)) {
      switch (tipoCedula) {
        case "fisico":
          identificacion = this._funcionesService.str_pad(cedula, 12, "0", "STR_PAD_LEFT");
          break;
        case "01":
          identificacion = this._funcionesService.str_pad(cedula, 12, "0", "STR_PAD_LEFT");
          break;
        case "juridico":
          if (cedula.length() < 12) {
            identificacion = this._funcionesService.str_pad(cedula, 12, "0", "STR_PAD_LEFT");
            break;
          } 
          if (cedula.length() == 12) {
            identificacion = cedula;
            break;
          } 
          return "{\"response\":\"Céula juríico incorrecto\"}";
        case "02":
          if (cedula.length() < 12) {
            identificacion = this._funcionesService.str_pad(cedula, 12, "0", "STR_PAD_LEFT");
            break;
          } 
          if (cedula.length() == 12) {
            identificacion = cedula;
            break;
          } 
          return "{\"response\":\"Céula juríico incorrecto\"}";
        case "dimex":
          if (cedula.length() < 12) {
            identificacion = this._funcionesService.str_pad(cedula, 12, "0", "STR_PAD_LEFT");
            break;
          } 
          if (cedula.length() == 12) {
            identificacion = cedula;
            break;
          } 
          return "{\"response\":\"Dimex incorrecto\"}";
        case "03":
          if (cedula.length() < 12) {
            identificacion = this._funcionesService.str_pad(cedula, 12, "0", "STR_PAD_LEFT");
            break;
          } 
          if (cedula.length() == 12) {
            identificacion = cedula;
            break;
          } 
          return "{\"response\":\"Dimex incorrecto\"}";
        case "nite":
          identificacion = this._funcionesService.str_pad(cedula, 12, "0", "STR_PAD_LEFT");
          break;
        case "04":
          identificacion = this._funcionesService.str_pad(cedula, 12, "0", "STR_PAD_LEFT");
          break;
      } 
    } else {
      return "{\"response\":\"No se encuentra tipo de céula\"}";
    } 
    String[] situaciones = { "normal", "contingencia", "sininternet" };
    if (ArrayUtils.contains((Object[])situaciones, situacion)) {
      switch (situacion) {
        case "normal":
          situacion = "1";
          break;
        case "contingencia":
          situacion = "2";
          break;
        case "sininternet":
          situacion = "3";
          break;
      } 
    } else {
      return "{\"response\":\"No se encuentra el tipo de situació\"}";
    } 
    String clave = codigoPais + fechaHoy + identificacion + consecutivoFinal + situacion + codigoSeguridad;
    response = "{";
    response = response + "\"response\":\"202\",";
    response = response + "\"clave\":\"" + clave + "\",";
    response = response + "\"consecutivo\":\"" + consecutivoFinal + "\",";
    response = response + "\"length\":\"" + clave.length() + "\"";
    response = response + "}";
    return response;
  }
}

