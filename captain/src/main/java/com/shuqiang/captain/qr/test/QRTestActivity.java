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
        test();;
    }

    private void test() {
        String key = "SSU";
        String iv = "123";
        String content = "Hello SSU!";
        Entity entity = Entity.create(Base64.encodeToString(content.getBytes(), Base64.DEFAULT));
        SharedPrefsBackedKeyChain keyChain = new SharedPrefsBackedKeyChain(this, CryptoConfig.KEY_256);
        Crypto crypto = AndroidConceal.get().createCrypto256Bits(keyChain);


        String plain = content;
        byte[] encrypted = null;
        try {
            encrypted = crypto.encrypt(plain.getBytes(), entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String cipher = Base64.encodeToString(encrypted, Base64.DEFAULT);
        Log.d("SSU", "encrypted=" + encrypted.length + ", " + encrypted + "encrypted=" + cipher);


        byte[] decrypted = null;
        try {
            byte[] cipherTextBytes = Base64.decode(cipher, Base64.DEFAULT);
            decrypted = crypto.decrypt(cipherTextBytes, entity);
        } catch (Exception e) {
            e.printStackTrace();
//            return null;
        }
        Log.d("SSU", "decrypted=" + decrypted.length + ", " + decrypted + "decrypted=" + new String(decrypted));
    }
}