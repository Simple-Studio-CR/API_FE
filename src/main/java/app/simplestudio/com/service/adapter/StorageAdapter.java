package app.simplestudio.com.service.adapter;

import app.simplestudio.com.service.IStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Adaptador simplificado que usa únicamente S3 para almacenamiento
 * Mantiene exactamente los mismos métodos y firmas del código original
 */
@Service
public class StorageAdapter {

    private static final Logger log = LoggerFactory.getLogger(StorageAdapter.class);

    @Autowired
    private IStorageService storageService;

    // ==================== OPERACIONES PRINCIPALES ====================

    /**
     * Guarda archivo XML (reemplazo directo de FileManagerUtil.saveToFile)
     * @param filePath - Ruta completa tradicional ej: "/home/XmlClientes/123456789/file.xml"
     * @param content - Contenido del archivo
     */
    public void saveToFile(String filePath, String content) throws Exception {
        try {
            FilePathComponents components = parseFilePath(filePath);
            String s3Key = storageService.buildKey(components.emisorId, components.filename);
            storageService.uploadFile(s3Key, content, determineContentType(filePath));
            log.info("Archivo guardado en S3: {} -> {}", filePath, s3Key);
        } catch (Exception e) {
            log.error("Error guardando en S3, filePath: {}", filePath, e);
            throw e;
        }
    }

    /**
     * Lee archivo XML (reemplazo directo de FileManagerUtil.readFromFile)
     * @param filePath - Ruta completa tradicional
     * @return Contenido del archivo
     */
    public String readFromFile(String filePath) throws Exception {
        try {
            FilePathComponents components = parseFilePath(filePath);
            String s3Key = storageService.buildKey(components.emisorId, components.filename);
            String content = storageService.downloadFileAsString(s3Key);
            if (content != null) {
                log.debug("Archivo leído desde S3: {} -> {}", filePath, s3Key);
                return content;
            }
            throw new Exception("Archivo no encontrado: " + filePath);
        } catch (Exception e) {
            log.error("Error leyendo desde S3, filePath: {}", filePath, e);
            throw e;
        }
    }

    /**
     * Verifica si archivo existe (reemplazo directo de FileManagerUtil.fileExists)
     * @param filePath - Ruta completa tradicional
     * @return true si existe
     */
    public boolean fileExists(String filePath) {
        try {
            FilePathComponents components = parseFilePath(filePath);
            String s3Key = storageService.buildKey(components.emisorId, components.filename);
            return storageService.fileExists(s3Key);
        } catch (Exception e) {
            log.error("Error verificando existencia en S3, filePath: {}", filePath, e);
            return false;
        }
    }

    /**
     * Ya no es necesario crear directorios en S3, pero mantenemos compatibilidad
     */
    public void createDirectoryIfNotExists(String directoryPath) {
        // En S3 no necesitamos crear directorios, se crean automáticamente
        log.debug("Directorio auto-creado en S3 (simulado): {}", directoryPath);
    }

    // ==================== MÉTODOS ESPECÍFICOS PARA XML ====================

    /**
     * Guarda XML original antes de firmar
     */
    public void saveXmlFile(String emisorId, String xmlFileName, String xmlContent) throws Exception {
        String s3Key = storageService.buildKey(emisorId, xmlFileName + ".xml");
        storageService.uploadFile(s3Key, xmlContent, "application/xml");
        log.info("XML original guardado: {}", s3Key);
    }

    /**
     * Guarda XML firmado
     */
    public void saveSignedXmlFile(String emisorId, String xmlFileName, String xmlContent) throws Exception {
        String s3Key = storageService.buildKey(emisorId, xmlFileName + ".xml");
        storageService.uploadFile(s3Key, xmlContent, "application/xml");
        log.info("XML firmado guardado: {}", s3Key);
    }

    /**
     * Guarda respuesta de Hacienda
     */
    public void saveResponseXmlFile(String emisorId, String xmlFileName, String xmlContent) throws Exception {
        String s3Key = storageService.buildKey(emisorId, xmlFileName + ".xml");
        storageService.uploadFile(s3Key, xmlContent, "application/xml");
        log.info("Respuesta MH guardada: {}", s3Key);
    }

    /**
     * Lee XML firmado
     */
    public String readSignedXmlFile(String emisorId, String xmlFileName) throws Exception {
        String s3Key = storageService.buildKey(emisorId, xmlFileName + ".xml");
        return storageService.downloadFileAsString(s3Key);
    }

    /**
     * Lee respuesta de Hacienda
     */
    public String readResponseXmlFile(String emisorId, String xmlFileName) throws Exception {
        String s3Key = storageService.buildKey(emisorId, xmlFileName + ".xml");
        return storageService.downloadFileAsString(s3Key);
    }

    /**
     * Verifica si XML firmado existe
     */
    public boolean signedXmlExists(String emisorId, String xmlFileName) {
        String s3Key = storageService.buildKey(emisorId, xmlFileName + ".xml");
        return storageService.fileExists(s3Key);
    }

    /**
     * Verifica si respuesta de Hacienda existe
     */
    public boolean responseXmlExists(String emisorId, String xmlFileName) {
        String s3Key = storageService.buildKey(emisorId, xmlFileName + ".xml");
        return storageService.fileExists(s3Key);
    }

    /**
     * Descarga archivo como InputStream (para attachments de email)
     */
    public InputStream downloadFile(String filePath) throws Exception {
        try {
            FilePathComponents components = parseFilePath(filePath);
            String s3Key = storageService.buildKey(components.emisorId, components.filename);
            return storageService.downloadFile(s3Key);
        } catch (Exception e) {
            log.error("Error descargando archivo de S3: {}", filePath, e);
            throw e;
        }
    }

    /**
     * Obtiene Resource para descargas HTTP (para DescargaXmlController)
     */
    public Resource getXmlAsResource(String emisorId, String xmlFileName) throws Exception {
        String s3Key = storageService.buildKey(emisorId, xmlFileName);
        try (InputStream inputStream = storageService.downloadFile(s3Key)) {
            if (inputStream != null) {
                // Crear recurso temporal desde S3
                byte[] content = inputStream.readAllBytes();
                return new S3Resource(xmlFileName, content);
            }
            throw new Exception("Archivo XML no encontrado en S3: " + xmlFileName);
        } catch (Exception e) {
            log.error("Error obteniendo archivo de S3 - emisor: {}, archivo: {}", emisorId, xmlFileName, e);
            throw e;
        }
    }

    /**
     * Lista archivos XML firmados pendientes de envío
     */
    public List<String> listPendingXmlFiles(String emisorId) {
        String prefix = storageService.buildKey(emisorId, "");
        return storageService.listFiles(prefix);
    }

    // ==================== MÉTODOS DE CONVERSIÓN Y UTILIDADES ====================

    /**
     * Parsea una ruta de disco legacy para extraer emisorId y filename
     */
    private FilePathComponents parseFilePath(String filePath) throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("FilePath no puede ser null o vacío");
        }

        // Normalizar separadores
        String normalizedPath = filePath.replace("\\", "/");

        // Remover slashes iniciales
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        // Separar componentes del path
        // Esperamos algo como: "XmlClientes/123456789/archivo.xml"
        // o "home/XmlClientes/123456789/archivo.xml"
        String[] parts = normalizedPath.split("/");

        // Filtrar partes vacías
        parts = java.util.Arrays.stream(parts)
            .filter(part -> !part.isEmpty())
            .toArray(String[]::new);

        if (parts.length < 2) {
            throw new IllegalArgumentException("Path inválido, debe tener al menos emisorId/filename: " + filePath);
        }

        // Buscar la parte que parece un emisorId (número de 9-12 dígitos)
        String emisorId = null;
        String filename = parts[parts.length - 1]; // último elemento es el filename

        for (String part : parts) {
            if (part.matches("\\d{9,12}")) { // Cédula/identificación
                emisorId = part;
                break;
            }
        }

        if (emisorId == null) {
            throw new IllegalArgumentException("No se encontró emisorId válido en el path: " + filePath);
        }

        return new FilePathComponents(emisorId, filename);
    }

    /**
     * Determina content type basado en extensión
     */
    private String determineContentType(String filePath) {
        if (filePath.toLowerCase().endsWith(".xml")) {
            return "application/xml";
        } else if (filePath.toLowerCase().endsWith(".p12")) {
            return "application/x-pkcs12";
        } else if (filePath.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }

    /**
     * Clase auxiliar para componentes de ruta
     */
    private static class FilePathComponents {
        final String emisorId;
        final String filename;

        FilePathComponents(String emisorId, String filename) {
            this.emisorId = emisorId;
            this.filename = filename;
        }
    }

    // ==================== CLASE AUXILIAR PARA RECURSOS S3 ====================

    /**
     * Resource implementation para contenido de S3
     */
    private static class S3Resource implements Resource {
        private final String filename;
        private final byte[] content;

        public S3Resource(String filename, byte[] content) {
            this.filename = filename;
            this.content = content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public boolean exists() {
            return content != null && content.length > 0;
        }

        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isFile() {
            return false;
        }

        @Override
        public java.net.URL getURL() {
            throw new UnsupportedOperationException("S3Resource does not support URL access");
        }

        @Override
        public java.net.URI getURI() {
            throw new UnsupportedOperationException("S3Resource does not support URI access");
        }

        @Override
        public java.io.File getFile() {
            throw new UnsupportedOperationException("S3Resource does not support File access");
        }

        @Override
        public long contentLength() {
            return content.length;
        }

        @Override
        public long lastModified() {
            return System.currentTimeMillis();
        }

        @Override
        public Resource createRelative(String relativePath) {
            throw new UnsupportedOperationException("S3Resource does not support relative resources");
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public String getDescription() {
            return "S3 Resource: " + filename;
        }
    }
}