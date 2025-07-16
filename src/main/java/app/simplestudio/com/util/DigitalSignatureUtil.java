package app.simplestudio.com.util;

import app.simplestudio.com.service.storage.S3FileService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
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
    private S3FileService s3FileService;

    /**
     * Resultado de la operación de firma - MANTIENE LA MISMA INTERFAZ
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

        // Getters IGUALES
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getSignedXmlPath() { return signedXmlPath; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public Exception getException() { return exception; }
    }

    /**
     * Parámetros completos para la firma digital - MANTIENE LA MISMA INTERFAZ
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

        // Getters IGUALES
        public String getKeyStorePath() { return keyStorePath; }
        public String getPassword() { return password; }
        public String getXmlInputPath() { return xmlInputPath; }
        public String getXmlOutputPath() { return xmlOutputPath; }
        public XadesSignatureUtil.XadesConfig getXadesConfig() { return xadesConfig; }
        public CertificateManagerUtil.CertificateConfig getCertificateConfig() { return certificateConfig; }

        // Setters IGUALES
        public void setXadesConfig(XadesSignatureUtil.XadesConfig xadesConfig) { this.xadesConfig = xadesConfig; }
        public void setCertificateConfig(CertificateManagerUtil.CertificateConfig certificateConfig) { this.certificateConfig = certificateConfig; }
    }

    /**
     * MÉTODO PRINCIPAL - SOLO S3, NO DISCO LOCAL
     */
    public SignatureResult signXmlDocument(SignatureParameters params) {
        long startTime = System.currentTimeMillis();

        try {
            // Validar parámetros
            validateSignatureParameters(params);

            // SOLO S3 - Si no es S3, fallar
            if (!isS3Path(params.getXmlInputPath())) {
                throw new Exception("Solo se admiten rutas S3. Ruta no válida: " + params.getXmlInputPath());
            }

            return signXmlDocumentFromS3(params, startTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Error firmando documento: {}", e.getMessage(), e);
            return new SignatureResult(false, "Error firmando documento: " + e.getMessage(), null, executionTime, e);
        }
    }

    /**
     * NUEVO: Firmar desde S3 (internamente)
     */
    private SignatureResult signXmlDocumentFromS3(SignatureParameters params, long startTime) throws Exception {
        log.debug("Firmando documento desde S3: {}", params.getXmlInputPath());

        // 1. Descargar XML desde S3
        String xmlContent = downloadXmlFromS3(params.getXmlInputPath());

        // 2. Crear documento en memoria
        Document xmlDocument = parseXmlFromString(xmlContent);

        // 3. Crear recursos para firma
        KeyingDataProvider keyingProvider = certificateManagerUtil.createKeyingDataProvider(params.getCertificateConfig());
        XadesSigner signer = xadesSignatureUtil.createXadesSigner(keyingProvider, params.getXadesConfig());

        // 4. Realizar firma
        Element elementToSign = xmlDocument.getDocumentElement();
        performDigitalSignature(signer, elementToSign);

        // 5. Convertir documento firmado a string
        String signedXmlContent = documentToString(xmlDocument);

        // 6. Subir documento firmado a S3
        uploadSignedXmlToS3(params.getXmlOutputPath(), signedXmlContent);

        long executionTime = System.currentTimeMillis() - startTime;

        log.info("Documento firmado exitosamente desde S3 en {} ms: {}", executionTime, params.getXmlOutputPath());
        return new SignatureResult(true, "Documento firmado con éxito", params.getXmlOutputPath(), executionTime);
    }

    /**
     * ORIGINAL: Firmar desde archivo local (por compatibilidad)
     */
    private SignatureResult signXmlDocumentFromFile(SignatureParameters params, long startTime) throws Exception {
        log.debug("Firmando documento desde archivo local: {}", params.getXmlInputPath());

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

        log.info("Documento firmado exitosamente desde archivo en {} ms: {}", executionTime, params.getXmlOutputPath());
        return new SignatureResult(true, "Documento firmado con éxito", params.getXmlOutputPath(), executionTime);
    }

    /**
     * HELPER: Detecta si la ruta es S3 - SOLO ACEPTA S3
     */
    private boolean isS3Path(String path) {
        // SOLO aceptar rutas S3 explícitas
        return path != null && (
            path.startsWith("XmlClientes/") ||
                path.matches(".*\\d+/.*\\.xml$") // patrón: números/archivo.xml
        );
    }

    /**
     * HELPER: Descarga XML desde S3
     */
    private String downloadXmlFromS3(String s3Key) throws Exception {
        try {
            String xmlContent = s3FileService.downloadFileAsString(s3Key);
            if (xmlContent == null || xmlContent.trim().isEmpty()) {
                throw new Exception("XML vacío o no encontrado en S3: " + s3Key);
            }
            log.debug("XML descargado desde S3: {} caracteres", xmlContent.length());
            return xmlContent;
        } catch (Exception e) {
            log.error("Error descargando XML desde S3: {}", e.getMessage());
            throw new Exception("No se pudo descargar XML desde S3: " + e.getMessage(), e);
        }
    }

    /**
     * HELPER: Parsea XML desde string en memoria
     */
    private Document parseXmlFromString(String xmlContent) throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // Crítico para XAdES

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

            log.debug("XML parseado exitosamente desde string");
            return document;

        } catch (Exception e) {
            log.error("Error parseando XML desde string: {}", e.getMessage());
            throw new Exception("No se pudo parsear el XML: " + e.getMessage(), e);
        }
    }

    /**
     * HELPER: Convierte documento a string
     */
    private String documentToString(Document document) throws Exception {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no"); // Hacienda requiere sin indentación
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

            DOMSource source = new DOMSource(document);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(outputStream);

            transformer.transform(source, result);

            String xmlString = outputStream.toString(StandardCharsets.UTF_8.name());
            log.debug("Documento convertido a string: {} caracteres", xmlString.length());

            return xmlString;

        } catch (Exception e) {
            log.error("Error convirtiendo documento a string: {}", e.getMessage());
            throw new Exception("No se pudo serializar el documento: " + e.getMessage(), e);
        }
    }

    /**
     * HELPER: Sube XML firmado a S3
     */
    private void uploadSignedXmlToS3(String s3Key, String xmlContent) throws Exception {
        try {
            s3FileService.uploadFile(s3Key, xmlContent, "application/xml");
            log.debug("XML firmado subido a S3: {}", s3Key);
        } catch (Exception e) {
            log.error("Error subiendo XML firmado a S3: {}", e.getMessage());
            throw new Exception("No se pudo subir XML firmado a S3: " + e.getMessage(), e);
        }
    }

    /**
     * MÉTODO VALIDACIÓN - SOLO S3
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

        // SOLO S3 - Validar que sean rutas S3
        if (!isS3Path(params.getXmlInputPath())) {
            throw new Exception("Solo se admiten rutas S3. Ruta de entrada no válida: " + params.getXmlInputPath());
        }

        if (!isS3Path(params.getXmlOutputPath())) {
            throw new Exception("Solo se admiten rutas S3. Ruta de salida no válida: " + params.getXmlOutputPath());
        }

        // Validar que el archivo XML existe en S3
        if (!s3FileService.fileExists(params.getXmlInputPath())) {
            throw new Exception("El archivo XML no existe en S3: " + params.getXmlInputPath());
        }

        // Validar configuración del certificado
        certificateManagerUtil.validateCertificateConfig(params.getCertificateConfig());

        // Validar configuración XAdES
        if (!xadesSignatureUtil.validateXadesConfig(params.getXadesConfig())) {
            throw new Exception("Configuración XAdES inválida");
        }

        log.debug("Parámetros de firma S3 validados exitosamente");
    }

    /**
     * MÉTODO ORIGINAL MANTENIDO - Carga el documento XML para firma (archivos locales)
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
     * MÉTODO ORIGINAL MANTENIDO - Realiza la firma digital usando XAdES
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
     * MÉTODO ORIGINAL MANTENIDO - Guarda el documento firmado (archivos locales)
     */
    private void saveSignedDocument(Document signedDocument, String outputPath) throws Exception {
        try {
            // Crear directorio si no existe
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Configurar transformer
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no"); // Hacienda requiere sin indentación

            // Transformar y guardar
            DOMSource source = new DOMSource(signedDocument);
            StreamResult result = new StreamResult(String.valueOf(outputFile));
            transformer.transform(source, result);

            log.debug("Documento firmado guardado: {}", outputPath);

        } catch (Exception e) {
            log.error("Error guardando documento firmado: {}", e.getMessage());
            throw new Exception("No se pudo guardar el documento firmado: " + e.getMessage(), e);
        }
    }

    /**
     * MÉTODO ORIGINAL MANTENIDO - Crea parámetros de firma con configuración por defecto
     */
    public SignatureParameters createDefaultSignatureParameters(String keyStorePath, String password,
        String xmlInputPath, String xmlOutputPath) {
        return new SignatureParameters(keyStorePath, password, xmlInputPath, xmlOutputPath);
    }
}