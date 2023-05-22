package com.shuqiang.captain.qr.test;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.shuqiang.captain.qr.utils.Utils;

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
        // TODO 每添加一行数据做一次溢出检查，最好显示还可容纳字符串数量
        // 最大汉字数
        Bitmap bitmap1 = Utils.createQRCodeBitmap(this, multipleStr(MAX_BYTES / 2 - 10, "船"));
        // 最大字母数
        Bitmap bitmap2 = Utils.createQRCodeBitmap(this, multipleStr(MAX_BYTES * 2 - 100, "S"));
        Log.i(TAG, "test: bitmap1=" + bitmap1 + ", bitmap2=" + bitmap2);
//        TODO 正确性自校验
    }

    private String multipleStr(int mult, String str) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mult; i++) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }
}