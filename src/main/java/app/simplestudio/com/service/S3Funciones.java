// ==================== Actualizar S3Funciones Existente ====================
    package app.simplestudio.com.service;

import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.storage.S3FileService;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.stereotype.Component;

/**
 * S3Funciones actualizado para usar S3FileService
 * Mantiene compatibilidad con código existente
 */
@Component
public class S3Funciones {

  private final S3FileService s3FileService;

  public S3Funciones(S3FileService s3FileService) {
    this.s3FileService = s3FileService;
  }

  public String getLogoUrl(Emisor emisor) {
    String logoName;
    if (emisor != null && emisor.getLogoEmpresa() != null && !emisor.getLogoEmpresa().trim().isEmpty()) {
      logoName = emisor.getLogoEmpresa();
    } else {
      logoName = "default.png";
    }

    String s3Key = s3FileService.generateLogoKey(logoName);
    return s3FileService.getPublicUrl(s3Key);
  }

  /**
   * Mantiene compatibilidad con DescargaController existente
   */
  public byte[] downloadFileFromS3(String bucket, String key) {
    // El bucket se ignora porque ya está configurado en S3FileService
    return s3FileService.downloadFileAsBytes(key);
  }

  public ByteArrayDataSource downloadFileAsDataSource(String bucket, String key, String mimeType) {
    byte[] fileBytes = downloadFileFromS3(bucket, key);
    return new ByteArrayDataSource(fileBytes, mimeType);
  }
}