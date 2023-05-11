package com.shuqiang.captain.qr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;

import com.captain.base.BaseActivity;
import com.captain.base.BasePermissionActivity;
import com.captain.base.LoadingView;
import com.captain.base.PermissionUtils;
import com.shuqiang.captain.qr.mvp.MVPHelper;
import com.shuqiang.captain.qr.mvp.MVPView;
import com.shuqiang.captain.qr.mvpmodule.MorseMVPModule;
import com.shuqiang.captain.qr.mvpmodule.MorseMessageData;
import com.shuqiang.captain.qr.mvppresenter.MorseMVPPresenter;
import com.shuqiang.captain.qr.mvppresenter.MorseMessageItemActionData;
import com.shuqiang.captain.qr.utils.Utils;
import com.shuqiang.captain.qr.widgets.PasswordPopupWindow;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Intents;

import captain.R;

public class QRActivity extends BasePermissionActivity {
    public static final int MORSE_MESSAGE_REQUEST_CODE = 0;

    private MVPView mvpView;
    // 首次输入密码弹窗
    private PasswordPopupWindow firstPasswordPopupWindow;
    // 二次确认输入密码弹窗
    private PasswordPopupWindow secondPasswordPopupWindow;

    private MorseMVPPresenter mvpPresenter;
    private String producePwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = QRActivity.this;
        mvpView = (MVPView) findViewById(R.id.mvp_view);
        mvpPresenter = new MorseMVPPresenter(context);
        firstPasswordPopupWindow = new PasswordPopupWindow(QRActivity.this);
        secondPasswordPopupWindow = new PasswordPopupWindow(QRActivity.this);
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
                externalStoragePermissionHandler.handleRequestPermissionAndWork();
            }
        });
    }

    @Override
    public void onReadWriteFile() {
        handleSaveQRBmp();
    }

    @Override
    protected int getContentLayoutResource() {
        return R.layout.activity_qr;
    }

    private void handleSaveQRBmp() {
        producePwd = null;
        showPasswordPopupWindow(firstPasswordPopupWindow,"请输入密码", new PasswordPopupWindow.OnPasswordCompleteListener() {
            @Override
            public void onComplete(String password) {
                producePwd = password;
                if (producePwd == null) {
                    return;
                }
                if (!TextUtils.isEmpty(producePwd)) {
                    showPasswordPopupWindow(secondPasswordPopupWindow,"请输入二次确认密码", new PasswordPopupWindow.OnPasswordCompleteListener() {
                        @Override
                        public void onComplete(String password) {
                            firstPasswordPopupWindow.dismiss();
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
//                                    Log.e("SSU", "password=" + password + ", morseMessageDecodeStr=" + morseMessageDecodeStr + ", encodeStr=" + encodeStr);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else {
                    Utils.showMessage(mvpView, "密码为空，无效!");
                }
//                Log.e("SSU", "password=" + producePwd);
            }
        }, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (MORSE_MESSAGE_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                final String morseMessageStr = data.getStringExtra(Intents.Scan.RESULT);
                showPasswordPopupWindow(firstPasswordPopupWindow, "请输入密码", new PasswordPopupWindow.OnPasswordCompleteListener() {
                    @Override
                    public void onComplete(String password) {
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

    private void showPasswordPopupWindow(final PasswordPopupWindow passwordPopupWindow, String title, PasswordPopupWindow.OnPasswordCompleteListener onPasswordCompleteListener) {
        showPasswordPopupWindow(passwordPopupWindow, title, onPasswordCompleteListener, true);
    }
    private void showPasswordPopupWindow(final PasswordPopupWindow passwordPopupWindow, String title, PasswordPopupWindow.OnPasswordCompleteListener onPasswordCompleteListener, boolean isPasswordCompleteDismiss) {
        passwordPopupWindow.setTitle(title);
        passwordPopupWindow.setPasswordCompleteDismiss(isPasswordCompleteDismiss);
        passwordPopupWindow.setOnPasswordCompleteListener(onPasswordCompleteListener);
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
