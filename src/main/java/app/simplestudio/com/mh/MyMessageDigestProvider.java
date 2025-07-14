package app.simplestudio.com.mh;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xades4j.UnsupportedAlgorithmException;
import xades4j.providers.MessageDigestEngineProvider;

/**
 * Proveedor de instancias de MessageDigest para XAdES4j.
 * <p>
 * Mapea URIs de algoritmos a nombres de {@code MessageDigest}, y opcionalmente
 * envuelve SHA-1 en un {@link MessageDigestWrapper} que inyecta un hash "hackeado".
 * <p>
 * Esta clase ya no es un bean de Spring; debe instanciarse manualmente pasando el
 * valor de hackedHash al constructor (puede ser cadena vacía si no se necesita).
 */
public class MyMessageDigestProvider implements MessageDigestEngineProvider {
  private static final Logger log = LoggerFactory.getLogger(MyMessageDigestProvider.class);

  /** Mapeo inmutable de URIs a nombres de algoritmo Java. */
  private static final Map<String, String> ALGORITHM_MAPPER = Map.of(
      "http://www.w3.org/2000/09/xmldsig#sha1",        "SHA-1",
      "http://www.w3.org/2001/04/xmlenc#sha256",         "SHA-256",
      "http://www.w3.org/2001/04/xmldsig-more#sha384",   "SHA-384",
      "http://www.w3.org/2001/04/xmlenc#sha512",         "SHA-512"
  );

  private final String hackedHash;

  /**
   * @param hackedHash valor Base64 para inyectar en el wrapper de SHA-1
   */
  public MyMessageDigestProvider(String hackedHash) {
    this.hackedHash = Objects.requireNonNull(hackedHash, "hackedHash must not be null");
    log.debug("MyMessageDigestProvider initialized with hackedHash length={}", hackedHash.length());
  }

  /**
   * Obtiene una instancia de {@link MessageDigest} para el URI solicitado.
   * <p>
   * Para SHA-1, retorna un {@link MessageDigestWrapper} que aplica un hash adicional.
   */
  @Override
  public MessageDigest getEngine(String algorithmURI) throws UnsupportedAlgorithmException {
    log.debug("Solicitando MessageDigest para URI: {}", algorithmURI);
    String algorithm = ALGORITHM_MAPPER.get(algorithmURI);
    if (algorithm == null) {
      log.error("Algoritmo no soportado: {}", algorithmURI);
      throw new UnsupportedAlgorithmException(
          "Digest algorithm not supported by MyMessageDigestProvider", algorithmURI
      );
    }

    try {
      MessageDigest md = MessageDigest.getInstance(algorithm);
      log.info("Instancia MessageDigest creada para algoritmo: {}", algorithm);

      if ("SHA-1".equals(algorithm)) {
        log.info("Envolviendo SHA-1 en MessageDigestWrapper con hash de longitud: {}", hackedHash.length());
        return new MessageDigestWrapper(md, hackedHash);
      }

      return md;
    } catch (NoSuchAlgorithmException e) {
      log.error("No se encontró algoritmo MessageDigest: {}", algorithm, e);
      throw new UnsupportedAlgorithmException(e.getMessage(), algorithmURI, e);
    }
  }
}