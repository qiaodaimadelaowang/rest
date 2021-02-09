package com.github.gobars.rest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * inline commons-codec库中的DigestUtils方法，oauth2中排除的该类
 *
 * @author tinyhuiwang
 */
public final class DigestUtils {

  private static final char[] DIGITS_LOWER = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };
  /** Used to build output as Hex */
  private static final char[] DIGITS_UPPER = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };
  private DigestUtils() {}

  public static MessageDigest getSha256Digest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static String encodeHexString(final byte[] data) {
    return new String(encodeHex(data));
  }

  protected static char[] encodeHex(final byte[] data, final char[] toDigits) {
    final int l = data.length;
    final char[] out = new char[l << 1];
    // two characters form the hex value.
    for (int i = 0, j = 0; i < l; i++) {
      out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
      out[j++] = toDigits[0x0F & data[i]];
    }
    return out;
  }

  private static char[] encodeHex(byte[] data) {
    return encodeHex(data, true);
  }

  private static char[] encodeHex(byte[] data, boolean toLowerCase) {
    return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
  }

  public static String sha256Hex(final byte[] data) {
    return encodeHexString(sha256(data));
  }

  public static byte[] sha256(final byte[] data) {
    return getSha256Digest().digest(data);
  }
}
