package app.simplestudio.com.mh;


import java.security.cert.X509Certificate;
import java.util.List;
import xades4j.providers.impl.KeyStoreKeyingDataProvider;

public class FirstCertificateSelector implements KeyStoreKeyingDataProvider.SigningCertSelector {
  public X509Certificate selectCertificate(List<X509Certificate> availableCertificates) {
    return availableCertificates.get(0);
  }
}

