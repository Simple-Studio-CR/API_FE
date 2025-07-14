package app.simplestudio.com.mh;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import xades4j.providers.impl.KeyStoreKeyingDataProvider;

/**
 * Selecciona el certificado de firma (SigningCertSelector) del KeyStoreProvider.
 * <p>
 * Actualmente retorna el primer certificado disponible que cumpla con USO DE FIRMA,
 * y lanza excepción si no hay ninguno.
 */
@Component
public class FirstCertificateSelector implements KeyStoreKeyingDataProvider.SigningCertSelector {
  private static final Logger log = LoggerFactory.getLogger(FirstCertificateSelector.class);

  /**
   * Retorna el primer certificado válido para firma.
   *
   * @param availableCertificates lista de certificados disponibles en el KeyStore
   * @return certificado seleccionado
   * @throws IllegalStateException si la lista está vacía o ninguno es apto
   */
  @Override
  public X509Certificate selectCertificate(List<X509Certificate> availableCertificates) {
    Objects.requireNonNull(availableCertificates, "availableCertificates must not be null");

    log.debug("Certificates available for selection: {}", availableCertificates.size());

    for (X509Certificate cert : availableCertificates) {
      if (isSuitableForSigning(cert)) {
        log.info("Selected certificate for signing: Subject='{}', SerialNumber='{}'",
            cert.getSubjectX500Principal(), cert.getSerialNumber());
        return cert;
      }
    }

    String msg = "No suitable signing certificate found in KeyStore";
    log.error(msg);
    throw new IllegalStateException(msg);
  }

  /**
   * Comprueba si el certificado es apto para firma (keyUsage digitalSignature o nonRepudiation).
   */
  private boolean isSuitableForSigning(X509Certificate cert) {
    boolean[] usages = cert.getKeyUsage();
    if (usages != null && usages.length > 0) {
      // digitalSignature is index 0, nonRepudiation is index 1
      return usages[0] || (usages.length > 1 && usages[1]);
    }
    // Si no hay keyUsage definido, asume que es válido
    return true;
  }
}