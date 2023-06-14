package com.shuqiang.captain.qr.test;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.android.crypto.keychain.AndroidCryptoLibrary;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;

import java.io.IOException;

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
        KeyChain keyChain = new KeyChain() {
            private final byte[] mKey = key.getBytes();
            private final byte[] mIV = iv.getBytes();

            @Override
            public byte[] getCipherKey() throws KeyChainException {
                return mKey;
            }

            @Override
            public byte[] getMacKey() throws KeyChainException {
                throw new UnsupportedOperationException();
            }

            @Override
            public byte[] getNewIV() throws KeyChainException {
                return mIV;
            }

            @Override
            public void destroyKeys() {
                // nothing
            }
        };
        CryptoConfig config = CryptoConfig.KEY_128;
        Crypto crypto = new Crypto(keyChain, new AndroidCryptoLibrary(), config);
        byte[] plainBytes = content.getBytes();
        byte[] encrypted = new byte[0];
        try {
            encrypted = crypto.encrypt(plainBytes, new Entity("whatever"));
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (CryptoInitializationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("SSU", "encrypted=" + encrypted.toString());

        byte[] decrypted = new byte[0];
        try {
            decrypted = crypto.decrypt(encrypted, new Entity("whatever"));
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (CryptoInitializationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("SSU", "decrypted=" + decrypted.toString());
    }
}