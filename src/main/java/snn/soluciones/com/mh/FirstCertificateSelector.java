package snn.soluciones.com.mh;

import java.security.cert.X509Certificate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xades4j.providers.impl.KeyStoreKeyingDataProvider;

public class FirstCertificateSelector implements KeyStoreKeyingDataProvider.SigningCertificateSelector {

  private static final Logger log = LoggerFactory.getLogger(FirstCertificateSelector.class);

  @Override
  public Entry selectCertificate(List<Entry> availableCertificates) {
    if (availableCertificates == null || availableCertificates.isEmpty()) {
      throw new IllegalStateException("No hay certificados disponibles para seleccionar");
    }

    log.debug("Certificados disponibles: {}", availableCertificates.size());

    // Opcionalmente, puedes loggear información sobre los certificados
    for (int i = 0; i < availableCertificates.size(); i++) {
      Entry entry = availableCertificates.get(i);
      X509Certificate cert = entry.getCertificate();
      log.debug("Certificado {}: Alias='{}', Subject='{}'",
          i, entry.getAlias(), cert.getSubjectDN());
    }

    // Seleccionar el primer certificado
    Entry selected = availableCertificates.get(0);
    log.info("Certificado seleccionado: Alias='{}', Subject='{}'",
        selected.getAlias(), selected.getCertificate().getSubjectDN());

    return selected;
  }
}