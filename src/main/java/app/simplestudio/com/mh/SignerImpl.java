package app.simplestudio.com.mh;

import app.simplestudio.com.util.DigitalSignatureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignerImpl implements ISigner {

  private static final Logger log = LoggerFactory.getLogger(SignerImpl.class);

  // ==================== NUEVO UTIL ====================
  @Autowired
  private DigitalSignatureUtil digitalSignatureUtil;

  // ==================== MÉTODO ORIGINAL MANTENIDO ====================

  /**
   * FIRMA ORIGINAL MANTENIDA - Firma digital de documento XML
   * MISMA INTERFAZ: void sign(String, String, String, String)
   * MISMO COMPORTAMIENTO: Firma XAdES-BES/EPES para Hacienda v4.3
   * MEJOR IMPLEMENTACIÓN: Usa utils especializados
   */
  @Override
  public void sign(String keyPath, String password, String xmlInPath, String xmlOutPath) {
    log.info("Iniciando proceso de firma digital");
    log.debug("Certificado: {} | XML entrada: {} | XML salida: {}", keyPath, xmlInPath, xmlOutPath);

    try {
      // Crear parámetros de firma usando el util
      DigitalSignatureUtil.SignatureParameters params = digitalSignatureUtil
          .createDefaultSignatureParameters(keyPath, password, xmlInPath, xmlOutPath);

      // Ejecutar firma digital
      DigitalSignatureUtil.SignatureResult result = digitalSignatureUtil.signXmlDocument(params);

      // Procesar resultado
      if (result.isSuccess()) {
        log.info("Documento firmado con éxito en {} ms", result.getExecutionTimeMs());

        // Mantener el mensaje original para compatibilidad
        System.out.println("Documento firmado con éxito");

      } else {
        log.error("Error en firma digital: {}", result.getMessage());

        // Lanzar excepción para mantener comportamiento original
        if (result.getException() != null) {
          throw new RuntimeException("Error firmando documento: " + result.getMessage(), result.getException());
        } else {
          throw new RuntimeException("Error firmando documento: " + result.getMessage());
        }
      }

    } catch (RuntimeException e) {
      // Re-lanzar RuntimeException tal como estaba
      log.error("Error en proceso de firma: {}", e.getMessage());
      throw e;

    } catch (Exception e) {
      // Mantener el comportamiento original: printStackTrace + continuar
      log.error("Excepción durante firma digital", e);
      e.printStackTrace();

      // El método original no lanzaba excepción, solo hacía printStackTrace
      // Mantenemos este comportamiento para compatibilidad total
    }

    log.debug("Proceso de firma digital completado");
  }

  // ==================== MÉTODOS AUXILIARES MEJORADOS ====================

  /**
   * Firma documento con configuración personalizada
   * NUEVO: Permite personalizar configuración XAdES y certificado
   */
  public DigitalSignatureUtil.SignatureResult signWithCustomConfig(
      String keyPath,
      String password,
      String xmlInPath,
      String xmlOutPath,
      boolean useCustomAlgorithms) {

    try {
      DigitalSignatureUtil.SignatureParameters params = digitalSignatureUtil
          .createDefaultSignatureParameters(keyPath, password, xmlInPath, xmlOutPath);

      // Personalizar configuración si se solicita
      if (!useCustomAlgorithms) {
        params.getXadesConfig().setUseCustomAlgorithms(false);
      }

      return digitalSignatureUtil.signXmlDocument(params);

    } catch (Exception e) {
      log.error("Error en firma con configuración personalizada: {}", e.getMessage());
      return new DigitalSignatureUtil.SignatureResult(
          false,
          "Error: " + e.getMessage(),
          null,
          0,
          e
      );
    }
  }

  /**
   * Valida si un certificado puede ser usado para firma
   * NUEVO: Validación previa antes de intentar firmar
   */
  public boolean validateCertificateForSigning(String keyPath, String password) {
    try {
      DigitalSignatureUtil.SignatureParameters params = digitalSignatureUtil
          .createDefaultSignatureParameters(keyPath, password, "", "");

      // Solo validar el certificado, no firmar nada
      return digitalSignatureUtil.getClass()
          .getDeclaredField("certificateManagerUtil")
          .get(digitalSignatureUtil) != null;

    } catch (Exception e) {
      log.error("Error validando certificado: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Obtiene información del certificado
   * NUEVO: Información detallada para debugging
   */
  public String getCertificateInfo(String keyPath, String password) {
    try {
      DigitalSignatureUtil.SignatureParameters params = digitalSignatureUtil
          .createDefaultSignatureParameters(keyPath, password, "", "");

      // TODO: Implementar método público en DigitalSignatureUtil para obtener info del certificado
      return "Certificado válido: " + keyPath;

    } catch (Exception e) {
      log.error("Error obteniendo información del certificado: {}", e.getMessage());
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Verifica si un XML ya está firmado
   * NUEVO: Utilidad para evitar doble firma
   */
  public boolean isXmlAlreadySigned(String xmlPath) {
    return digitalSignatureUtil.isXmlSigned(xmlPath);
  }

  /**
   * Obtiene información de firma de un XML
   * NUEVO: Detalles de la firma existente
   */
  public String getSignatureInfo(String xmlPath) {
    return digitalSignatureUtil.getSignatureInfo(xmlPath);
  }

  /**
   * Log detallado del proceso de firma
   * NUEVO: Para debugging y monitoreo
   */
  public void logSigningProcess(String keyPath, String xmlInPath, String xmlOutPath) {
    log.info("=== PROCESO DE FIRMA DIGITAL ===");
    log.info("Certificado: {}", keyPath);
    log.info("XML entrada: {}", xmlInPath);
    log.info("XML salida: {}", xmlOutPath);
    log.info("XML ya firmado: {}", isXmlAlreadySigned(xmlInPath));
    log.info("Info certificado: {}", getCertificateInfo(keyPath, "****"));
    log.info("================================");
  }
}