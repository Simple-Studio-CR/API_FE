package snn.soluciones.com.mh;

import snn.soluciones.com.util.DigitalSignatureUtil;
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
}