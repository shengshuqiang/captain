/*
 * Copyright 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shuqiang.captain.qr.utils;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.qrcode.decoder.Mode;

import java.io.UnsupportedEncodingException;

/**
 * @author satorux@google.com (Satoru Takabayashi) - creator
 * @author dswitkin@google.com (Daniel Switkin) - ported from C++
 */
public final class Encoder {

  // The original table is defined in the table 5 of JISX0510:2004 (p.19).
  private static final int[] ALPHANUMERIC_TABLE = {
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  // 0x00-0x0f
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  // 0x10-0x1f
      36, -1, -1, -1, 37, 38, -1, -1, -1, -1, 39, 40, -1, 41, 42, 43,  // 0x20-0x2f
      0,   1,  2,  3,  4,  5,  6,  7,  8,  9, 44, -1, -1, -1, -1, -1,  // 0x30-0x3f
      -1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,  // 0x40-0x4f
      25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1,  // 0x50-0x5f
  };

  public static final String DEFAULT_BYTE_MODE_ENCODING = "ISO-8859-1";

  private Encoder() {
  }

  /**
   * @return the code point of the table used in alphanumeric mode or
   *  -1 if there is no corresponding code in the table.
   */
  static int getAlphanumericCode(int code) {
    if (code < ALPHANUMERIC_TABLE.length) {
      return ALPHANUMERIC_TABLE[code];
    }
    return -1;
  }

  /**
   * Choose the best mode by examining the content. Note that 'encoding' is used as a hint;
   * if it is Shift_JIS, and the input is only double-byte Kanji, then we return {@link Mode#KANJI}.
   */
  public static Mode chooseMode(String content, String encoding) {
    if ("Shift_JIS".equals(encoding)) {
      // Choose Kanji mode if all input are double-byte characters
      return isOnlyDoubleByteKanji(content) ? Mode.KANJI : Mode.BYTE;
    }
    boolean hasNumeric = false;
    boolean hasAlphanumeric = false;
    for (int i = 0; i < content.length(); ++i) {
      char c = content.charAt(i);
      if (c >= '0' && c <= '9') {
        hasNumeric = true;
      } else if (getAlphanumericCode(c) != -1) {
        hasAlphanumeric = true;
      } else {
        return Mode.BYTE;
      }
    }
    if (hasAlphanumeric) {
      return Mode.ALPHANUMERIC;
    }
    if (hasNumeric) {
      return Mode.NUMERIC;
    }
    return Mode.BYTE;
  }

  private static boolean isOnlyDoubleByteKanji(String content) {
    byte[] bytes;
    try {
      bytes = content.getBytes("Shift_JIS");
    } catch (UnsupportedEncodingException ignored) {
      return false;
    }
    int length = bytes.length;
    if (length % 2 != 0) {
      return false;
    }
    for (int i = 0; i < length; i += 2) {
      int byte1 = bytes[i] & 0xFF;
      if ((byte1 < 0x81 || byte1 > 0x9F) && (byte1 < 0xE0 || byte1 > 0xEB)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Append mode info. On success, store the result in "bits".
   */
  static void appendModeInfo(Mode mode, BitArray bits) {
    bits.appendBits(mode.getBits(), 4);
  }

  /**
   * Append "bytes" in "mode" mode (encoding) into "bits". On success, store the result in "bits".
   */
  static void appendBytes(String content,
                          Mode mode,
                          BitArray bits,
                          String encoding) throws WriterException {
    switch (mode) {
      case NUMERIC:
        appendNumericBytes(content, bits);
        break;
      case ALPHANUMERIC:
        appendAlphanumericBytes(content, bits);
        break;
      case BYTE:
        append8BitBytes(content, bits, encoding);
        break;
      case KANJI:
        appendKanjiBytes(content, bits);
        break;
      default:
        throw new WriterException("Invalid mode: " + mode);
    }
  }

  static void appendNumericBytes(CharSequence content, BitArray bits) {
    int length = content.length();
    int i = 0;
    while (i < length) {
      int num1 = content.charAt(i) - '0';
      if (i + 2 < length) {
        // Encode three numeric letters in ten bits.
        int num2 = content.charAt(i + 1) - '0';
        int num3 = content.charAt(i + 2) - '0';
        bits.appendBits(num1 * 100 + num2 * 10 + num3, 10);
        i += 3;
      } else if (i + 1 < length) {
        // Encode two numeric letters in seven bits.
        int num2 = content.charAt(i + 1) - '0';
        bits.appendBits(num1 * 10 + num2, 7);
        i += 2;
      } else {
        // Encode one numeric letter in four bits.
        bits.appendBits(num1, 4);
        i++;
      }
    }
  }

  static void appendAlphanumericBytes(CharSequence content, BitArray bits) throws WriterException {
    int length = content.length();
    int i = 0;
    while (i < length) {
      int code1 = getAlphanumericCode(content.charAt(i));
      if (code1 == -1) {
        throw new WriterException();
      }
      if (i + 1 < length) {
        int code2 = getAlphanumericCode(content.charAt(i + 1));
        if (code2 == -1) {
          throw new WriterException();
        }
        // Encode two alphanumeric letters in 11 bits.
        bits.appendBits(code1 * 45 + code2, 11);
        i += 2;
      } else {
        // Encode one alphanumeric letter in six bits.
        bits.appendBits(code1, 6);
        i++;
      }
    }
  }

  static void append8BitBytes(String content, BitArray bits, String encoding)
      throws WriterException {
    byte[] bytes;
    try {
      bytes = content.getBytes(encoding);
    } catch (UnsupportedEncodingException uee) {
      throw new WriterException(uee);
    }
    for (byte b : bytes) {
      bits.appendBits(b, 8);
    }
  }

  static void appendKanjiBytes(String content, BitArray bits) throws WriterException {
    byte[] bytes;
    try {
      bytes = content.getBytes("Shift_JIS");
    } catch (UnsupportedEncodingException uee) {
      throw new WriterException(uee);
    }
    int length = bytes.length;
    for (int i = 0; i < length; i += 2) {
      int byte1 = bytes[i] & 0xFF;
      int byte2 = bytes[i + 1] & 0xFF;
      int code = (byte1 << 8) | byte2;
      int subtracted = -1;
      if (code >= 0x8140 && code <= 0x9ffc) {
        subtracted = code - 0x8140;
      } else if (code >= 0xe040 && code <= 0xebbf) {
        subtracted = code - 0xc140;
      }
      if (subtracted == -1) {
        throw new WriterException("Invalid byte sequence");
      }
      int encoded = ((subtracted >> 8) * 0xc0) + (subtracted & 0xff);
      bits.appendBits(encoded, 13);
    }
  }

  public static void appendECI(CharacterSetECI eci, BitArray bits) {
    bits.appendBits(Mode.ECI.getBits(), 4);
    // This is correct for values up to 127, which is all we need now.
    bits.appendBits(eci.getValue(), 8);
  }

}
