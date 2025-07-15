package app.simplestudio.com.controllers;

import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.util.DocumentTypeUtil;
import app.simplestudio.com.util.EmailManagerUtil;
import app.simplestudio.com.util.FileManagerUtil;
import app.simplestudio.com.util.ReportGeneratorUtil;
import app.simplestudio.com.util.ResendDocumentUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api-4.3"})
public class ReenviarDocsController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  // ==================== SERVICIOS ORIGINALES ====================
  @Autowired
  public JavaMailSender emailSender;

  @Autowired
  public DataSource dataSource;

  @Autowired
  public IComprobantesElectronicosService _comprobantesElectronicosService;

  @Autowired
  public IEmisorService _emisorService;

  // ==================== NUEVOS UTILS ====================
  @Autowired
  private DocumentTypeUtil documentTypeUtil;

  @Autowired
  private EmailManagerUtil emailManagerUtil;

  @Autowired
  private ReportGeneratorUtil reportGeneratorUtil;

  @Autowired
  private FileManagerUtil fileManagerUtil;

  @Autowired
  private ResendDocumentUtil resendDocumentUtil;

  // ==================== CONFIGURACIÓN ORIGINAL ====================

  @Value("${url.qr}")
  private String urlQr;

  @Value("${correo.de.distribucion}")
  private String correoDistribucion;

  // ==================== ENDPOINT ORIGINAL MANTENIDO ====================

  /**
   * FIRMA ORIGINAL MANTENIDA - Reenvío de documentos XML y PDF
   */
  @RequestMapping(value = {"/reenviar-xmls"}, method = {RequestMethod.POST},
      consumes = {"application/json"}, produces = {"application/json"})
  public String sendXmlAndPdf(@RequestBody String jsonRequest) throws IOException, SQLException {

    return processDocumentResend(jsonRequest);
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Mapeo de tipos de documento
   */
  public String tipoDocumento(String td) {
    return documentTypeUtil.tipoDocumento(td);
  }

  // ==================== MÉTODOS AUXILIARES REFACTORIZADOS ====================

  /**
   * Procesa el reenvío completo del documento
   */
  private String processDocumentResend(String jsonRequest) {
    Connection dbConnection = null;
    InputStream reportFile = null;

    try {
      // Parsear JSON request
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode requestData = objectMapper.readTree(jsonRequest);

      String clave = requestData.path("clave").asText();
      String email = requestData.path("correo").asText();

      // Logging de inicio
      resendDocumentUtil.logResendStart(clave, email);

      // Validaciones básicas
      Map<String, Object> requestValidation = resendDocumentUtil.validateResendRequest(clave, email);
      if (!(Boolean) requestValidation.get("valid")) {
        return resendDocumentUtil.buildValidationErrorResponse((String) requestValidation.get("message"));
      }

      // Validar formato de email
      if (!resendDocumentUtil.isValidEmail(email)) {
        return resendDocumentUtil.buildValidationErrorResponse("El formato del correo electrónico no es válido");
      }

      // Buscar documento
      ComprobantesElectronicos ce = _comprobantesElectronicosService.findByClave(clave);
      if (ce == null) {
        return resendDocumentUtil.buildDocumentNotFoundResponse();
      }

      // Validar que esté aceptado
      if (!resendDocumentUtil.isDocumentAccepted(ce)) {
        return resendDocumentUtil.buildDocumentNotAcceptedResponse();
      }

      // Obtener emisor
      Emisor emisor = _emisorService.findEmisorOnlyIdentificacion(ce.getIdentificacion());
      if (emisor == null) {
        return resendDocumentUtil.buildSystemErrorResponse();
      }

      // Validar archivos XML
      Map<String, String> xmlPaths = resendDocumentUtil.buildXmlFilePaths(ce.getIdentificacion(), clave);
      Map<String, Object> filesValidation = resendDocumentUtil.validateXmlFilesExist(xmlPaths);
      if (!(Boolean) filesValidation.get("valid")) {
        log.warn("Archivos XML faltantes para clave {}: {}", clave, filesValidation.get("missingFile"));
        return resendDocumentUtil.buildSystemErrorResponse();
      }

      // Generar PDF
      byte[] pdfBytes = generateInvoicePdf(clave, ce, emisor);

      // Enviar email con attachments
      sendDocumentEmail(ce, emisor, email, clave, xmlPaths, pdfBytes);

      // Log de éxito
      resendDocumentUtil.logResendSuccess(clave, email);

      return resendDocumentUtil.buildSuccessResponse(email);

    } catch (Exception e) {
      String email = extractEmailFromJson(jsonRequest);
      resendDocumentUtil.logResendError(extractClaveFromJson(jsonRequest), email, e.getMessage());
      return resendDocumentUtil.buildEmailErrorResponse(email, e.getMessage());

    } finally {
      // Limpiar recursos
      cleanupResources(dbConnection, reportFile);
    }
  }

  /**
   * Genera el PDF de la factura usando ReportGeneratorUtil
   */
  private byte[] generateInvoicePdf(String clave, ComprobantesElectronicos ce, Emisor emisor) throws Exception {
    Connection dbConnection = null;

    try {
      dbConnection = dataSource.getConnection();

      String tipoDocumentoDesc = resendDocumentUtil.getDocumentTypeDescription(ce.getTipoDocumento());
      String logoPath = resendDocumentUtil.buildLogoPath(emisor.getLogoEmpresa());

      // Usar ReportGeneratorUtil para generar PDF
      return reportGeneratorUtil.generateInvoicePdf(
          clave,
          tipoDocumentoDesc,
          logoPath,
          emisor.getNataFactura() != null ? emisor.getNataFactura() : "",
          emisor.getDetalleEnFactura1() != null ? emisor.getDetalleEnFactura1() : "",
          emisor.getDetalleEnFactura2() != null ? emisor.getDetalleEnFactura2() : "",
          dbConnection
      );

    } finally {
      if (dbConnection != null) {
        try {
          dbConnection.close();
        } catch (SQLException e) {
          log.warn("Error cerrando conexión de base de datos: {}", e.getMessage());
        }
      }
    }
  }

  /**
   * Envía email con los documentos adjuntos
   */
  private void sendDocumentEmail(ComprobantesElectronicos ce, Emisor emisor, String email,
      String clave, Map<String, String> xmlPaths, byte[] pdfBytes) throws Exception {

    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    // Configurar email
    String tipoDocumentoDesc = resendDocumentUtil.getDocumentTypeDescription(ce.getTipoDocumento());
    String subject = resendDocumentUtil.buildEmailSubject(tipoDocumentoDesc, emisor.getNombreRazonSocial(), clave);
    String htmlMessage = resendDocumentUtil.buildEmailMessage(tipoDocumentoDesc, emisor.getNombreRazonSocial(), clave, urlQr);

    helper.setFrom(correoDistribucion);
    helper.setTo(email);
    helper.setSubject(subject);
    helper.setText(htmlMessage, true);

    // Adjuntar archivos XML
    attachXmlFiles(helper, xmlPaths, clave);

    // Adjuntar PDF
    attachPdfFile(helper, pdfBytes, clave);

    // Enviar email
    emailSender.send(message);

    log.info("Email enviado exitosamente a: {} para clave: {}", email, clave);
  }

  /**
   * Adjunta los archivos XML al email
   */
  private void attachXmlFiles(MimeMessageHelper helper, Map<String, String> xmlPaths, String clave) throws Exception {
    // Adjuntar respuesta de Hacienda
    String respuestaMhPath = xmlPaths.get("respuestaMh");
    if (fileManagerUtil.fileExists(respuestaMhPath)) {
      FileSystemResource respuestaMhFile = new FileSystemResource(new File(respuestaMhPath));
      helper.addAttachment(clave + "-respuesta-mh.xml", respuestaMhFile, "application/xml");
      log.debug("Adjuntado archivo respuesta MH: {}", respuestaMhPath);
    }

    // Adjuntar factura firmada
    String facturaSignPath = xmlPaths.get("facturaSign");
    if (fileManagerUtil.fileExists(facturaSignPath)) {
      FileSystemResource facturaSignFile = new FileSystemResource(new File(facturaSignPath));
      helper.addAttachment(clave + "-factura-sign.xml", facturaSignFile, "application/xml");
      log.debug("Adjuntado archivo factura firmada: {}", facturaSignPath);
    }
  }

  /**
   * Adjunta el archivo PDF al email
   */
  private void attachPdfFile(MimeMessageHelper helper, byte[] pdfBytes, String clave) throws Exception {
    if (pdfBytes != null && pdfBytes.length > 0) {
      helper.addAttachment(clave + "-factura.pdf", () -> new java.io.ByteArrayInputStream(pdfBytes), "application/pdf");
      log.debug("Adjuntado PDF para clave: {}", clave);
    } else {
      log.warn("PDF bytes están vacíos para clave: {}", clave);
    }
  }

  /**
   * Limpia recursos utilizados
   */
  private void cleanupResources(Connection dbConnection, InputStream reportFile) {
    // Cerrar conexión de base de datos
    if (dbConnection != null) {
      try {
        dbConnection.close();
        log.debug("Conexión de base de datos cerrada");
      } catch (SQLException e) {
        log.error("Error cerrando conexión de base de datos: {}", e.getMessage());
      }
    }

    // Cerrar InputStream del reporte
    if (reportFile != null) {
      try {
        reportFile.close();
        log.debug("InputStream del reporte cerrado");
      } catch (IOException e) {
        log.error("Error cerrando InputStream del reporte: {}", e.getMessage());
      }
    }
  }

  // ==================== MÉTODOS AUXILIARES DE UTILIDAD ====================

  /**
   * Extrae la clave del JSON para logging en caso de error
   */
  private String extractClaveFromJson(String jsonRequest) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(jsonRequest);
      return node.path("clave").asText();
    } catch (Exception e) {
      return "UNKNOWN";
    }
  }

  /**
   * Extrae el email del JSON para logging en caso de error
   */
  private String extractEmailFromJson(String jsonRequest) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(jsonRequest);
      return node.path("correo").asText();
    } catch (Exception e) {
      return "UNKNOWN";
    }
  }
}