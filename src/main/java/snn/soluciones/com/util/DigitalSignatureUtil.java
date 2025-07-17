package snn.soluciones.com.util;

import snn.soluciones.com.service.storage.S3FileService;
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
     * [Sin cambios en la interfaz]
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

            // Procesar firma con S3
            return signXmlDocumentFromS3(params, startTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Error firmando documento: {}", e.getMessage(), e);
            return new SignatureResult(false, "Error firmando documento: " + e.getMessage(), null, executionTime, e);
        }
    }

    /**
     * NUEVO: Firmar desde S3 (internamente)
     * [Sin cambios en la interfaz]
     */
    private SignatureResult signXmlDocumentFromS3(SignatureParameters params, long startTime) throws Exception {
        log.debug("Firmando documento desde S3: {}", params.getXmlInputPath());

        // 1. Descargar XML desde S3
        String xmlContent = downloadXmlFromS3(params.getXmlInputPath());

        // 2. Parsear XML en memoria
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
     * HELPER: Parsea XML desde string en memoria
     * MEJORADO: Incluye limpieza y validación del XML antes de parsearlo
     *
     * Mantiene exactamente la misma firma: Document parseXmlFromString(String xmlContent) throws Exception
     */
    private Document parseXmlFromString(String xmlContent) throws Exception {
        try {
            // NUEVO: Limpiar y validar el XML antes de parsearlo
            xmlContent = cleanAndPrepareXml(xmlContent);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // Crítico para XAdES

            // NUEVO: Configuración adicional para prevenir problemas
            factory.setIgnoringElementContentWhitespace(true);
            factory.setIgnoringComments(true);
            factory.setCoalescing(true); // Combina nodos de texto adyacentes

            // Configuración de seguridad
            try {
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (Exception e) {
                log.warn("No se pudieron configurar algunas características de seguridad: {}", e.getMessage());
            }

            DocumentBuilder builder = factory.newDocumentBuilder();

            // NUEVO: Error handler para mejor debugging
            String finalXmlContent = xmlContent;
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                @Override
                public void warning(org.xml.sax.SAXParseException e) {
                    log.warn("Advertencia XML en línea {}, columna {}: {}",
                        e.getLineNumber(), e.getColumnNumber(), e.getMessage());
                }

                @Override
                public void error(org.xml.sax.SAXParseException e) {
                    log.error("Error XML en línea {}, columna {}: {}",
                        e.getLineNumber(), e.getColumnNumber(), e.getMessage());
                }

                @Override
                public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
                    log.error("Error fatal XML en línea {}, columna {}: {}",
                        e.getLineNumber(), e.getColumnNumber(), e.getMessage());

                    // Log de contexto para debugging
                    try {
                        String[] lines = finalXmlContent.split("\n");
                        if (e.getLineNumber() > 0 && e.getLineNumber() <= lines.length) {
                            log.error("Línea problemática: {}", lines[e.getLineNumber() - 1]);
                        }
                    } catch (Exception logEx) {
                        // Ignorar errores de logging
                    }

                    throw e;
                }
            });

            Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
            document.getDocumentElement().normalize();

            log.debug("XML parseado exitosamente desde string. Elemento raíz: {}",
                document.getDocumentElement().getNodeName());
            return document;

        } catch (org.xml.sax.SAXParseException e) {
            log.error("Error de parseo XML en línea {}, columna {}: {}",
                e.getLineNumber(), e.getColumnNumber(), e.getMessage());
            throw new Exception("No se pudo parsear el XML: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error parseando XML desde string: {}", e.getMessage());
            throw new Exception("No se pudo parsear el XML: " + e.getMessage(), e);
        }
    }

    /**
     * NUEVO MÉTODO INTERNO: Limpia y prepara el XML para evitar errores de parseo
     * Este método es completamente interno y no afecta ninguna interfaz pública
     */
    private String cleanAndPrepareXml(String xmlContent) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            throw new IllegalArgumentException("El contenido XML no puede ser nulo o vacío");
        }

        // 1. Eliminar BOM (Byte Order Mark) si existe
        if (xmlContent.startsWith("\uFEFF")) {
            xmlContent = xmlContent.substring(1);
            log.debug("BOM removido del XML");
        }

        // 2. Eliminar espacios en blanco al inicio y final
        xmlContent = xmlContent.trim();

        // 3. Verificar que empiece con declaración XML o elemento raíz
        if (!xmlContent.startsWith("<?xml") && !xmlContent.startsWith("<")) {
            throw new IllegalArgumentException("El XML no empieza con una declaración válida");
        }

        // 4. Detectar y limpiar contenido después del elemento raíz
        int lastValidTagEnd = findLastValidTagEnd(xmlContent);
        if (lastValidTagEnd > 0 && lastValidTagEnd < xmlContent.length()) {
            // Solo hay contenido extra si hay caracteres no-blancos después del cierre
            String afterContent = xmlContent.substring(lastValidTagEnd);
            String trimmedAfter = afterContent.trim();

            if (!trimmedAfter.isEmpty()) {
                // Hay contenido no-blanco después del cierre del elemento raíz
                log.warn("Se encontró contenido después del elemento raíz: '{}'. Se eliminará.",
                    trimmedAfter.length() > 100 ? trimmedAfter.substring(0, 100) + "..." : trimmedAfter);
                xmlContent = xmlContent.substring(0, lastValidTagEnd);
            }
        } else if (lastValidTagEnd == -1) {
            // No se encontró el cierre del elemento raíz - el XML está mal formado
            log.error("No se pudo encontrar el cierre del elemento raíz en el XML");
            // No modificar el XML, dejar que el parser lance el error apropiado
        }

        // 5. Verificar múltiples declaraciones XML
        int firstDecl = xmlContent.indexOf("<?xml");
        if (firstDecl != -1) {
            int secondDecl = xmlContent.indexOf("<?xml", firstDecl + 1);
            if (secondDecl != -1) {
                log.error("Se detectaron múltiples declaraciones XML en posiciones {} y {}", firstDecl, secondDecl);
                // Esto es un error grave - no intentar arreglarlo
                throw new IllegalArgumentException("El XML contiene múltiples declaraciones XML");
            }
        }

        // 6. Normalizar saltos de línea
        xmlContent = xmlContent.replaceAll("\r\n", "\n").replaceAll("\r", "\n");

        // 7. Log para debugging (solo en modo debug)
        if (log.isDebugEnabled()) {
            log.debug("XML después de limpieza - Longitud: {} caracteres", xmlContent.length());
            log.debug("Primeros 300 caracteres: {}",
                xmlContent.length() > 300 ? xmlContent.substring(0, 300) + "..." : xmlContent);
            log.debug("Últimos 300 caracteres: {}",
                xmlContent.length() > 300 ?
                    "..." + xmlContent.substring(xmlContent.length() - 300) : xmlContent);
        }

        return xmlContent;
    }

    /**
     * NUEVO MÉTODO AUXILIAR: Encuentra el final del último tag válido
     * Busca el elemento raíz y su cierre correspondiente
     */
    private int findLastValidTagEnd(String xmlContent) {
        try {
            // Buscar donde empieza el elemento raíz (saltando la declaración XML)
            int rootElementStart = -1;

            // Si hay declaración XML, buscar después de ella
            if (xmlContent.startsWith("<?xml")) {
                int xmlDeclEnd = xmlContent.indexOf("?>");
                if (xmlDeclEnd != -1) {
                    // Buscar el primer < después de la declaración XML
                    rootElementStart = xmlContent.indexOf("<", xmlDeclEnd + 2);
                }
            } else {
                // Si no hay declaración XML, el primer < es el elemento raíz
                rootElementStart = xmlContent.indexOf("<");
            }

            if (rootElementStart == -1) {
                return -1;
            }

            // Extraer el nombre del elemento raíz
            int nameStart = rootElementStart + 1;
            int nameEnd = nameStart;

            // Buscar el final del nombre del elemento (espacio, > o /)
            while (nameEnd < xmlContent.length()) {
                char ch = xmlContent.charAt(nameEnd);
                if (ch == ' ' || ch == '>' || ch == '/' || ch == '\n' || ch == '\r' || ch == '\t') {
                    break;
                }
                nameEnd++;
            }

            String rootElementName = xmlContent.substring(nameStart, nameEnd);

            // Buscar el cierre del elemento raíz
            String closeTag = "</" + rootElementName + ">";
            int closeTagIndex = xmlContent.lastIndexOf(closeTag);

            if (closeTagIndex != -1) {
                // Retornar la posición después del cierre del tag
                return closeTagIndex + closeTag.length();
            }

            // Si no se encuentra el cierre, algo está mal con el XML
            log.error("No se encontró el cierre del elemento raíz: {}", rootElementName);
            return -1;

        } catch (Exception e) {
            log.warn("Error buscando el final del elemento raíz: {}", e.getMessage());
            return -1;
        }
    }

    // ... [El resto de los métodos se mantienen exactamente igual]

    /**
     * HELPER: Descarga XML desde S3
     * [Sin cambios]
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
     * HELPER: Convierte documento a string
     * [Sin cambios]
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
            throw new Exception("No se pudo serializar el documento XML: " + e.getMessage(), e);
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

    /**
     * HELPER: Detecta si la ruta es S3 - SOLO ACEPTA S3
     * Basado en el patrón de rutas que usa tu aplicación
     */
    boolean isS3Path(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        // Solo aceptar rutas S3 del patrón usado en la aplicación
        return path.startsWith("XmlClientes/") ||
            path.startsWith("certificados/") ||
            path.contains(".digitaloceanspaces.com");
    }
}