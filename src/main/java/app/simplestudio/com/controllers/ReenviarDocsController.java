package app.simplestudio.com.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api-4.3"})
public class ReenviarDocsController {
  @Autowired
  public JavaMailSender emailSender;
  
  @Autowired
  public DataSource dataSource;
  
  @Autowired
  public IComprobantesElectronicosService _comprobantesElectronicosService;
  
  @Autowired
  public IEmisorService _emisorService;
  
  @Value("${path.upload.files.api}")
  private String pathUploadFilesApi;
  
  @Value("${url.qr}")
  private String urlQr;
  
  @Value("${correo.de.distribucion}")
  private String correoDistribucion;
  
  @RequestMapping(value = {"/reenviar-xmls"}, method = {RequestMethod.POST}, consumes = {"application/json"}, produces = {"application/json"})
  public String sendXmlAndPdf(@RequestBody String j) throws JRException, IOException, SQLException, MessagingException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode m = objectMapper.readTree(j);
    String clave = m.path("clave").asText();
    String email = m.path("correo").asText();
    String resp = "";
    Connection db = this.dataSource.getConnection();
    InputStream reportfile = getClass().getResourceAsStream("/facturas.jasper");
    URL base = getClass().getResource("/");
    String baseUrl = base.toString();
    try {
      if (clave != null && clave.length() == 50 && email != null && email.length() > 0) {
        ComprobantesElectronicos ce = this._comprobantesElectronicosService.findByClave(clave);
        if (ce != null) {
          if (ce.getIndEstado() != null && ce.getIndEstado().equals("aceptado")) {
            System.out.println("correo " + ce.getIdentificacion());
            Emisor e = this._emisorService.findEmisorOnlyIdentificacion(ce.getIdentificacion());
            String td = tipoDocumento(ce.getTipoDocumento());
            String logo = e.getLogoEmpresa();
            if (logo != null && !logo.equals("") && logo.length() > 0) {
              logo = this.pathUploadFilesApi + "logo/" + logo;
            } else {
              logo = this.pathUploadFilesApi + "logo/default.png";
            } 
            Map<String, Object> parameter = new HashMap<>();
            parameter.put("BASE_URL", baseUrl);
            parameter.put("BASE_URL_LOGO", logo);
            parameter.put("CLAVE_FACTURA", clave);
            parameter.put("TIPO_DOCUMENTO", td);
            parameter.put("RESOLUCION", "“Autorizada mediante resolución Nº DGT-R-48-2016 del 7 de octubre de 2016”");
            parameter.put("NOTA_FACTURA", e.getNataFactura());
            parameter.put("URL_QR", this.urlQr + clave);
            byte[] bytes = JasperRunManager.runReportToPdf(reportfile, parameter, db);
            if (bytes != null && bytes.length > 0) {
              ByteArrayDataSource pdfBytes = new ByteArrayDataSource(bytes, "application/pdf");
              MimeMessage message = this.emailSender.createMimeMessage();
              MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
              String msj = null;
              msj = "<p style=\"font-family: Arial;\">Estimado cliente,</p>";
              msj = msj + "<p style=\"font-family: Arial;\">Le hacemos llegar el comprobante de <b>" + td + "</b> con el número de consecutivo <b>" + clave.substring(21, 41) + "</b>, generada por <b>" + e.getNombreRazonSocial() + "</b></p>";
              msj = msj + "<p style=\"font-family: Arial;\">Saludos,</p>";
              msj = msj + "<p style=\"font-family: Arial;\"><b>" + e.getNombreRazonSocial() + "</b></p>";
              helper.setTo(new InternetAddress(email, td + " - " + e.getNombreRazonSocial()));
              helper.setReplyTo(e.getEmail(), e.getNombreRazonSocial());
              helper.setFrom(new InternetAddress(this.correoDistribucion, td));
              helper.setSubject(td + " - " + e.getNombreRazonSocial());
              helper.setText(msj, true);
              String file1 = this.pathUploadFilesApi + ce.getIdentificacion() + "/" + clave + "-respuesta-mh.xml";
              String file2 = this.pathUploadFilesApi + ce.getIdentificacion() + "/" + clave + "-factura-sign.xml";
              FileSystemResource file_1 = new FileSystemResource(new File(file1));
              FileSystemResource file_2 = new FileSystemResource(new File(file2));
              helper.addAttachment("" + clave + "-respuesta-mh.xml", (InputStreamSource)file_1, "application/xml");
              helper.addAttachment("" + clave + "-factura-sign.xml", (InputStreamSource)file_2, "application/xml");
              helper.addAttachment("" + clave + "-factura.pdf", (jakarta.activation.DataSource)pdfBytes);
              this.emailSender.send(message);
              resp = resp + "{";
              resp = resp + "\"response\":\"1\",";
              resp = resp + "\"msj\":\"Se envío un correo con la factura a: " + email + "\"";
              resp = resp + "}";
              return resp;
            } 
            resp = resp + "{";
            resp = resp + "\"response\":\"3\",";
            resp = resp + "\"msj\":\"Error contacte al desarrollador del sistema.\"";
            resp = resp + "}";
            return resp;
          } 
          resp = resp + "{";
          resp = resp + "\"response\":\"3\",";
          resp = resp + "\"msj\":\"Este documento está a la espera de la aceptación del Ministerio de Hacienda, se podrá distribuir hasta que este aceptado.\"";
          resp = resp + "}";
          return resp;
        } 
        resp = resp + "{";
        resp = resp + "\"response\":\"3\",";
        resp = resp + "\"msj\":\"El documento que desea reenviar no existe!!!\"";
        resp = resp + "}";
        return resp;
      } 
      if (clave != null && clave.length() > 50) {
        resp = resp + "{";
        resp = resp + "\"response\":\"0\",";
        resp = resp + "\"La clave y debe ser de 50 digitos!!!\"";
        resp = resp + "}";
        return resp;
      } 
      resp = resp + "{";
      resp = resp + "\"response\":\"0\",";
      resp = resp + "\"La clave y el correo son requeridos!!!\"";
      resp = resp + "}";
      return resp;
    } catch (Exception ex) {
      resp = resp + "{";
      resp = resp + "\"response\":\"2\",";
      resp = resp + "\"msj\":\"Error al intentar enviar el correo a " + email + ", error generado: " + ex.getMessage() + "\"";
      resp = resp + "}";
      ex.printStackTrace();
      return resp;
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
    } 
    return resp;
  }
}

