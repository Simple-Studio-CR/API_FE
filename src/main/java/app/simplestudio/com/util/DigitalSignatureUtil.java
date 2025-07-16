package app.simplestudio.com.util;

import app.simplestudio.com.service.adapter.StorageAdapter;
import java.io.File;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xades4j.production.Enveloped;
import xades4j.production.XadesSigner;
import xades4j.providers.KeyingDataProvider;

@Component
public class DigitalSignatureUtil {
    
    private static final Logger log = LoggerFactory.getLogger(DigitalSignatureUtil.class);
    
    @Autowired
    private CertificateManagerUtil certificateManagerUtil;
    
    @Autowired
    private XadesSignatureUtil xadesSignatureUtil;
    
    @Autowired
    private StorageAdapter fileManagerUtil;
    
    /**
     * Resultado de la operación de firma
     */
    public static class SignatureResult {
        private boolean success;
        private String message;
        private String signedXmlPath;
        private long executionTimeMs;
        private Exception exception;
        
        public SignatureResult(boolean success, String message, String signedXmlPath, long executionTimeMs) {
            this(success, message, signedXmlPath, executionTimeMs, null);
        }
        
        public SignatureResult(boolean success, String message, String signedXmlPath, long executionTimeMs, Exception exception) {
            this.success = success;
            this.message = message;
            this.signedXmlPath = signedXmlPath;
            this.executionTimeMs = executionTimeMs;
            this.exception = exception;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getSignedXmlPath() { return signedXmlPath; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public Exception getException() { return exception; }
    }
    
    /**
     * Parámetros completos para la firma digital
     */
    public static class SignatureParameters {
        private String keyStorePath;
        private String password;
        private String xmlInputPath;
        private String xmlOutputPath;
        private XadesSignatureUtil.XadesConfig xadesConfig;
        private CertificateManagerUtil.CertificateConfig certificateConfig;
        
        public SignatureParameters(String keyStorePath, String password, String xmlInputPath, String xmlOutputPath) {
            this.keyStorePath = keyStorePath;
            this.password = password;
            this.xmlInputPath = xmlInputPath;
            this.xmlOutputPath = xmlOutputPath;
            this.xadesConfig = new XadesSignatureUtil.XadesConfig(); // Default Hacienda config
            this.certificateConfig = new CertificateManagerUtil.CertificateConfig(keyStorePath, password);
        }
        
        // Getters
        public String getKeyStorePath() { return keyStorePath; }
        public String getPassword() { return password; }
        public String getXmlInputPath() { return xmlInputPath; }
        public String getXmlOutputPath() { return xmlOutputPath; }
        public XadesSignatureUtil.XadesConfig getXadesConfig() { return xadesConfig; }
        public CertificateManagerUtil.CertificateConfig getCertificateConfig() { return certificateConfig; }
        
        // Setters para personalización
        public void setXadesConfig(XadesSignatureUtil.XadesConfig xadesConfig) { this.xadesConfig = xadesConfig; }
        public void setCertificateConfig(CertificateManagerUtil.CertificateConfig certificateConfig) { this.certificateConfig = certificateConfig; }
    }
    
    /**
     * Firma un documento XML usando XAdES-BES/EPES
     * MANTIENE LA MISMA FUNCIONALIDAD que el método original sign()
     */
    public SignatureResult signXmlDocument(SignatureParameters params) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validar parámetros
            validateSignatureParameters(params);
            
            // Crear recursos para firma
            Document xmlDocument = loadXmlDocument(params.getXmlInputPath());
            KeyingDataProvider keyingProvider = certificateManagerUtil.createKeyingDataProvider(params.getCertificateConfig());
            XadesSigner signer = xadesSignatureUtil.createXadesSigner(keyingProvider, params.getXadesConfig());
            
            // Realizar firma
            Element elementToSign = xmlDocument.getDocumentElement();
            performDigitalSignature(signer, elementToSign);
            
            // Guardar documento firmado
            saveSignedDocument(xmlDocument, params.getXmlOutputPath());
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("Documento firmado exitosamente en {} ms: {}", executionTime, params.getXmlOutputPath());
            return new SignatureResult(true, "Documento firmado con éxito", params.getXmlOutputPath(), executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Error firmando documento: {}", e.getMessage(), e);
            return new SignatureResult(false, "Error firmando documento: " + e.getMessage(), null, executionTime, e);
        }
    }
    
    /**
     * Valida los parámetros de firma
     */
    private void validateSignatureParameters(SignatureParameters params) throws Exception {
        if (params == null) {
            throw new IllegalArgumentException("Parámetros de firma no pueden ser nulos");
        }
        
        if (params.getXmlInputPath() == null || params.getXmlInputPath().trim().isEmpty()) {
            throw new IllegalArgumentException("Ruta del XML de entrada es requerida");
        }
        
        if (params.getXmlOutputPath() == null || params.getXmlOutputPath().trim().isEmpty()) {
            throw new IllegalArgumentException("Ruta del XML de salida es requerida");
        }
        
        // Validar que el archivo XML existe
        if (!fileManagerUtil.fileExists(params.getXmlInputPath())) {
            throw new Exception("El archivo XML no existe: " + params.getXmlInputPath());
        }
        
        // Validar configuración del certificado
        certificateManagerUtil.validateCertificateConfig(params.getCertificateConfig());
        
        // Validar configuración XAdES
        if (!xadesSignatureUtil.validateXadesConfig(params.getXadesConfig())) {
            throw new Exception("Configuración XAdES inválida");
        }
        
        log.debug("Parámetros de firma validados exitosamente");
    }
    
    /**
     * Carga el documento XML para firma
     */
    private Document loadXmlDocument(String xmlPath) throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // Crítico para XAdES
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(xmlPath));
            
            log.debug("Documento XML cargado exitosamente: {}", xmlPath);
            return document;
            
        } catch (Exception e) {
            log.error("Error cargando documento XML: {}", e.getMessage());
            throw new Exception("No se pudo cargar el documento XML: " + e.getMessage(), e);
        }
    }
    
    /**
     * Realiza la firma digital usando XAdES
     */
    private void performDigitalSignature(XadesSigner signer, Element elementToSign) throws Exception {
        try {
            // Usar Enveloped signature (firma dentro del documento)
            new Enveloped(signer).sign(elementToSign);
            
            log.debug("Firma digital aplicada exitosamente");
            
        } catch (Exception e) {
            log.error("Error aplicando firma digital: {}", e.getMessage());
            throw new Exception("Error en el proceso de firma digital: " + e.getMessage(), e);
        }
    }
    
    /**
     * Guarda el documento firmado
     */
    private void saveSignedDocument(Document signedDocument, String outputPath) throws Exception {
        try {
            // Crear directorio si no existe
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                fileManagerUtil.createDirectoryIfNotExists(parentDir.getAbsolutePath());
            }
            
            // Configurar transformer
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no"); // Hacienda requiere sin indentación
            
            // Transformar y guardar
            DOMSource source = new DOMSource(signedDocument);
            StreamResult result = new StreamResult(outputFile);
            transformer.transform(source, result);
            
            log.debug("Documento firmado guardado: {}", outputPath);
            
        } catch (Exception e) {
            log.error("Error guardando documento firmado: {}", e.getMessage());
            throw new Exception("No se pudo guardar el documento firmado: " + e.getMessage(), e);
        }
    }
    
    /**
     * Crea parámetros de firma con configuración por defecto
     */
    public SignatureParameters createDefaultSignatureParameters(String keyStorePath, String password, 
                                                               String xmlInputPath, String xmlOutputPath) {
        return new SignatureParameters(keyStorePath, password, xmlInputPath, xmlOutputPath);
    }

}