// ==================== Servicio de Upload Especializado ====================
package app.simplestudio.com.service.upload;

import app.simplestudio.com.service.storage.S3FileService;
import app.simplestudio.com.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio especializado para uploads de archivos específicos
 */
@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final S3FileService s3FileService;

    public FileUploadService(S3FileService s3FileService) {
        this.s3FileService = s3FileService;
    }

    /**
     * Sube un certificado con validaciones específicas
     */
    public String uploadCertificate(MultipartFile file, String identificacionEmisor) {
        try {
            validateCertificateFile(file);

            // Generar nombre único con timestamp
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = String.format("cert_%s_%s%s", identificacionEmisor, timestamp, extension);

            String s3Key = s3FileService.generateCertificateKey(identificacionEmisor, fileName);
            return s3FileService.uploadFileFromMultipart(file, s3Key);

        } catch (Exception e) {
            log.error("Error subiendo certificado para {}: {}", identificacionEmisor, e.getMessage(), e);
            throw new ValidationException("Error subiendo certificado: " + e.getMessage());
        }
    }

    /**
     * Sube un logo con validaciones específicas
     */
    public String uploadLogo(MultipartFile file, String nombreLogo) {
        try {
            validateImageFile(file);

            // Asegurar extensión correcta
            if (!nombreLogo.matches(".*\\.(png|jpg|jpeg|gif)$")) {
                String extension = getFileExtension(file.getOriginalFilename());
                nombreLogo += extension;
            }

            String s3Key = s3FileService.generateLogoKey(nombreLogo);
            return s3FileService.uploadFileFromMultipart(file, s3Key);

        } catch (Exception e) {
            log.error("Error subiendo logo {}: {}", nombreLogo, e.getMessage(), e);
            throw new ValidationException("Error subiendo logo: " + e.getMessage());
        }
    }

    /**
     * Sube certificado desde archivo temporal (para procesos internos)
     */
    public String uploadCertificateFromPath(Path certificatePath, String identificacionEmisor, String nombreOriginal) {
        String s3Key = s3FileService.generateCertificateKey(identificacionEmisor, nombreOriginal);
        return s3FileService.uploadFileFromPathWithValidation(
            certificatePath,
            s3Key,
            "application/x-pkcs12",
            "Certificado"
        );
    }

    // ==================== VALIDACIONES PRIVADAS ====================

    private void validateCertificateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Archivo de certificado requerido");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".p12") && !fileName.toLowerCase().endsWith(".pfx"))) {
            throw new ValidationException("El certificado debe ser un archivo .p12 o .pfx");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ValidationException("El certificado no puede ser mayor a 10MB");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Archivo de imagen requerido");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().matches(".*\\.(png|jpg|jpeg|gif)$")) {
            throw new ValidationException("El logo debe ser una imagen PNG, JPG, JPEG o GIF");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ValidationException("La imagen no puede ser mayor a 5MB");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".p12";
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : ".p12";
    }
}