package app.simplestudio.com.mh;

import java.util.HashMap;
import java.util.Map;
import xades4j.UnsupportedAlgorithmException;
import xades4j.algorithms.Algorithm;
import xades4j.algorithms.CanonicalXMLWithoutComments;
import xades4j.algorithms.GenericAlgorithm;
import xades4j.providers.AlgorithmsProviderEx;

public class MyAlgorithmsProviderEx implements AlgorithmsProviderEx {
  private int hackCounter = 0;
  
  private static final Map<String, Algorithm> signatureAlgsMaps = new HashMap<>(2);
  
  static {
    signatureAlgsMaps.put("DSA", new GenericAlgorithm("http://www.w3.org/2000/09/xmldsig#dsa-sha1", new org.w3c.dom.Node[0]));
    signatureAlgsMaps.put("RSA", new GenericAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", new org.w3c.dom.Node[0]));
  }
  
  public Algorithm getSignatureAlgorithm(String keyAlgorithmName) throws UnsupportedAlgorithmException {
    Algorithm sigAlg = signatureAlgsMaps.get(keyAlgorithmName);
    if (null == sigAlg)
      throw new UnsupportedAlgorithmException("Signature algorithm not supported by the provider", keyAlgorithmName); 
    return sigAlg;
  }
  
  public Algorithm getCanonicalizationAlgorithmForSignature() {
    return (Algorithm)new CanonicalXMLWithoutComments();
  }
  
  public Algorithm getCanonicalizationAlgorithmForTimeStampProperties() {
    return (Algorithm)new CanonicalXMLWithoutComments();
  }
  
  public String getDigestAlgorithmForDataObjsReferences() {
    return "http://www.w3.org/2001/04/xmlenc#sha256";
  }
  
  public String getDigestAlgorithmForReferenceProperties() {
    if (this.hackCounter == 1)
      return "http://www.w3.org/2000/09/xmldsig#sha1"; 
    this.hackCounter++;
    return "http://www.w3.org/2001/04/xmlenc#sha256";
  }
  
  public String getDigestAlgorithmForTimeStampProperties() {
    return "http://www.w3.org/2000/09/xmldsig#sha1";
  }
}

