package com.shuqiang.captain.xhs.download.hls;

import java.io.IOException;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** AES-128 HLS 分片解密与 IV 生成，不保存密钥或明文。 */
public final class HlsSegmentCrypto {
    public byte[] decrypt(byte[] encrypted, byte[] key, String explicitIv, long mediaSequence) throws IOException {
        if (encrypted == null || key == null || key.length != 16) {
            throw new IOException("AES-128 分片或密钥无效");
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new IvParameterSpec(buildIv(explicitIv, mediaSequence)));
            return cipher.doFinal(encrypted);
        } catch (Exception exception) {
            throw new IOException("AES-128 分片解密失败", exception);
        }
    }

    byte[] buildIv(String explicitIv, long mediaSequence) throws IOException {
        byte[] iv = new byte[16];
        if (explicitIv == null || explicitIv.trim().isEmpty()) {
            for (int index = 15; index >= 8; index--) {
                iv[index] = (byte) (mediaSequence & 0xff);
                mediaSequence >>>= 8;
            }
            return iv;
        }
        String hex = explicitIv.trim().toLowerCase(Locale.US);
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }
        if (hex.isEmpty() || hex.length() > 32 || (hex.length() & 1) != 0) {
            throw new IOException("AES-128 IV 格式无效");
        }
        int offset = 16 - hex.length() / 2;
        try {
            for (int index = 0; index < hex.length(); index += 2) {
                iv[offset++] = (byte) Integer.parseInt(hex.substring(index, index + 2), 16);
            }
        } catch (NumberFormatException exception) {
            throw new IOException("AES-128 IV 格式无效", exception);
        }
        return iv;
    }
}
