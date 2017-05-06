package com.example.shengshuqiang.morse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.shengshuqiang.morse.mvp.MVPHelper;
import com.example.shengshuqiang.morse.mvp.MVPView;
import com.example.shengshuqiang.morse.mvpmodule.MorseMVPModule;
import com.example.shengshuqiang.morse.mvppresenter.MorseMVPPresenter;
import com.example.shengshuqiang.morse.utils.Utils;
import com.google.zxing.client.android.CaptureActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Context context = MainActivity.this;
        MVPView mvpView = (MVPView) findViewById(R.id.mvp_view);
        MVPHelper.init(mvpView, new MorseMVPPresenter(context), new MorseMVPModule(context));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startActivity(new Intent(MainActivity.this, CaptureActivity.class));
            }
        });
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
            MainActivity context = MainActivity.this;
            String str = Utils.getFromAssets(context, Utils.MORSE_ASSETS_FILE_NAME);
            String encodeStr = Utils.encode("560569", Utils.bytes2bytesStr(str.getBytes()));
            Bitmap qrCodeBitmap = Utils.createQRCodeBitmap(context, encodeStr);
            Utils.saveBitmap(context, qrCodeBitmap);
//                Log.d("SSU", "encodeStr=" + encodeStr);
//            String decodeStr = Utils.encode("560569", encodeStr);
//            Log.d("SSU", "decodeStr=" + new String(Utils.bytesStr2ByteArray(decodeStr)));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
