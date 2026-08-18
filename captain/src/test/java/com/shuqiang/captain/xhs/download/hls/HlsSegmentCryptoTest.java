package com.shuqiang.captain.xhs.download.hls;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HlsSegmentCryptoTest {
    private final HlsSegmentCrypto crypto = new HlsSegmentCrypto();

    @Test
    public void decryptUsesExplicitIv() throws Exception {
        byte[] key = "0123456789abcdef".getBytes("UTF-8");
        byte[] iv = crypto.buildIv("0x00000000000000000000000000000007", 99);
        byte[] plain = "authorized fixture transport stream".getBytes("UTF-8");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

        byte[] decrypted = crypto.decrypt(cipher.doFinal(plain), key,
                "0x00000000000000000000000000000007", 99);

        Assert.assertArrayEquals(plain, decrypted);
    }

    @Test
    public void buildIvFallsBackToMediaSequence() throws Exception {
        byte[] iv = crypto.buildIv(null, 0x0102030405060708L);

        Assert.assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0,
                1, 2, 3, 4, 5, 6, 7, 8}, iv);
    }
}
