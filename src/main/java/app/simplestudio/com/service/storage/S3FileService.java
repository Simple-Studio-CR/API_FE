// ==================== S3FileService Simplificado ====================
package app.simplestudio.com.service.storage;

import app.simplestudio.com.exception.ValidationException;
import app.simplestudio.com.models.entity.Emisor;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.ResponseTransformer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

/**
 * Servicio S3 - Reemplaza completamente el almacenamiento local
 */
@Service
public class S3FileService {

    private static final Logger log = LoggerFactory.getLogger(S3FileService.class);

    private final S3Client s3Client;
    private final String bucketName;
    private final String s3PublicUrl;
    private final String s3Prefix;

    public S3FileService(
        S3Client s3Client,
        @Value("${cloud.aws.s3.bucket}") String bucketName,
        @Value("${cloud.aws.s3.url}") String s3PublicUrl,
        @Value("${cloud.aws.s3.prefix}") String s3Prefix) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.s3PublicUrl = s3PublicUrl;
        this.s3Prefix = s3Prefix;
        log.info("S3FileService inicializado - bucket: {}, url: {}", bucketName, s3PublicUrl);
    }

    // ==================== SUBIDA DE ARCHIVOS ====================

    // ==================== OPERACIONES BÁSICAS S3 ====================

    /**
     * Sube archivo a S3 desde contenido String
     */
    public String uploadFile(String key, String content, String contentType) {
        try {
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength((long) contentBytes.length)
                .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(contentBytes));
            log.info("Archivo subido a S3: {} ({} bytes)", key, contentBytes.length);
            return buildUrl(key);

        } catch (Exception e) {
            log.error("Error subiendo archivo a S3: {}", key, e);
            throw new RuntimeException("Error subiendo archivo a S3: " + e.getMessage(), e);
        }
    }

    /**
     * Sube archivo a S3 desde InputStream
     */
    public String uploadFile(String key, InputStream inputStream, String contentType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));
            log.info("Archivo subido a S3: {}", key);
            return buildUrl(key);

        } catch (Exception e) {
            log.error("Error subiendo archivo a S3: {}", key, e);
            throw new RuntimeException("Error subiendo archivo a S3: " + e.getMessage(), e);
        }
    }

    /**
     * Descarga archivo de S3 como InputStream
     */
    public InputStream downloadFile(String key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getRequest);
            log.debug("Archivo descargado de S3: {}", key);
            return s3Object;

        } catch (NoSuchKeyException e) {
            log.warn("Archivo no encontrado en S3: {}", key);
            return null;
        } catch (Exception e) {
            log.error("Error descargando archivo de S3: {}", key, e);
            throw new RuntimeException("Error descargando archivo de S3: " + e.getMessage(), e);
        }
    }
    /**
     * Obtiene Resource para descargas HTTP (para DescargaXmlController)
     */
    public Resource getXmlAsResource(String emisorId, String xmlFileName) throws Exception {
        String s3Key = buildKey(emisorId, xmlFileName);
        try (InputStream inputStream = downloadFile(s3Key)) {
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
     * Descarga archivo de S3 como String
     */
    public String downloadFileAsString(String key) {
        try (InputStream inputStream = downloadFile(key)) {
            if (inputStream == null) {
                return null;
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error leyendo archivo como string: {}", key, e);
            throw new RuntimeException("Error leyendo archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Descarga archivo de S3 como byte array
     */
    public byte[] downloadFileAsBytes(String key) {
        try (InputStream inputStream = downloadFile(key)) {
            if (inputStream == null) {
                return null;
            }
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("Error leyendo archivo como bytes: {}", key, e);
            throw new RuntimeException("Error leyendo archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si archivo existe en S3
     */
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error verificando existencia en S3: {}", key, e);
            return false;
        }
    }

    /**
     * Elimina archivo de S3
     */
    public boolean deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Archivo eliminado de S3: {}", key);
            return true;

        } catch (Exception e) {
            log.error("Error eliminando archivo de S3: {}", key, e);
            return false;
        }
    }

    /**
     * Lista archivos en S3 con prefijo
     */
    public List<String> listFiles(String prefix) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);
            return response.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error listando archivos en S3: {}", prefix, e);
            throw new RuntimeException("Error listando archivos en S3: " + e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS ESPECÍFICOS PARA XML ====================

    /**
     * Construye la key S3 para un emisor y archivo
     */
    public String buildKey(String emisorId, String filename) {
        if (emisorId == null || emisorId.trim().isEmpty()) {
            throw new IllegalArgumentException("EmisorId no puede ser null o vacío");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename no puede ser null o vacío");
        }

        // Estructura: XmlClientes/{emisorId}/{filename}
        return s3Prefix + "/" + emisorId.trim() + "/" + filename.trim();
    }

    /**
     * Guarda XML firmado
     */
    public void saveSignedXmlFile(String emisorId, String xmlFileName, String xmlContent) {
        String s3Key = buildKey(emisorId, xmlFileName);
        uploadFile(s3Key, xmlContent, "application/xml");
        log.info("XML firmado guardado: {}", s3Key);
    }

    /**
     * Guarda respuesta de Hacienda
     */
    public void saveResponseXmlFile(String emisorId, String xmlFileName, String xmlContent) {
        String s3Key = buildKey(emisorId, xmlFileName);
        uploadFile(s3Key, xmlContent, "application/xml");
        log.info("Respuesta MH guardada: {}", s3Key);
    }

    /**
     * Lee XML firmado
     */
    public String readSignedXmlFile(String emisorId, String xmlFileName) {
        String s3Key = buildKey(emisorId, xmlFileName);
        return downloadFileAsString(s3Key);
    }

    /**
     * Lee respuesta de Hacienda
     */
    public String readResponseXmlFile(String emisorId, String xmlFileName) {
        String s3Key = buildKey(emisorId, xmlFileName);
        return downloadFileAsString(s3Key);
    }

    /**
     * Verifica si XML firmado existe
     */
    public boolean signedXmlExists(String emisorId, String xmlFileName) {
        String s3Key = buildKey(emisorId, xmlFileName);
        return fileExists(s3Key);
    }

    /**
     * Verifica si respuesta de Hacienda existe
     */
    public boolean responseXmlExists(String emisorId, String xmlFileName) {
        String s3Key = buildKey(emisorId, xmlFileName);
        return fileExists(s3Key);
    }

    // ==================== MÉTODOS PARA LOGOS ====================

    /**
     * Obtiene URL del logo para un emisor
     */
    public String getLogoUrl(Emisor emisor) {
        String logoName;
        if (emisor != null && emisor.getLogoEmpresa() != null && !emisor.getLogoEmpresa().trim().isEmpty()) {
            logoName = emisor.getLogoEmpresa();
        } else {
            logoName = "default.png";
        }

        String s3Key = buildKey("logo", logoName);
        return buildUrl(s3Key);
    }

    /**
     * Obtiene logo como Resource para descarga HTTP
     */
    public Resource getLogoAsResource(String logoFileName) {
        String s3Key = buildKey("logo", logoFileName);
        try (InputStream inputStream = downloadFile(s3Key)) {
            if (inputStream != null) {
                byte[] content = inputStream.readAllBytes();
                return new S3Resource(logoFileName, content);
            }
            return null;
        } catch (Exception e) {
            log.error("Error obteniendo logo de S3: {}", logoFileName, e);
            return null;
        }
    }

    /**
     * Verifica si logo existe
     */
    public boolean logoExists(String logoFileName) {
        String s3Key = buildKey("logo", logoFileName);
        return fileExists(s3Key);
    }

    // ==================== MÉTODOS PARA COMPATIBILIDAD ====================

    /**
     * Método legacy para compatibilidad con DescargaController existente
     */
    public byte[] downloadFileFromS3(String bucket, String key) {
        // El bucket se ignora porque ya está configurado
        return downloadFileAsBytes(key);
    }

    /**
     * Para compatibilidad con email attachments
     */
    public ByteArrayDataSource downloadFileAsDataSource(String bucket, String key, String mimeType) {
        byte[] fileBytes = downloadFileFromS3(bucket, key);
        if (fileBytes == null) {
            return null;
        }
        return new ByteArrayDataSource(fileBytes, mimeType);
    }

    /**
     * Método para compatibilidad con rutas legacy de disco
     */
    public void saveToFile(String filePath, String content) {
        FilePathComponents components = parseFilePath(filePath);
        String s3Key = buildKey(components.emisorId, components.filename);
        uploadFile(s3Key, content, determineContentType(filePath));
        log.info("Archivo legacy guardado en S3: {} -> {}", filePath, s3Key);
    }

    /**
     * Método para compatibilidad con rutas legacy de disco
     */
    public String readFromFile(String filePath) {
        FilePathComponents components = parseFilePath(filePath);
        String s3Key = buildKey(components.emisorId, components.filename);
        return downloadFileAsString(s3Key);
    }

    /**
     * Método para compatibilidad con rutas legacy de disco
     */
    public boolean fileExistsLegacy(String filePath) {
        try {
            FilePathComponents components = parseFilePath(filePath);
            String s3Key = buildKey(components.emisorId, components.filename);
            return fileExists(s3Key);
        } catch (Exception e) {
            log.error("Error verificando existencia legacy: {}", filePath, e);
            return false;
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Construye URL pública del archivo
     */
    public String buildUrl(String key) {
        return s3PublicUrl + "/" + key;
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
     * Parsea ruta legacy para extraer emisorId y filename
     */
    private FilePathComponents parseFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("FilePath no puede ser null o vacío");
        }

        String normalizedPath = filePath.replace("\\", "/");
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        String[] parts = normalizedPath.split("/");
        parts = java.util.Arrays.stream(parts)
            .filter(part -> !part.isEmpty())
            .toArray(String[]::new);

        if (parts.length < 2) {
            throw new IllegalArgumentException("Path inválido: " + filePath);
        }

        String emisorId = null;
        String filename = parts[parts.length - 1];

        for (String part : parts) {
            if (part.matches("\\d{9,12}") || "logo".equals(part)) {
                emisorId = part;
                break;
            }
        }

        if (emisorId == null) {
            throw new IllegalArgumentException("No se encontró emisorId válido: " + filePath);
        }

        return new FilePathComponents(emisorId, filename);
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