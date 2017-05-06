package com.example.shengshuqiang.morse.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.encode.QRCodeEncoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public class Utils {
    public static final String MORSE_ASSETS_FILE_NAME = "asys";

    public static String getFromAssets(Context context, String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);

            String line;
            String result = "";

            while ((line = bufReader.readLine()) != null) {
                result += line;
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String encode(String key, String bytesStr) {
        byte[] keyBytes = key.getBytes();
        byte[] byteStrsBytes = bytesStr2ByteArray(bytesStr);
        byte[] encodeStrBytes = new byte[byteStrsBytes.length];
        for (int i = 0; i < byteStrsBytes.length; i++) {
            encodeStrBytes[i] = (byte) (byteStrsBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

//        Log.d("SSU", "keyBytes=" + Arrays.toString(keyBytes));
//            Log.d("SSU", "strBytes[" + strBytes.length + "]=" + Arrays.toString(strBytes));
//            Log.d("SSU", "encodeStrBytes[" + encodeStrBytes.length + "]=" + Arrays.toString(encodeStrBytes));

        String encodebytesStr = bytes2bytesStr(encodeStrBytes);
//        Log.d("SSU", "str[" + bytesStr.length() + "]=" + bytesStr);
//        Log.d("SSU", "encodeStr[" + encodebytesStr.length() + "]=" + encodebytesStr);

        return encodebytesStr;
    }

    public static Bitmap createQRCodeBitmap(Context context, String message) {
        Intent intent = new Intent(Intents.Encode.ACTION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
        intent.putExtra(Intents.Encode.DATA, message);
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());

        try {
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(context, intent, getQRCodeSmallerDimension(context), false);
            return qrCodeEncoder.encodeAsBitmap();

        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void saveBitmap(Context context, Bitmap bitmap) {
        File file = new File(Environment.getExternalStorageDirectory(), "morse_qrcode.png");
        if (file.exists()) {
            file.delete();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

            Toast.makeText(context, file.getPath(), Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getQRCodeSmallerDimension(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        int width = displaySize.x;
        int height = displaySize.y;
        int smallerDimension = width < height ? width : height;
        return smallerDimension * 7 / 8;
    }

    public static String bytes2bytesStr(byte[] bytes) {
        return Arrays.toString(bytes);
    }

    public static byte[] bytesStr2ByteArray(String bytesStr) {
        byte[] bytes = null;

        if (!TextUtils.isEmpty(bytesStr) && bytesStr.startsWith("[") && bytesStr.endsWith("]")) {
            bytesStr = bytesStr.substring(1, bytesStr.length() - 1);

            String[] byteStrs = bytesStr.split(", ");
            bytes = new byte[byteStrs.length];
            for (int i = 0; i < byteStrs.length; i++) {
                bytes[i] = Byte.parseByte(byteStrs[i]);
            }
        }

        return bytes;
    }

    /**
     * DES加密介绍
     * DES是一种对称加密算法，所谓对称加密算法即：加密和解密使用相同密钥的算法。DES加密算法出自IBM的研究，
     * 后来被美国政府正式采用，之后开始广泛流传，但是近些年使用越来越少，因为DES使用56位密钥，以现代计算能力，
     * 24小时内即可被破解。虽然如此，在某些简单应用中，我们还是可以使用DES加密算法，本文简单讲解DES的JAVA实现
     * 。
     * 注意：DES加密和解密过程中，密钥长度都必须是8的倍数
     */

    /**
     * 加密
     *
     * @param datasource byte[]
     * @param password   String
     * @return byte[]
     */
    public static byte[] code(byte[] datasource, String password, boolean isEnCode) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            //Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES");
            //用密匙初始化Cipher对象
            cipher.init(isEnCode ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, securekey, random);
            return cipher.doFinal(datasource);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }



}
