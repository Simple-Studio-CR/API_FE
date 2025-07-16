package app.simplestudio.com.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailManagerUtil {
    
    private static final Logger log = LoggerFactory.getLogger(EmailManagerUtil.class);
    
    @Autowired
    private JavaMailSender emailSender;
    
    @Value("${correo.de.distribucion}")
    private String correoDistribucion;
    
    @Value("${path.upload.files.api}")
    private String pathUploadFilesApi;
    
    /**
     * Configuración para envío de email
     */
    public static class EmailConfig {
        private String toEmail;
        private String toName;
        private String fromEmail;
        private String fromName;
        private String replyToEmail;
        private String replyToName;
        private String subject;
        private String htmlContent;
        
        // Builder pattern
        public static EmailConfig builder() {
            return new EmailConfig();
        }
        
        public EmailConfig to(String email, String name) {
            this.toEmail = email;
            this.toName = name;
            return this;
        }
        
        public EmailConfig from(String email, String name) {
            this.fromEmail = email;
            this.fromName = name;
            return this;
        }
        
        public EmailConfig replyTo(String email, String name) {
            this.replyToEmail = email;
            this.replyToName = name;
            return this;
        }
        
        public EmailConfig subject(String subject) {
            this.subject = subject;
            return this;
        }
        
        public EmailConfig htmlContent(String htmlContent) {
            this.htmlContent = htmlContent;
            return this;
        }
        
        // Getters
        public String getToEmail() { return toEmail; }
        public String getToName() { return toName; }
        public String getFromEmail() { return fromEmail; }
        public String getFromName() { return fromName; }
        public String getReplyToEmail() { return replyToEmail; }
        public String getReplyToName() { return replyToName; }
        public String getSubject() { return subject; }
        public String getHtmlContent() { return htmlContent; }
    }
    
    /**
     * Crea mensaje HTML para factura electrónica
     */
    public String buildInvoiceEmailContent(String tipoDocumento, String consecutivo, String nombreEmpresa) {
        StringBuilder msj = new StringBuilder();
        msj.append("<p style=\"font-family: Arial;\">Estimado cliente,</p>");
        msj.append("<p style=\"font-family: Arial;\">Le hacemos llegar el comprobante de <b>")
           .append(tipoDocumento)
           .append("</b> con el número de consecutivo <b>")
           .append(consecutivo)
           .append("</b>, generada por <b>")
           .append(nombreEmpresa)
           .append("</b></p>");
        msj.append("<p style=\"font-family: Arial;\">Saludos,</p>");
        msj.append("<p style=\"font-family: Arial;\"><b>").append(nombreEmpresa).append("</b></p>");
        return msj.toString();
    }
    
    /**
     * Envía email con archivos adjuntos (XML y PDF)
     */
    public void sendInvoiceEmail(String clave, String tipoDocumento, String emisorId, 
                               String nombreEmpresa, String emailTo, String emailEmpresa, 
                               byte[] pdfBytes) throws MessagingException, UnsupportedEncodingException {
        
        String consecutivo = clave.substring(21, 41);
        String htmlContent = buildInvoiceEmailContent(tipoDocumento, consecutivo, nombreEmpresa);
        
        EmailConfig config = EmailConfig.builder()
            .to(emailTo.trim(), tipoDocumento + " - " + nombreEmpresa)
            .from(correoDistribucion, tipoDocumento)
            .replyTo(emailEmpresa.trim(), nombreEmpresa)
            .subject(tipoDocumento + " - " + nombreEmpresa)
            .htmlContent(htmlContent);
        
        sendEmailWithAttachments(config, clave, emisorId, pdfBytes);
    }
    
    /**
     * Envía email con configuración personalizada y attachments
     */
    public void sendEmailWithAttachments(EmailConfig config, String clave, String emisorId, byte[] pdfBytes) 
            throws MessagingException, UnsupportedEncodingException {
        
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        // Configurar destinatarios y remitentes
        helper.setTo(new InternetAddress(config.getToEmail(), config.getToName()));
        helper.setFrom(new InternetAddress(config.getFromEmail(), config.getFromName()));
        helper.setReplyTo(config.getReplyToEmail(), config.getReplyToName());
        helper.setSubject(config.getSubject());
        helper.setText(config.getHtmlContent(), true);
        
        // Adjuntar archivos XML
        attachXmlFiles(helper, clave, emisorId);
        
        // Adjuntar PDF si existe
        if (pdfBytes != null && pdfBytes.length > 0) {
            ByteArrayDataSource pdfDataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
            helper.addAttachment(clave + "-factura.pdf", pdfDataSource);
        }
        
        try {
            emailSender.send(message);
            log.info("Email enviado exitosamente a: {}", config.getToEmail());
        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", config.getToEmail(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Adjunta archivos XML al email
     */
    private void attachXmlFiles(MimeMessageHelper helper, String clave, String emisorId) throws MessagingException {
        String basePath = pathUploadFilesApi + emisorId + "/";
        
        // XML de respuesta MH
        String responseXmlPath = basePath + clave + "-respuesta-mh.xml";
        File responseXmlFile = new File(responseXmlPath);
        if (responseXmlFile.exists()) {
            FileSystemResource responseXml = new FileSystemResource(responseXmlFile);
            helper.addAttachment(clave + "-respuesta-mh.xml", responseXml, "application/xml");
        }
        
        // XML firmado
        String signedXmlPath = basePath + clave + "-factura-sign.xml";
        File signedXmlFile = new File(signedXmlPath);
        if (signedXmlFile.exists()) {
            FileSystemResource signedXml = new FileSystemResource(signedXmlFile);
            helper.addAttachment(clave + "-factura-sign.xml", signedXml, "application/xml");
        }
    }
    
    /**
     * Verifica si un email es válido (básico)
     */
    public boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && email.contains("@");
    }
}