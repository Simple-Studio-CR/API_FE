package snn.soluciones.com.mh;

import java.security.cert.X509Certificate;
import xades4j.providers.impl.KeyStoreKeyingDataProvider;

/**
 * Implementación de los proveedores de contraseña para xades4j
 * Implementa tanto KeyStorePasswordProvider como KeyEntryPasswordProvider
 */
public class DirectPasswordProvider implements
    KeyStoreKeyingDataProvider.KeyStorePasswordProvider,
    KeyStoreKeyingDataProvider.KeyEntryPasswordProvider {

  private final String password;

  public DirectPasswordProvider(String password) {
    this.password = password;
  }

  /**
   * Proporciona la contraseña para abrir el KeyStore
   */
  @Override
  public char[] getPassword() {
    return password != null ? password.toCharArray() : new char[0];
  }

  /**
   * Proporciona la contraseña para acceder a una entrada específica del KeyStore
   * @param entryAlias el alias de la entrada
   * @param entryCertificate el certificado asociado con la entrada
   */
  @Override
  public char[] getPassword(String entryAlias, X509Certificate entryCertificate) {
    // Para todas las entradas, usar la misma contraseña
    return getPassword();
  }
}