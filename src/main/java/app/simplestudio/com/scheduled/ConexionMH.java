package app.simplestudio.com.scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.simplestudio.com.mh.Sender;
import app.simplestudio.com.mh.XmlHelper;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.models.entity.MensajeReceptor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IMensajeReceptorService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import javax.sql.DataSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Component
public class ConexionMH {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Autowired
  private IComprobantesElectronicosService _comprobantesElectronicosService;
  
  @Autowired
  private IEmisorService _emisorService;
  
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
  private Sender _sender;
  
  @Autowired
  public JavaMailSender emailSender;
  
  @Autowired
  public DataSource dataSource;
  
  @Autowired
  private IMensajeReceptorService _mensajeReceptorService;
  
  @Value("${correo.de.distribucion}")
  private String correoDistribucion;
  
  @Value("${url.qr}")
  private String urlQr;
  
  private String _endpoint;
  
  private String _username;
  
  private String _password;
  
  private String _urlToken;
  
  private String _clientId;
  
  @Scheduled(fixedDelay = 60000L)
  public void EnviarComprobantesMH() {
    try {
      this.log.info("Preparando el entorno para enviar los documentos a MH");
      ObjectMapper objectMapper = new ObjectMapper();
      List<ComprobantesElectronicos> listComprobantes = this._comprobantesElectronicosService.findAllForSend();
      for (ComprobantesElectronicos ce : listComprobantes) {
        Emisor e = this._emisorService.findEmisorOnlyIdentificacion(ce.getIdentificacion());
        if (ce.getAmbiente().equals("prod")) {
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
        String pathXml = this.pathUploadFilesApi + ce.getIdentificacion() + "/" + ce.getNameXmlSign() + ".xml";
        File f = new File(pathXml);
        if (f.exists() && !f.isDirectory()) {
          String resp = this._sender.send(ce.getClave(), this._endpoint, pathXml, this._username, this._password, this._urlToken, this._clientId, e
              .getIdentificacion(), ce.getTipoDocumento());
          JsonNode m = objectMapper.readTree(resp);
          this._comprobantesElectronicosService.updateComprobantesElectronicosByClaveAndEmisor(m
              .path("resp").asText(), m.path("headers").asText(), ce.getClave(), ce.getIdentificacion());
          continue;
        } 
        this.log.info("El xml del documento " + ce.getClave() + " no existe!!!");
      } 
      this.log.info("Finalizo el proceso de envío");
    } catch (Exception e) {
      this.log.info("Menaje de error generado por el envío a MH: " + e.getMessage());
    } 
  }
  
  @Scheduled(fixedDelay = 120000L)
  public void ConsultaComprobantesMH() {
    this.log.info("Preparando el entorno para consultar los documentos a MH");
    ObjectMapper objectMapper = new ObjectMapper();
    String nameXmlAcceptacion = "";
    List<ComprobantesElectronicos> listComprobantes = this._comprobantesElectronicosService.findAllForCheckStatus();
    for (ComprobantesElectronicos ce : listComprobantes) {
      try {
        if (ce.getAmbiente().equals("prod")) {
          this._endpoint = this.endpointProd;
          this._urlToken = this.tokenProd;
          this._clientId = "api-prod";
        } else {
          this._endpoint = this.endpointStag;
          this._urlToken = this.tokenStag;
          this._clientId = "api-stag";
        } 
        this._username = ce.getEmisor().getUserApi();
        this._password = ce.getEmisor().getPwApi();
        String clave = ce.getClave();
        String tipoDocumento = clave.substring(29, 31);
        if (tipoDocumento.equals("05") || tipoDocumento.equals("06") || tipoDocumento.equals("07")) {
          MensajeReceptor mr = this._mensajeReceptorService.findByClave(clave);
          clave = mr.getClaveDocumentoEmisor() + "-" + mr.getClave().substring(21, 41);
        } 
        String pathXml = this.pathUploadFilesApi + ce.getIdentificacion() + "/";
        String resp = this._sender.consultarEstadoDocumento(this._endpoint, clave, this._username, this._password, this._urlToken, pathXml, this._clientId, ce
            .getIdentificacion());
        JsonNode m = objectMapper.readTree(resp);
        String estadoHacienda = m.path("resp").asText();
        int reconsultas = (ce.getReconsultas() + "" != null) ? 1 : ce.getReconsultas().intValue();
        if (m.path("resp").asText().equalsIgnoreCase("rechazado")) {
          String td = tipoDocumento(ce.getTipoDocumento());
          String file1 = this.pathUploadFilesApi + ce.getIdentificacion() + "/" + clave + "-respuesta-mh.xml";
          FileSystemResource file_1 = new FileSystemResource(new File(file1));
          if (reconsultas <= 10) {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document xml = XmlHelper.getDocument(file1);
            NodeList nodes = (NodeList)xPath.evaluate("/MensajeHacienda/DetalleMensaje", xml.getDocumentElement(), XPathConstants.NODESET);
            if (nodes != null && nodes.item(0).getTextContent().equals("La firma del comprobante electrónico no es válida")) {
              estadoHacienda = "";
            } else {
              MimeMessage message = this.emailSender.createMimeMessage();
              MimeMessageHelper helper = new MimeMessageHelper(message, true);
              String msj = null;
              msj = "<p style=\"font-family: Arial;\">Estimado cliente,</p>";
              msj = msj + "<p style=\"font-family: Arial;\">El comprobante de <b>" + td + "</b> con el número de consecutivo <b>" + ce.getClave().substring(21, 41) + "</b>, presenta los siguientes errores, ver documento adjunto.</b></p>";
              msj = msj + "<p style=\"font-family: Arial;\">Saludos,</p>";
              msj = msj + "<p style=\"font-family: Arial;\"><b>" + ce.getEmisor().getNombreComercial() + "</b></p>";
              helper.setTo(ce.getEmisor().getEmailNotificacion());
              helper.setFrom(this.correoDistribucion);
              helper.setSubject(td + " - " + ce.getEmisor().getNombreComercial());
              helper.setText(msj, true);
              helper.addAttachment(ce.getClave() + "-respuesta-mh.xml", (InputStreamSource)file_1);
              this.emailSender.send(message);
              this.log.info("Se envío un mail de notificación de error a: " + ce.getEmisor().getEmailNotificacion());
              estadoHacienda = m.path("resp").asText();
            } 
            this.log.info("Error generado: " + nodes.item(0).getTextContent());
          } 
          reconsultas++;
        } 
        if (m.path("resp").asText().equalsIgnoreCase("aceptado") && ce.getEmailDistribucion() != null && 
          !ce.getEmailDistribucion().equals("")) {
          estadoHacienda = m.path("resp").asText();
          this.log.info("Se envío un mail de ACEPTACIÓN a: " + ce.getEmailDistribucion());
          enviaFacturas(ce.getTipoDocumento(), clave, ce.getIdentificacion(), ce
              .getEmisor().getNombreComercial(), ce.getEmailDistribucion(), ce.getEmisor().getEmail(), ce
              .getEmisor().getLogoEmpresa(), ce.getEmisor().getNataFactura(), ce
              .getEmisor().getDetalleEnFactura1(), ce.getEmisor().getDetalleEnFactura2());
        } 
        nameXmlAcceptacion = clave + "-respuesta-mh.xml";
        if (estadoHacienda != null && estadoHacienda.equalsIgnoreCase("null"))
          estadoHacienda = ""; 
        this._comprobantesElectronicosService.updateComprobantesElectronicosByClaveAndEmisor(nameXmlAcceptacion, m
            .path("fecha").asText(), estadoHacienda, m.path("headers").asText(), reconsultas, ce
            .getClave(), ce.getIdentificacion());
        this.log.info("Documentos consultados con éxito.");
      } catch (Exception e) {
        e.printStackTrace();
        this.log.info("Hacienda esta presentando problemas.");
      } 
    } 
  }
  
  public void enviaFacturas(String tipoDocumento, String clave, String emisor, String nombreEmpresa, String emailTo, String emailEmpresa, String logo, String notaFactura, String detalleFactura1, String detalleFactura2) throws JRException, IOException, SQLException, MessagingException {
    Connection db = this.dataSource.getConnection();
    InputStream reportfile = getClass().getResourceAsStream("/facturas.jasper");
    if (logo != null && !logo.equals("") && logo.length() > 0) {
      logo = this.pathUploadFilesApi + "logo/" + logo;
    } else {
      logo = this.pathUploadFilesApi + "logo/default.png";
    } 
    URL base = getClass().getResource("/");
    String baseUrl = base.toString();
    String td = tipoDocumento(tipoDocumento);
    Map<String, Object> parameter = new HashMap<>();
    parameter.put("BASE_URL", baseUrl);
    parameter.put("BASE_URL_LOGO", logo);
    parameter.put("CLAVE_FACTURA", clave);
    parameter.put("TIPO_DOCUMENTO", td);
    parameter.put("RESOLUCION", "“Autorizada mediante resolución Nº DGT-R-033-2019 del 20/06/2019”");
    parameter.put("NOTA_FACTURA", notaFactura);
    parameter.put("URL_QR", this.urlQr + clave);
    try {
      byte[] bytes = JasperRunManager.runReportToPdf(reportfile, parameter, db);
      if (bytes != null && bytes.length > 0) {
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(bytes, "application/pdf");
        MimeMessage message = this.emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        String msj = null;
        msj = "<p style=\"font-family: Arial;\">Estimado cliente,</p>";
        msj = msj + "<p style=\"font-family: Arial;\">Le hacemos llegar el comprobante de <b>" + td + "</b> con el número de consecutivo <b>" + clave.substring(21, 41) + "</b>, generada por <b>" + nombreEmpresa + "</b></p>";
        msj = msj + "<p style=\"font-family: Arial;\">Saludos,</p>";
        msj = msj + "<p style=\"font-family: Arial;\"><b>" + nombreEmpresa + "</b></p>";
        helper.setTo(new InternetAddress(emailTo.trim(), td + " - " + nombreEmpresa));
        helper.setReplyTo(emailEmpresa.trim(), nombreEmpresa);
        helper.setFrom(new InternetAddress(this.correoDistribucion, td));
        helper.setSubject(td + " - " + nombreEmpresa);
        helper.setText(msj, true);
        String file1 = this.pathUploadFilesApi + emisor + "/" + clave + "-respuesta-mh.xml";
        String file2 = this.pathUploadFilesApi + emisor + "/" + clave + "-factura-sign.xml";
        FileSystemResource file_1 = new FileSystemResource(new File(file1));
        FileSystemResource file_2 = new FileSystemResource(new File(file2));
        helper.addAttachment("" + clave + "-respuesta-mh.xml", file_1, "application/xml");
        helper.addAttachment("" + clave + "-factura-sign.xml", file_2, "application/xml");
        helper.addAttachment("" + clave + "-factura.pdf", byteArrayDataSource);
        try {
          this.emailSender.send(message);
          this.log.info("Se envío un mail a " + emailTo);
        } catch (Exception e) {
          this.log.info("No se pudo enviar el correo a " + emailTo);
        } 
      } 
    } catch (JRException ex) {
      System.out.println("Error del reporte: " + ex.getMessage());
    } finally {
      reportfile.close();
      try {
        if (db != null)
          db.close(); 
      } catch (SQLException e) {
        System.out.println("Error: desconectando la base de datos.");
      } 
    } 
  }
  
  public String tipoDocumento(String td) {
    String resp = "";
    switch (td) {
      case "FE":
        resp = "Factura Electrónica";
        break;
      case "ND":
        resp = "Nota de débito Electrónica";
        break;
      case "NC":
        resp = "Nota de crédito Electrónica";
        break;
      case "TE":
        resp = "Tiquete Electrónico";
        break;
      case "FEC":
        resp = "Factura Electrónica Compra";
        break;
      case "FEE":
        resp = "Factura Electrónica Exportación";
        break;
    } 
    return resp;
  }
}

