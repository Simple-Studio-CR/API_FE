// ==================== S3FileService Simplificado ====================
package app.simplestudio.com.service.storage;

import app.simplestudio.com.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    public S3FileService(
        S3Client s3Client,
        @Value("${cloud.aws.s3.bucket}") String bucketName,
        @Value("${cloud.aws.s3.url}") String s3PublicUrl) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.s3PublicUrl = s3PublicUrl;
        log.info("S3FileService inicializado - bucket: {}, url: {}", bucketName, s3PublicUrl);
    }

    // ==================== SUBIDA DE ARCHIVOS ====================

    /**
     * Sube XML desde contenido String
     */
    public String uploadXmlFromContent(String xmlContent, String s3Key) {
        try {
            log.debug("Subiendo XML a S3: {}", s3Key);

            byte[] contentBytes = xmlContent.getBytes("UTF-8");

            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("application/xml")
                .contentLength((long) contentBytes.length)
                .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(contentBytes));

            log.info("XML subido a S3: {} ({} bytes)", s3Key, contentBytes.length);
            return getPublicUrl(s3Key);

        } catch (Exception e) {
            log.error("Error subiendo XML a S3 {}: {}", s3Key, e.getMessage(), e);
            throw new ValidationException("Error subiendo XML a S3: " + e.getMessage());
        }
    }

    /**
     * Sube archivo desde Path temporal (para XMLs firmados, certificados, etc.)
     */
    public String uploadFileFromPath(Path localPath, String s3Key, String contentType) {
        try {
            log.debug("Subiendo archivo {} a S3: {}", localPath, s3Key);

            if (!Files.exists(localPath)) {
                throw new ValidationException("Archivo temporal no existe: " + localPath);
            }

            byte[] fileBytes = Files.readAllBytes(localPath);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .contentLength((long) fileBytes.length)
                .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(fileBytes));

            log.info("✅ Archivo subido a S3: {} ({} bytes)", s3Key, fileBytes.length);
            return getPublicUrl(s3Key);

        } catch (IOException e) {
            log.error("❌ Error leyendo archivo {}: {}", localPath, e.getMessage(), e);
            throw new ValidationException("Error leyendo archivo temporal: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error subiendo archivo a S3 {}: {}", s3Key, e.getMessage(), e);
            throw new ValidationException("Error subiendo archivo a S3: " + e.getMessage());
        }
    }

    /**
     * Sube archivo desde bytes directamente (más eficiente para archivos en memoria)
     */
    public String uploadFileFromBytes(byte[] fileBytes, String s3Key, String contentType) {
        try {
            log.debug("Subiendo {} bytes a S3: {}", fileBytes.length, s3Key);

            if (fileBytes == null || fileBytes.length == 0) {
                throw new ValidationException("Datos de archivo vacíos");
            }

            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .contentLength((long) fileBytes.length)
                .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(fileBytes));

            log.info("✅ Bytes subidos a S3: {} ({} bytes)", s3Key, fileBytes.length);
            return getPublicUrl(s3Key);

        } catch (Exception e) {
            log.error("❌ Error subiendo bytes a S3 {}: {}", s3Key, e.getMessage(), e);
            throw new ValidationException("Error subiendo datos a S3: " + e.getMessage());
        }
    }

    /**
     * Sube archivo desde MultipartFile (para endpoints de upload)
     */
    public String uploadFileFromMultipart(org.springframework.web.multipart.MultipartFile file, String s3Key) {
        try {
            log.debug("Subiendo MultipartFile a S3: {} -> {}", file.getOriginalFilename(), s3Key);

            if (file.isEmpty()) {
                throw new ValidationException("Archivo vacío");
            }

            String contentType = file.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            byte[] fileBytes = file.getBytes();

            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .contentLength((long) fileBytes.length)
                .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(fileBytes));

            log.info("✅ MultipartFile subido a S3: {} ({} bytes)", s3Key, fileBytes.length);
            return getPublicUrl(s3Key);

        } catch (java.io.IOException e) {
            log.error("❌ Error leyendo MultipartFile: {}", e.getMessage(), e);
            throw new ValidationException("Error procesando archivo subido: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error subiendo MultipartFile a S3 {}: {}", s3Key, e.getMessage(), e);
            throw new ValidationException("Error subiendo archivo a S3: " + e.getMessage());
        }
    }

    // ==================== DESCARGA DE ARCHIVOS ====================

    /**
     * Descarga archivo como bytes
     */
    public byte[] downloadFileAsBytes(String s3Key) {
        try {
            log.debug("Descargando de S3: {}", s3Key);

            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

            byte[] fileBytes = s3Client.getObject(getRequest, ResponseTransformer.toBytes()).asByteArray();
            log.debug("Descargado de S3: {} bytes", fileBytes.length);

            return fileBytes;

        } catch (NoSuchKeyException e) {
            log.warn("Archivo no encontrado en S3: {}", s3Key);
            return null;
        } catch (Exception e) {
            log.error("Error descargando de S3 {}: {}", s3Key, e.getMessage(), e);
            throw new ValidationException("Error descargando de S3: " + e.getMessage());
        }
    }

    /**
     * Descarga archivo como String (para XMLs)
     */
    public String downloadFileAsString(String s3Key) {
        byte[] bytes = downloadFileAsBytes(s3Key);
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            throw new ValidationException("Error procesando archivo: " + e.getMessage());
        }
    }

    /**
     * Descarga archivo a temporal local (para firmado)
     */
    public Path downloadFileToTemp(String s3Key, String tempFileName) {
        try {
            byte[] fileBytes = downloadFileAsBytes(s3Key);
            if (fileBytes == null) {
                return null;
            }

            Path tempPath = Files.createTempFile("s3-temp-", "-" + tempFileName);
            Files.write(tempPath, fileBytes);

            log.debug("Descargado a temporal: {} -> {}", s3Key, tempPath);
            return tempPath;

        } catch (IOException e) {
            log.error("Error creando temporal para {}: {}", s3Key, e.getMessage(), e);
            throw new ValidationException("Error creando temporal: " + e.getMessage());
        }
    }

    // ==================== UTILIDADES ====================

    /**
     * Verifica si archivo existe
     */
    public boolean fileExists(String s3Key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error verificando existencia {}: {}", s3Key, e.getMessage());
            return false;
        }
    }

    /**
     * Limpia archivos temporales
     */
    public void cleanupTempFiles(Path... tempFiles) {
        for (Path tempFile : tempFiles) {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                    log.debug("Temporal eliminado: {}", tempFile);
                } catch (IOException e) {
                    log.warn("No se pudo eliminar temporal {}: {}", tempFile, e.getMessage());
                }
            }
        }
    }

    // ==================== MÉTODOS ESPECÍFICOS DE UPLOAD ====================

    /**
     * Sube XML sin firmar a S3
     */
    public String uploadXmlSinFirmar(String xmlContent, String identificacionEmisor, String clave) {
        String fileName = clave + "-factura.xml";
        String s3Key = generateXmlKey(identificacionEmisor, fileName);
        return uploadXmlFromContent(xmlContent, s3Key);
    }

    /**
     * Sube XML firmado a S3
     */
    public String uploadXmlFirmado(Path xmlFirmadoPath, String identificacionEmisor, String clave) {
        String fileName = clave + "-factura-sign.xml";
        String s3Key = generateXmlKey(identificacionEmisor, fileName);
        return uploadFileFromPath(xmlFirmadoPath, s3Key, "application/xml");
    }

    /**
     * Sube certificado a S3
     */
    public String uploadCertificado(Path certificadoPath, String identificacionEmisor, String nombreCertificado) {
        String s3Key = generateCertificateKey(identificacionEmisor, nombreCertificado);
        return uploadFileFromPath(certificadoPath, s3Key, "application/x-pkcs12");
    }

    /**
     * Sube certificado desde bytes
     */
    public String uploadCertificadoFromBytes(byte[] certificadoBytes, String identificacionEmisor, String nombreCertificado) {
        String s3Key = generateCertificateKey(identificacionEmisor, nombreCertificado);
        return uploadFileFromBytes(certificadoBytes, s3Key, "application/x-pkcs12");
    }

    /**
     * Sube logo a S3
     */
    public String uploadLogo(Path logoPath, String nombreLogo) {
        String s3Key = generateLogoKey(nombreLogo);
        String contentType = determineImageContentType(nombreLogo);
        return uploadFileFromPath(logoPath, s3Key, contentType);
    }

    /**
     * Sube logo desde bytes
     */
    public String uploadLogoFromBytes(byte[] logoBytes, String nombreLogo) {
        String s3Key = generateLogoKey(nombreLogo);
        String contentType = determineImageContentType(nombreLogo);
        return uploadFileFromBytes(logoBytes, s3Key, contentType);
    }

    /**
     * Determina el content type de una imagen por extensión
     */
    private String determineImageContentType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "image/png"; // Default
        }
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida que un archivo temporal existe antes de subirlo
     */
    public void validateTempFile(Path tempFile, String description) {
        if (tempFile == null) {
            throw new ValidationException(description + " es null");
        }
        if (!Files.exists(tempFile)) {
            throw new ValidationException(description + " no existe: " + tempFile);
        }
        if (!Files.isRegularFile(tempFile)) {
            throw new ValidationException(description + " no es un archivo regular: " + tempFile);
        }
        try {
            if (Files.size(tempFile) == 0) {
                throw new ValidationException(description + " está vacío: " + tempFile);
            }
        } catch (IOException e) {
            throw new ValidationException("Error verificando tamaño de " + description + ": " + e.getMessage());
        }
    }

    /**
     * Upload con validación automática
     */
    public String uploadFileFromPathWithValidation(Path localPath, String s3Key, String contentType, String description) {
        validateTempFile(localPath, description);
        return uploadFileFromPath(localPath, s3Key, contentType);
    }

    public String generateXmlKey(String identificacionEmisor, String nombreArchivo) {
        return String.format("XmlClientes/%s/%s", identificacionEmisor, nombreArchivo);
    }

    public String generateCertificateKey(String identificacionEmisor, String nombreCertificado) {
        return String.format("XmlClientes/%s/cert/%s", identificacionEmisor, nombreCertificado);
    }

    public String generateLogoKey(String nombreLogo) {
        return String.format("XmlClientes/logo/%s", nombreLogo);
    }

    public String getPublicUrl(String s3Key) {
        return String.format("%s/%s", s3PublicUrl, s3Key);
    }
}