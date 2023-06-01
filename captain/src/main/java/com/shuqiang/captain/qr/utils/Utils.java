package com.shuqiang.captain.qr.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;

import com.google.android.material.snackbar.Snackbar;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import static android.content.Context.WINDOW_SERVICE;
import static com.shuqiang.toolbox.Contants.QR_DIR;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public class Utils {
    public static final float DIFF = 0.5f;
    // 本次持久缓存二维码密码信息
    public static final String QR_MESSAGE_KEY = "qr-message-key";
    // 无效校验字符
    public static final char INVALID_CHECK_CODE = ' ';

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

    /**
     *  信息加解密，因为采用的是对称加密方案，再次加密即为解密。
     *
     * @param passward 加密密码
     * @param bytesStr 待加密字符串
     * @return
     */
    public static String encode(String passward, String bytesStr) {
        char checkCode = genCheckCode(passward);
        // 校验码放在第一位
        try {
            return  symmetricEncode(passward, checkCode + bytesStr, checkCode, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  信息加解密，因为采用的是对称加密方案，再次加密即为解密。
     *
     * @param passward 加密密码
     * @param bytesStr 待加密字符串
     * @return
     */
    public static String decode(String passward, String bytesStr) throws Exception {
        char checkCode = genCheckCode(passward);
        return symmetricEncode(passward, bytesStr, checkCode, false);
    }
    /**
     *  信息加解密，因为采用的是对称加密方案，再次加密即为解密。
     *
     * @param passward 加密密码
     * @param bytesStr 待加密字符串
     * @param checkCode 密码校验字符串，空则不校验
     * @param isEncode 是否是加密
     * @return
     */
    public static String symmetricEncode(String passward, String bytesStr, char checkCode, boolean isEncode) throws Exception {
        char[] passwardChars = passward.toCharArray();
        char[] byteStrsChars = bytesStr.toCharArray();
        char[] encodeStrChars = new char[byteStrsChars.length];
        // 按单字节遍历待加密字符串，使用加密密码按序号取模进行单字节加密
        for (int i = 0; i < byteStrsChars.length; i++) {
            encodeStrChars[i] = (char) (byteStrsChars[i] ^ passwardChars[i % passwardChars.length]);
            // 解密
            if (!isEncode && checkCode != INVALID_CHECK_CODE && checkCode != encodeStrChars[0]) {
                throw new Exception("解密校验失败");
            }
        }
        // 加密后字符数组转字符串
        String encodebytesStr = new String(encodeStrChars);
//        Log.e("SSU", "byteStrsChars[" + byteStrsChars.length + "]=" + Arrays.toString(byteStrsChars));
//        Log.e("SSU", "encodeStrChars[" + encodeStrChars.length + "]=" + Arrays.toString(encodeStrChars));
//        Log.e("SSU", "encodebytesStr[" + encodebytesStr.length() + "]=" + Arrays.toString(encodebytesStr.toCharArray()));

        return encodebytesStr;
    }

    /**
     * 生成密码校验码，算法为密码字符串依次抑或，用于判断输入密码是否正确
     * @param passward
     * @return
     */
    private static char genCheckCode(String passward) {
        if (!TextUtils.isEmpty(passward)) {
            char[] pwdChars = passward.toCharArray();
            char checkCode = pwdChars[0];
            for (int i = 1; i < pwdChars.length; i++) {
                checkCode ^= pwdChars[i];
            }
            return checkCode;
        }
        return INVALID_CHECK_CODE;
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

    /**
     * 本地持久存储密码二维码信息
     * @param context
     * @param message
     */
    public static final void saveQRMessage(Context context, String message) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(QR_MESSAGE_KEY, message).commit();
    }

    /**
     * 读取本地存储密码二维码信息
     * @param context
     */
    public static final String readQRMessage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(QR_MESSAGE_KEY, "");
    }

    public static void saveBitmap(Context context, View view, Bitmap bitmap) {
        String path = Environment.getExternalStorageDirectory().toString();
        File file = new File(path);
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            //通过file的mkdirs()方法创建<span style="color:#FF0000;">目录中包含却不存在</span>的文件夹
            file.mkdirs();
        }

        String imageName = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "-船长App密码二维马.png";
        file = new File(path + "/" + QR_DIR);
        if (!file.exists()) {
            boolean result = file.mkdirs();
            if (!result) {
                showMessage(view, "创建" + file.getPath() + "失败");
                return;
            }
        }

        file = new File(path + "/" + QR_DIR + "/" + imageName);
        if (file.exists()) {
            file.delete();
        }

        String message = "二维码保存成功";
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 30, out);

            showMessage(view, "账号密码信息二维码保存成功，请您到 相册/图库 中查看");
            Log.e("SSU", message + "\n" + file.getPath());
        } catch (FileNotFoundException e) {
            message = "二维码保存失败";
            showMessage(view, "账号密码信息二维码保存失败");
            e.printStackTrace();
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                showMessage(view, "账号密码信息二维码保存失败");
                e.printStackTrace();
            }
        }
        // 下面的步骤必须有，不然在相册里找不到图片，若不需要让用户知道你保存了图片，可以不写下面的代码。
        // 把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    file.getAbsolutePath(), imageName, null);
//            showMessage(view, "保存成功，请您到 相册/图库 中查看");
//        } catch (FileNotFoundException e) {
//            showMessage(view,  "保存失败");
//            e.printStackTrace();
//        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(file.getPath()))));
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
        return new String(bytes);
//        return Arrays.toString(bytes);
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
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + DIFF);
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

    public static boolean isEmpty(Map map) {
        if (null == map) {
            return true;
        }

        return map.isEmpty();
    }

    public static boolean isEmpty(Collection collection) {
        if (null == collection) {
            return true;
        }

        return collection.isEmpty();
    }

    public static <T> boolean isEmpty(T... items) {
        return items == null || items.length == 0;
    }

    public static void showMessage(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public static String emptyIfNull(String str) {
        return str == null ? "" : str;
    }

}
