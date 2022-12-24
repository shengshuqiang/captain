package com.shuqiang.captain;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;

import com.shuqiang.captain.mvp.MVPHelper;
import com.shuqiang.captain.mvp.MVPView;
import com.shuqiang.captain.mvpmodule.MorseMVPModule;
import com.shuqiang.captain.mvpmodule.MorseMessageData;
import com.shuqiang.captain.mvppresenter.MorseMVPPresenter;
import com.shuqiang.captain.mvppresenter.MorseMessageItemActionData;
import com.shuqiang.captain.utils.Utils;
import com.shuqiang.captain.widgets.PasswordPopupWindow;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Intents;
import captain.R;

public class QRActivity extends AppCompatActivity {
    public static final int MORSE_MESSAGE_REQUEST_CODE = 0;

    private MVPView mvpView;

    private PasswordPopupWindow passwordPopupWindow;
    private MorseMVPPresenter mvpPresenter;
    private String producePwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Context context = QRActivity.this;
        mvpView = (MVPView) findViewById(R.id.mvp_view);
        mvpPresenter = new MorseMVPPresenter(context);
        MVPHelper.init(mvpView, mvpPresenter, new MorseMVPModule());

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QRActivity.this, CaptureActivity.class);
                intent.setAction(Intents.Scan.ACTION);
                startActivityForResult(intent, MORSE_MESSAGE_REQUEST_CODE);
            }
        });

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mvpPresenter.doAction(new MorseMessageItemActionData(MorseMVPPresenter.ADD_ITEM_ACTION_ID, null));
            }
        });

        findViewById(R.id.produce).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                producePwd = null;
                showPasswordPopupWindow("请输入密码", new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        producePwd = passwordPopupWindow.getPassword();
                        if (producePwd == null) {
                            return;
                        }
                        if (!TextUtils.isEmpty(producePwd)) {
                            showPasswordPopupWindow("请输入确认密码", new PopupWindow.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    String password = passwordPopupWindow.getPassword();
                                    if (password == null) {
                                        return;
                                    }
                                    if (!TextUtils.equals(producePwd, password)) {
                                        Utils.showMessage(mvpView, "密码无效，两次输入密码不一致!");
                                    } else {
                                        try {
                                            Context context = QRActivity.this;
                                            MorseMessageData morseMessageData = mvpPresenter.getMorseMessageData();
                                            String morseMessageDecodeStr = morseMessageData.toSerialize();
                                            String encodeStr = null;
                                            if (!TextUtils.isEmpty(password)) {
                                                encodeStr = Utils.encode(password, morseMessageDecodeStr);
                                                Bitmap qrCodeBitmap = Utils.createQRCodeBitmap(context, encodeStr);
                                                Utils.saveBitmap(context, mvpView, qrCodeBitmap);
                                            } else {
                                                Utils.showMessage(mvpView, "密码为空，无效");
                                            }
                                            Log.e("SSU", "password=" + password + ", morseMessageDecodeStr=" + morseMessageDecodeStr + ", encodeStr=" + encodeStr);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } else {
                            Utils.showMessage(mvpView, "密码为空，无效!");
                        }
                        Log.e("SSU", "password=" + producePwd);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (MORSE_MESSAGE_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                final String morseMessageStr = data.getStringExtra(Intents.Scan.RESULT);
                showPasswordPopupWindow("请输入密码", new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        String password = passwordPopupWindow.getPassword();
                        if (password == null) {
                            return;
                        }
                        String morseMessageDecodeStr = null;
                        if (!TextUtils.isEmpty(password)) {
                            morseMessageDecodeStr = Utils.encode(password, morseMessageStr);
                            MorseMessageData morseMessageData = MorseMessageData.toDeserialize(morseMessageDecodeStr);
                            if (morseMessageData != null) {
                                mvpPresenter.handleMorseMessageData(morseMessageData);
                            } else {
                                Utils.showMessage(mvpView, "密码无效");
                            }
                        } else {
                            Utils.showMessage(mvpView, "密码为空，无效");
                        }
                        Log.e("SSU", "password=" + password + ", morseMessageDecodeStr=" + morseMessageDecodeStr);
                    }
                });
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showPasswordPopupWindow(String title, PopupWindow.OnDismissListener dismissListener) {
        if (passwordPopupWindow == null) {
            passwordPopupWindow = new PasswordPopupWindow(QRActivity.this);
        }
        passwordPopupWindow.setTitle(title);
        passwordPopupWindow.setOnDismissListener(dismissListener);
        passwordPopupWindow.clear();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                passwordPopupWindow.show(mvpView);
            }
        }, 300);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}