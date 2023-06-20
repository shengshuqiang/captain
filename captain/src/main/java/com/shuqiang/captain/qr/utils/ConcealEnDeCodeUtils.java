package com.shuqiang.captain.qr.utils;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;

import captain.R;

public final class ConcealEnDeCodeUtils {


    public static byte[] encrypt(String pwd, byte[] contentBytes) {
        Entity entity = Entity.create(Base64.encodeToString(pwd.getBytes(), Base64.DEFAULT));
        SharedPrefsBackedKeyChain keyChain = new SharedPrefsBackedKeyChain(this, CryptoConfig.KEY_256);
        Crypto crypto = AndroidConceal.get().createCrypto256Bits(keyChain);


        byte[] encrypted = null;
        try {
            encrypted = crypto.encrypt(contentBytes, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        String cipher = Base64.encodeToString(encrypted, Base64.DEFAULT);
//        Log.d("SSU", "encrypted=" + encrypted.length + ", " + encrypted + "encrypted=" + cipher);
        return encrypted;
    }
    private byte[] decrypted(String pwd, String content) {
        Entity entity = Entity.create(Base64.encodeToString(pwd.getBytes(), Base64.DEFAULT));
        SharedPrefsBackedKeyChain keyChain = new SharedPrefsBackedKeyChain(this, CryptoConfig.KEY_256);
        Crypto crypto = AndroidConceal.get().createCrypto256Bits(keyChain);
        byte[] decrypted = null;
        try {
            byte[] cipherTextBytes = Base64.decode(content, Base64.DEFAULT);
            decrypted = crypto.decrypt(cipherTextBytes, entity);
        } catch (Exception e) {
            e.printStackTrace();
//            return null;
        }
        Log.d("SSU", "decrypted=" + decrypted.length + ", " + decrypted + "decrypted=" + new String(decrypted));
        return decrypted;
    }
}