package app.simplestudio.com.mh;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import xades4j.UnsupportedAlgorithmException;
import xades4j.algorithms.Algorithm;
import xades4j.algorithms.CanonicalXMLWithoutComments;
import xades4j.algorithms.GenericAlgorithm;
import xades4j.providers.AlgorithmsProviderEx;

/**
 * Proveedor de algoritmos para XAdES4j.
 * <p>
 * - Mapea claves de firma (DSA, RSA) a sus correspondientes URIs.
 * - Define algoritmos de canonicalización y digest según el estándar.
 * - Aplica un hack counter para alternar el digest de ReferenceProperties.
 */
@Component
public class MyAlgorithmsProviderEx implements AlgorithmsProviderEx {
  private static final Logger log = LoggerFactory.getLogger(MyAlgorithmsProviderEx.class);

  // URI de algoritmos de digest
  private static final String SHA1_URI    = "http://www.w3.org/2000/09/xmldsig#sha1";
  private static final String SHA256_URI  = "http://www.w3.org/2001/04/xmlenc#sha256";

  // Mapeo inmutable de algoritmo de clave a Algorithm para firma
  private static final Map<String, Algorithm> SIGNATURE_ALGORITHMS = Map.of(
      "DSA", new GenericAlgorithm("http://www.w3.org/2000/09/xmldsig#dsa-sha1", new Node[0]),
      "RSA", new GenericAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", new Node[0])
  );

  // Contador atómico para alternar digest en ReferenceProperties
  private final AtomicInteger hackCounter = new AtomicInteger();

  /**
   * Devuelve el algoritmo de firma según el tipo de clave.
   */
  @Override
  public Algorithm getSignatureAlgorithm(String keyAlgorithmName) throws UnsupportedAlgorithmException {
    log.debug("Seleccionando algoritmo de firma para clave: {}", keyAlgorithmName);
    Algorithm alg = SIGNATURE_ALGORITHMS.get(keyAlgorithmName);
    if (alg == null) {
      log.error("Algoritmo de firma no soportado: {}", keyAlgorithmName);
      throw new UnsupportedAlgorithmException("Signature algorithm not supported", keyAlgorithmName);
    }
    return alg;
  }

  /**
   * Canonicalización para firma (sin comentarios).
   */
  @Override
  public Algorithm getCanonicalizationAlgorithmForSignature() {
    log.debug("Usando canonicalization for Signature");
    return new CanonicalXMLWithoutComments();
  }

  /**
   * Canonicalización para propiedades de timestamp (sin comentarios).
   */
  @Override
  public Algorithm getCanonicalizationAlgorithmForTimeStampProperties() {
    log.debug("Usando canonicalization for TimeStampProperties");
    return new CanonicalXMLWithoutComments();
  }

  /**
   * Algoritmo de digest para Data Object References (siempre SHA-256).
   */
  @Override
  public String getDigestAlgorithmForDataObjsReferences() {
    return SHA256_URI;
  }

  /**
   * Algoritmo de digest para ReferenceProperties.
   * <p>
   * Alterna: primeras llamadas devuelven SHA-256, luego SHA-1 en adelante.
   */
  @Override
  public String getDigestAlgorithmForReferenceProperties() {
    int count = hackCounter.incrementAndGet();
    String uri = (count > 1 ? SHA1_URI : SHA256_URI);
    log.info("Digest for ReferenceProperties [{}] llamado {} vez/veces", uri, count);
    return uri;
  }

  /**
   * Algoritmo de digest para properties de timestamp (siempre SHA-1).
   */
  @Override
  public String getDigestAlgorithmForTimeStampProperties() {
    return SHA1_URI;
  }
}