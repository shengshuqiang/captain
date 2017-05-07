package com.example.shengshuqiang.morse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.shengshuqiang.morse.mvp.MVPHelper;
import com.example.shengshuqiang.morse.mvp.MVPView;
import com.example.shengshuqiang.morse.mvpmodule.IMorseMessageRequest;
import com.example.shengshuqiang.morse.mvpmodule.MorseMVPModule;
import com.example.shengshuqiang.morse.mvpmodule.MorseMessageData;
import com.example.shengshuqiang.morse.mvppresenter.MorseMVPPresenter;
import com.example.shengshuqiang.morse.utils.Utils;
import com.google.gson.Gson;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Intents;

import java.util.concurrent.CountDownLatch;

public class QRActivity extends AppCompatActivity implements IMorseMessageRequest {
    public static final int MORSE_MESSAGE_REQUEST_CODE = 0;

    private CountDownLatch latch;
    private MorseMessageData morseMessageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Context context = QRActivity.this;
        MVPView mvpView = (MVPView) findViewById(R.id.mvp_view);
        MVPHelper.init(mvpView, new MorseMVPPresenter(context, this), new MorseMVPModule());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startActivity(new Intent(QRActivity.this, CaptureActivity.class));
            }
        });

        try {
            String str = Utils.getFromAssets(context, Utils.MORSE_ASSETS_FILE_NAME);
            Gson gson = new Gson();
            MorseMessageData morseMessageData = gson.fromJson(str, MorseMessageData.class);
            str = gson.toJson(morseMessageData);
            String encodeStr = Utils.encode("560569", str);
            Bitmap qrCodeBitmap = Utils.createQRCodeBitmap(context, encodeStr);
            Utils.saveBitmap(context, qrCodeBitmap);
//            Log.d("SSU", "str=" + str + "\nencodeStr=" + encodeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.create_qrcode) {
            QRActivity context = QRActivity.this;
            String str = Utils.getFromAssets(context, Utils.MORSE_ASSETS_FILE_NAME);
            String encodeStr = Utils.encode("560569", str);
            Bitmap qrCodeBitmap = Utils.createQRCodeBitmap(context, encodeStr);
            Utils.saveBitmap(context, qrCodeBitmap);
            Log.d("SSU", "encodeStr=" + encodeStr);
//            String decodeStr = Utils.encode("560569", encodeStr);
//            Log.d("SSU", "decodeStr=" + new String(Utils.bytesStr2ByteArray(decodeStr)));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (MORSE_MESSAGE_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                String morseMessageStr = data.getStringExtra(Intents.Scan.RESULT);
                String morseMessageDecodeStr = Utils.encode("560569", morseMessageStr);
                Log.d("SSU", "morseMessageDecodeStr=" + morseMessageDecodeStr);
                morseMessageData = new Gson().fromJson(morseMessageDecodeStr, MorseMessageData.class);
                latch.countDown();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public MorseMessageData getData() {
        if (latch != null) {
            latch.countDown();
        }
        latch = new CountDownLatch(1);
        Intent intent = new Intent(QRActivity.this, CaptureActivity.class);
        intent.setAction(Intents.Scan.ACTION);
        startActivityForResult(intent, MORSE_MESSAGE_REQUEST_CODE);

        try {
            latch.await();
        } catch (InterruptedException e) {

        }


        return morseMessageData;
    }


}
