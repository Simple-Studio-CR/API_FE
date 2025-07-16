package app.simplestudio.com.service.adapter;

import app.simplestudio.com.service.IStorageService;
import app.simplestudio.com.util.FileManagerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Adaptador que unifica el acceso a almacenamiento migrando gradualmente de disco local a S3
 * Mantiene la misma interfaz que FileManagerUtil pero usa S3 internamente
 */
@Service
public class StorageAdapter {

    private static final Logger log = LoggerFactory.getLogger(StorageAdapter.class);

    @Autowired
    private IStorageService storageService;

    @Autowired
    private FileManagerUtil fileManagerUtil; // Fallback durante migración

    @Value("${path.upload.files.api}")
    private String pathUploadFilesApi;

    @Value("${migration.use-s3:true}")
    private boolean useS3;

    @Value("${migration.fallback-to-disk:false}")
    private boolean fallbackToDisk;

    // ==================== OPERACIONES PRINCIPALES ====================

    /**
     * Guarda archivo XML (reemplazo directo de FileManagerUtil.saveToFile)
     * @param filePath - Ruta completa tradicional ej: "/home/XmlClientes/123456789/file.xml"
     * @param content - Contenido del archivo
     */
    public void saveToFile(String filePath, String content) throws Exception {
        if (useS3) {
            try {
                String s3Key = convertFilePathToS3Key(filePath);
                storageService.uploadFile(s3Key, content, determineContentType(filePath));
                log.info("Archivo guardado en S3: {} -> {}", filePath, s3Key);
            } catch (IllegalArgumentException e) {
                // Error de validación de path - no intentar fallback
                if (fallbackToDisk) {
                    log.warn("Path inválido para S3, usando disco local: {}", filePath);
                    fileManagerUtil.saveToFile(filePath, content);
                } else {
                    throw e;
                }
            } catch (Exception e) {
                log.error("Error guardando en S3, filePath: {}", filePath, e);
                if (fallbackToDisk) {
                    log.warn("Fallback a disco local para: {}", filePath);
                    fileManagerUtil.saveToFile(filePath, content);
                } else {
                    throw e;
                }
            }
        } else {
            fileManagerUtil.saveToFile(filePath, content);
        }
    }

    /**
     * Lee archivo XML (reemplazo directo de FileManagerUtil.readFromFile)
     * @param filePath - Ruta completa tradicional
     * @return Contenido del archivo
     */
    public String readFromFile(String filePath) throws Exception {
        if (useS3) {
            try {
                String s3Key = convertFilePathToS3Key(filePath);
                String content = storageService.downloadFileAsString(s3Key);
                if (content != null) {
                    log.debug("Archivo leído desde S3: {} -> {}", filePath, s3Key);
                    return content;
                }

                if (fallbackToDisk && fileManagerUtil.fileExists(filePath)) {
                    log.warn("Archivo no encontrado en S3, fallback a disco: {}", filePath);
                    return fileManagerUtil.readFromFile(filePath);
                }

                throw new Exception("Archivo no encontrado: " + filePath);
            } catch (Exception e) {
                log.error("Error leyendo desde S3, filePath: {}", filePath, e);
                if (fallbackToDisk && fileManagerUtil.fileExists(filePath)) {
                    log.warn("Fallback a disco local para lectura: {}", filePath);
                    return fileManagerUtil.readFromFile(filePath);
                } else {
                    throw e;
                }
            }
        } else {
            return fileManagerUtil.readFromFile(filePath);
        }
    }

    /**
     * Verifica si archivo existe (reemplazo directo de FileManagerUtil.fileExists)
     * @param filePath - Ruta completa tradicional
     * @return true si existe
     */
    public boolean fileExists(String filePath) {
        if (useS3) {
            try {
                String s3Key = convertFilePathToS3Key(filePath);
                boolean existsInS3 = storageService.fileExists(s3Key);

                if (!existsInS3 && fallbackToDisk) {
                    boolean existsInDisk = fileManagerUtil.fileExists(filePath);
                    if (existsInDisk) {
                        log.debug("Archivo existe en disco pero no en S3: {}", filePath);
                    }
                    return existsInDisk;
                }

                return existsInS3;
            } catch (Exception e) {
                log.error("Error verificando existencia en S3, filePath: {}", filePath, e);
                if (fallbackToDisk) {
                    return fileManagerUtil.fileExists(filePath);
                }
                return false;
            }
        } else {
            return fileManagerUtil.fileExists(filePath);
        }
    }

    /**
     * Ya no es necesario crear directorios en S3, pero mantenemos compatibilidad
     */
    public void createDirectoryIfNotExists(String directoryPath) {
        if (!useS3) {
            fileManagerUtil.createDirectoryIfNotExists(directoryPath);
        }
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
        if (useS3) {
            try {
                String s3Key = convertFilePathToS3Key(filePath);
                return storageService.downloadFile(s3Key);
            } catch (Exception e) {
                log.error("Error descargando archivo de S3: {}", filePath, e);
                if (fallbackToDisk) {
                    log.warn("downloadFile no soportado en fallback a disco para: {}", filePath);
                    throw new UnsupportedOperationException("downloadFile no soportado en fallback a disco");
                }
                throw e;
            }
        } else {
            throw new UnsupportedOperationException("downloadFile solo disponible con S3");
        }
    }

    /**
     * Obtiene Resource para descargas HTTP (para DescargaXmlController)
     */
    public Resource getXmlAsResource(String emisorId, String xmlFileName) throws Exception {
        if (useS3) {
            String s3Key = storageService.buildKey(emisorId, xmlFileName);
            try (InputStream inputStream = storageService.downloadFile(s3Key)) {
                if (inputStream != null) {
                    // Crear recurso temporal desde S3
                    byte[] content = inputStream.readAllBytes();
                    return new S3Resource(xmlFileName, content);
                }
            } catch (Exception e) {
                log.warn("Error obteniendo archivo de S3: {}", e.getMessage());
            }
        }

        // Fallback a disco local
        String filePath = pathUploadFilesApi + emisorId + "/" + xmlFileName;
        Path path = Paths.get(filePath);
        return new UrlResource(path.toUri());
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
     * Convierte ruta de disco tradicional a S3 key
     * Ej: "/home/XmlClientes/123456789/file.xml" -> "XmlClientes/123456789/xml/signed/file.xml"
     */
    private String convertFilePathToS3Key(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("FilePath no puede ser null o vacío");
        }

        // Normalizar separadores
        String normalizedPath = filePath.replace("\\", "/");

        // Remover path base si está presente
        if (normalizedPath.startsWith(pathUploadFilesApi)) {
            normalizedPath = normalizedPath.substring(pathUploadFilesApi.length());
        }

        // Limpiar slashes iniciales
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        // Separar componentes del path
        String[] parts = normalizedPath.split("/");

        // Filtrar partes vacías
        parts = java.util.Arrays.stream(parts)
            .filter(part -> !part.isEmpty())
            .toArray(String[]::new);

        if (parts.length < 2) {
            throw new IllegalArgumentException("Path inválido, debe tener al menos emisorId/filename: " + filePath);
        }

        String emisorId = parts[0];
        String fileName = parts[parts.length - 1];

        // Determinar subcarpeta basada en el nombre del archivo
        String subfolder = determineSubfolder(fileName);

        return storageService.buildKey(emisorId, fileName);
    }

    /**
     * Determina la subcarpeta S3 basada en el nombre del archivo
     */
    private String determineSubfolder(String fileName) {
        if (fileName.contains("-factura-sign.xml") || fileName.contains("-sign.xml")) {
            return "signed";
        } else if (fileName.contains("-respuesta-mh.xml")) {
            return "responses";
        } else if (fileName.contains("-factura.xml")) {
            return "original";
        } else {
            return "misc";
        }
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