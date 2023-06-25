package com.shuqiang.captain.qr.test;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.AndroidCryptoLibrary;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;
import com.google.zxing.WriterException;
import com.shuqiang.captain.qr.utils.Utils;

import java.io.IOException;

import captain.BuildConfig;
import captain.R;

public class QRTestActivity extends AppCompatActivity {
    public static final String TAG = "QRTestActivity";
    // QrCode二维码最大容量 https://tuzim.net/blog/233.html
    public static final int MAX_BYTES = 2953;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrtest);
//        testQRCode(983);
//        testQRCode(984);
//        testQRCode(985);
        testCryptoEnDe();
    }

    private void testQREncode() {

    }

//    测试二维码大小
//    https://www.cnblogs.com/mq0036/p/14445719.html
//    QR码最大数据容量（对于版本40）
//    数字	最多7089字元
//    字母	最多4296字元
//    二进制数（8bit）	最多2953字节
//    日文汉字/片假名	最多1,817字元（采用Shift JIS）
//    中文汉字	最多984字元（采用UTF-8）
    private void testQRCode(int size) {
        String message = "";
        for (int i = 0; i < size; i++) {
            message += "盛";
        }
        try {
            int count = Utils.getLastBytesCount(message);
            Log.d("SSU", "size=" + size +  "count=" + count + ", " + "盛".getBytes(UTF_8));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
//    测试加解密
    private void testCryptoEnDe() {
        String pwd = "123456";
//        Entity entity = Entity.create(Base64.encodeToString(pwd.getBytes(), Base64.DEFAULT));
        Entity entity = Entity.create(pwd);
        SharedPrefsBackedKeyChain keyChain = new SharedPrefsBackedKeyChain(this, CryptoConfig.KEY_128);
        Crypto crypto = AndroidConceal.get().createCrypto128Bits(keyChain);


        String plain = "Hello 盛书强!";
        byte[] plainTextBytes = plain.getBytes();
        byte[] encrypted = null;
        try {
            encrypted = crypto.encrypt(plainTextBytes, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String cipher = Base64.encodeToString(encrypted, Base64.DEFAULT);
        Log.d("SSU", "plainTextBytes=" + plainTextBytes.length + ", encrypted=" + encrypted.length + ", " + encrypted + "encrypted=" +cipher.length() + "," + cipher);

        byte[] decrypted = null;
        byte[] cipherTextBytes = Base64.decode(cipher, Base64.DEFAULT);
        try {
            decrypted = crypto.decrypt(cipherTextBytes, entity);
        } catch (Exception e) {
            e.printStackTrace();
//            return null;
        }
        Log.d("SSU", "cipherTextBytes=" + cipherTextBytes.length + ", decrypted=" + decrypted.length + ", "  + "decrypted="  +new String(decrypted).length() + "," + new String(decrypted));
    }
}