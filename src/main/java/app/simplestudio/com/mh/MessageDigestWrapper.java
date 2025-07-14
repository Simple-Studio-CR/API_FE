package app.simplestudio.com.mh;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.MessageDigest;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessageDigest wrapper that optionally returns a precomputed ("hacked") hash
 * when the input data begins with a specific prefix.
 * <p>
 * Delegates all operations to the underlying MessageDigest except:
 * - On update(byte[], int, int), checks for a prefix to enable hack mode.
 * - On digest, returns the hacked hash if hack mode is active.
 */
public class MessageDigestWrapper extends MessageDigest {
  private static final Logger log = LoggerFactory.getLogger(MessageDigestWrapper.class);
  private static final String HACK_PREFIX = "www.snnsoluciones.com";

  private final MessageDigest delegate;
  private final byte[] hackedHashBytes;
  private boolean hackMode = false;

  /**
   * @param delegate underlying digest instance (e.g. SHA-1)
   * @param hackedHash base64-encoded hash to return when hack mode is triggered
   */
  public MessageDigestWrapper(MessageDigest delegate, String hackedHash) {
    super(delegate.getAlgorithm());
    this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    Objects.requireNonNull(hackedHash, "hackedHash must not be null");
    this.hackedHashBytes = Base64.decodeBase64(hackedHash);
  }

  @Override
  protected void engineUpdate(byte input) {
    delegate.update(input);
  }

  @Override
  protected void engineUpdate(byte[] input, int offset, int len) {
    // Check prefix to enable hack mode
    String sample = new String(input, offset, Math.min(len, HACK_PREFIX.length()), StandardCharsets.UTF_8);
    if (sample.startsWith(HACK_PREFIX)) {
      hackMode = true;
      log.info("Hack mode enabled: input starts with '{}'", HACK_PREFIX);
    }
    delegate.update(input, offset, len);
  }

  @Override
  protected void engineUpdate(ByteBuffer input) {
    delegate.update(input);
  }

  @Override
  protected byte[] engineDigest() {
    if (hackMode) {
      log.info("Returning hacked hash instead of computed digest");
      return hackedHashBytes.clone();
    }
    return delegate.digest();
  }

  @Override
  protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
    byte[] result = engineDigest();
    int copyLen = Math.min(len, result.length);
    System.arraycopy(result, 0, buf, offset, copyLen);
    return copyLen;
  }

  @Override
  protected void engineReset() {
    hackMode = false;
    delegate.reset();
  }

  @Override
  public String toString() {
    return "MessageDigestWrapper[" + delegate.getAlgorithm() + "]";
  }
}