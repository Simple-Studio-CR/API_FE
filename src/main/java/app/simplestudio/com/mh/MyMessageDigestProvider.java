package app.simplestudio.com.mh;

import app.simplestudio.com.mh.MessageDigestWrapper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import xades4j.UnsupportedAlgorithmException;
import xades4j.providers.MessageDigestEngineProvider;

public class MyMessageDigestProvider implements MessageDigestEngineProvider {
  private static final HashMap<String, String> algorithmMapper = new HashMap<>(4);
  
  static {
    algorithmMapper.put("http://www.w3.org/2000/09/xmldsig#sha1", "SHA-1");
    algorithmMapper.put("http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256");
    algorithmMapper.put("http://www.w3.org/2001/04/xmldsig-more#sha384", "SHA-384");
    algorithmMapper.put("http://www.w3.org/2001/04/xmlenc#sha512", "SHA-512");
  }
  
  private String hackedHash = null;
  
  public MyMessageDigestProvider(String hackedHash) {
    this.hackedHash = hackedHash;
  }
  
  public MessageDigest getEngine(String digestAlgorithmURI) throws UnsupportedAlgorithmException {
    String digestAlgorithmName = algorithmMapper.get(digestAlgorithmURI);
    if (null == digestAlgorithmName)
      throw new UnsupportedAlgorithmException("Digest algorithm not supported by the provider", digestAlgorithmURI); 
    try {
      MessageDigest messageDigest = null;
      if (digestAlgorithmName.equals("SHA-1")) {
        messageDigest = MessageDigest.getInstance(digestAlgorithmName);
        MessageDigestWrapper messageDigestWrapper = new MessageDigestWrapper(messageDigest, this.hackedHash);
      } else {
        messageDigest = MessageDigest.getInstance(digestAlgorithmName);
      } 
      return messageDigest;
    } catch (NoSuchAlgorithmException nsae) {
      throw new UnsupportedAlgorithmException(nsae.getMessage(), digestAlgorithmURI, nsae);
    } 
  }
}

