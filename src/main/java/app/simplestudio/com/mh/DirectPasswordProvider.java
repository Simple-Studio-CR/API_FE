package app.simplestudio.com.mh;

import java.security.cert.X509Certificate;
import xades4j.providers.impl.KeyStoreKeyingDataProvider;

/**
 * Proveedor de contraseñas para KeyStore y KeyEntry en XAdES4j.
 * Se usa manualmente con new DirectPasswordProvider(password).
 */
public class DirectPasswordProvider
    implements KeyStoreKeyingDataProvider.KeyStorePasswordProvider,
    KeyStoreKeyingDataProvider.KeyEntryPasswordProvider {

  private final String password;

  public DirectPasswordProvider(String password) {
    this.password = password;
  }

  @Override
  public char[] getPassword() {
    return this.password.toCharArray();
  }

  @Override
  public char[] getPassword(String entryAlias, X509Certificate entryCert) {
    return this.password.toCharArray();
  }
}