package com.example.shengshuqiang.morse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.shengshuqiang.morse.mvp.MVPHelper;
import com.example.shengshuqiang.morse.mvp.MVPView;
import com.example.shengshuqiang.morse.mvpmodule.IMorseMessageRequest;
import com.example.shengshuqiang.morse.mvpmodule.MorseMVPModule;
import com.example.shengshuqiang.morse.mvpmodule.MorseMessageData;
import com.example.shengshuqiang.morse.mvpmodule.MorseMessageItemData;
import com.example.shengshuqiang.morse.mvppresenter.MorseMVPPresenter;
import com.example.shengshuqiang.morse.mvppresenter.MorseMessageItemActionData;
import com.example.shengshuqiang.morse.utils.Utils;
import com.example.shengshuqiang.morse.widgets.MorseItemDetailInfoPopupWindow;
import com.example.shengshuqiang.morse.widgets.PasswordPopupWindow;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Intents;

import java.util.concurrent.CountDownLatch;

public class QRActivity extends AppCompatActivity implements IMorseMessageRequest {
    public static final int MORSE_MESSAGE_REQUEST_CODE = 0;

    private CountDownLatch latch;
    private MorseMessageData morseMessageData;
    private Exception morseMessageException;

    private MVPView mvpView;

    private PasswordPopupWindow passwordPopupWindow;
    private MorseItemDetailInfoPopupWindow itemDetailInfoPopupWindow;
    private MorseMVPPresenter mvpPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        morseMessageData = new MorseMessageData();

        Context context = QRActivity.this;
        mvpView = (MVPView) findViewById(R.id.mvp_view);
        mvpPresenter = new MorseMVPPresenter(context, this);
        MVPHelper.init(mvpView, mvpPresenter, new MorseMVPModule());

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                mvpPresenter.start();
            }
        });

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                showItemDetailInfoPopupWindow(null, MorseItemDetailInfoPopupWindow.MODE.NEW);
            }
        });

        findViewById(R.id.produce).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordPopupWindow();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (MORSE_MESSAGE_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                final String morseMessageStr = data.getStringExtra(Intents.Scan.RESULT);
                showPasswordPopupWindow();

                passwordPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        String password = passwordPopupWindow.getPassword();
                        String morseMessageDecodeStr = Utils.encode(password, morseMessageStr);
                        Log.e("SSU", "password=" + password + ", morseMessageDecodeStr=" + morseMessageDecodeStr);
                        morseMessageData = null;
                        morseMessageException = null;
                        try {
                            morseMessageData = new Gson().fromJson(morseMessageDecodeStr, MorseMessageData.class);
                        } catch (JsonSyntaxException e) {
                            morseMessageException = e;
                            Toast.makeText(QRActivity.this, "密码无效", Toast.LENGTH_SHORT).show();
                        }
                        latch.countDown();
                    }
                });
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public MorseMessageData getData() throws Exception {
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

        if (morseMessageException != null) {
            throw morseMessageException;
        }
        return morseMessageData;
    }

    private void showPasswordPopupWindow() {
        if (passwordPopupWindow == null) {
            passwordPopupWindow = new PasswordPopupWindow(QRActivity.this);
            passwordPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    try {
                        Context context = QRActivity.this;
//                            String str = Utils.getFromAssets(context, Utils.MORSE_ASSETS_FILE_NAME);
                        String morseMessageDecodeStr = new Gson().toJson(morseMessageData);
                        String password = passwordPopupWindow.getPassword();
                        String encodeStr = Utils.encode(password, morseMessageDecodeStr);
                        Bitmap qrCodeBitmap = Utils.createQRCodeBitmap(context, encodeStr);
                        Utils.saveBitmap(context, qrCodeBitmap);
                        Log.e("SSU", "password=" + password + ", morseMessageDecodeStr=" + morseMessageDecodeStr + ", encodeStr=" + encodeStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                passwordPopupWindow.show(mvpView);
            }
        }, 300);

    }

    private void showItemDetailInfoPopupWindow(MorseMessageItemData data, MorseItemDetailInfoPopupWindow.MODE mode) {
        if (itemDetailInfoPopupWindow == null) {
            itemDetailInfoPopupWindow = new MorseItemDetailInfoPopupWindow(QRActivity.this);
            itemDetailInfoPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (itemDetailInfoPopupWindow.isOperateDismiss()) {
                        MorseItemDetailInfoPopupWindow.MODE mode = itemDetailInfoPopupWindow.getMode();
                        MorseMessageItemData morseMessageItemData = itemDetailInfoPopupWindow.getMorseMessageItemData();
                        switch (mode) {
                            case READ:
                                // do nothing
                                break;

                            case WRITE:
                                //
                                mvpPresenter.doAction(new MorseMessageItemActionData(MorseMVPPresenter.EDIT_ITEM_ACTION_ID, morseMessageItemData));
                                break;

                            case NEW:
                                mvpPresenter.doAction(new MorseMessageItemActionData(MorseMVPPresenter.ADD_ITEM_ACTION_ID, morseMessageItemData));
                                break;

                            case DEL:
                                mvpPresenter.doAction(new MorseMessageItemActionData(MorseMVPPresenter.DELETE_ITEM_ACTION_ID, morseMessageItemData));
                                break;
                        }
                    }
                }
            });
        }

        itemDetailInfoPopupWindow.setData(data, mode);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                itemDetailInfoPopupWindow.show(mvpView);
            }
        }, 300);
    }

}
