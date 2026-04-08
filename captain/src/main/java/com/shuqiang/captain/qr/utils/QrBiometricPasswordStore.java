package com.shuqiang.captain.qr.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.crypto.Cipher;

/**
 * 通过 Android Keystore 托管二维码解锁密码，只允许在指纹校验通过后取回。
 */
public class QrBiometricPasswordStore {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "qr_message_biometric_unlock";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String PREF_FILE = "qr_biometric_unlock";
    private static final String PREF_ENCRYPTED_PASSWORD = "encrypted_password";
    private static final String PREF_QR_MESSAGE_HASH = "qr_message_hash";

    private final Context appContext;

    public QrBiometricPasswordStore(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public boolean supportsBiometric() {
        return isBiometricSupported();
    }

    public boolean canUseBiometricUnlock(String qrMessage) {
        return supportsBiometric()
                && !TextUtils.isEmpty(readEncryptedPassword())
                && TextUtils.equals(hashQrMessage(qrMessage), readQrMessageHash());
    }

    public void savePassword(String password, String qrMessage) {
        if (TextUtils.isEmpty(password)
                || TextUtils.isEmpty(qrMessage)
                || !supportsBiometric()
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        try {
            ensureKeyPair();
            KeyStore keyStore = getKeyStore();
            Certificate certificate = keyStore.getCertificate(KEY_ALIAS);
            if (certificate == null) {
                return;
            }
            PublicKey publicKey = certificate.getPublicKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            String encryptedPassword = Base64.encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP);
            getPreferences().edit()
                    .putString(PREF_ENCRYPTED_PASSWORD, encryptedPassword)
                    .putString(PREF_QR_MESSAGE_HASH, hashQrMessage(qrMessage))
                    .apply();
        } catch (GeneralSecurityException | IOException e) {
            clearSavedPassword();
        }
    }

    public void clearSavedPassword() {
        getPreferences().edit()
                .remove(PREF_ENCRYPTED_PASSWORD)
                .remove(PREF_QR_MESSAGE_HASH)
                .apply();
    }

    public void authenticate(@NonNull FragmentActivity activity, @NonNull String qrMessage, @NonNull Callback callback) {
        if (!canUseBiometricUnlock(qrMessage)) {
            callback.onError("当前还不能使用指纹解锁，请先输入一次密码");
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.onError("当前 Android 版本不支持指纹解锁");
            return;
        }
        final byte[] encryptedPasswordBytes;
        try {
            encryptedPasswordBytes = Base64.decode(readEncryptedPassword(), Base64.NO_WRAP);
        } catch (IllegalArgumentException e) {
            clearSavedPassword();
            callback.onError("指纹解锁信息已损坏，请重新输入密码");
            return;
        }

        final Cipher decryptCipher;
        try {
            decryptCipher = createDecryptCipher();
        } catch (GeneralSecurityException | IOException e) {
            clearSavedPassword();
            callback.onError("指纹解锁信息已失效，请重新输入密码");
            return;
        }

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        || errorCode == BiometricPrompt.ERROR_USER_CANCELED
                        || errorCode == BiometricPrompt.ERROR_CANCELED) {
                    callback.onCancel();
                    return;
                }
                callback.onError(errString.toString());
            }

            @Override
            public void onAuthenticationFailed() {
                callback.onFailure();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                Cipher cipher = result.getCryptoObject() != null ? result.getCryptoObject().getCipher() : null;
                if (cipher == null) {
                    callback.onError("指纹验证成功，但未取到解锁信息");
                    return;
                }
                try {
                    String password = new String(cipher.doFinal(encryptedPasswordBytes), StandardCharsets.UTF_8);
                    if (TextUtils.isEmpty(password)) {
                        clearSavedPassword();
                        callback.onError("指纹解锁信息为空，请重新输入密码");
                        return;
                    }
                    callback.onPasswordReady(password);
                } catch (GeneralSecurityException e) {
                    clearSavedPassword();
                    callback.onError("指纹解锁信息已失效，请重新输入密码");
                }
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("指纹解锁")
                .setSubtitle("验证后查看当前二维码解码信息")
                .setNegativeButtonText("取消")
                .build();
        biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(decryptCipher));
    }

    private boolean isBiometricSupported() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        return BiometricManager.from(appContext).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Cipher createDecryptCipher() throws GeneralSecurityException, IOException {
        ensureKeyPair();
        KeyStore keyStore = getKeyStore();
        java.security.PrivateKey privateKey = (java.security.PrivateKey) keyStore.getKey(KEY_ALIAS, null);
        if (privateKey == null) {
            throw new GeneralSecurityException("private key is missing");
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void ensureKeyPair() throws GeneralSecurityException, IOException {
        KeyStore keyStore = getKeyStore();
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return;
        }
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setKeySize(2048)
                .setUserAuthenticationRequired(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(true);
        }
        keyPairGenerator.initialize(builder.build());
        keyPairGenerator.generateKeyPair();
    }

    private KeyStore getKeyStore() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        return keyStore;
    }

    private SharedPreferences getPreferences() {
        return appContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    private String readEncryptedPassword() {
        return getPreferences().getString(PREF_ENCRYPTED_PASSWORD, null);
    }

    private String readQrMessageHash() {
        return getPreferences().getString(PREF_QR_MESSAGE_HASH, null);
    }

    private String hashQrMessage(String qrMessage) {
        if (TextUtils.isEmpty(qrMessage)) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(qrMessage.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(digest, Base64.NO_WRAP);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to hash qr message", e);
        }
    }

    public interface Callback {
        void onPasswordReady(String password);

        void onFailure();

        void onError(String message);

        void onCancel();
    }
}
