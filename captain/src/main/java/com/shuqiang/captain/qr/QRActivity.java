package com.shuqiang.captain.qr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.captain.base.BasePermissionActivity;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.utils.PictureFileUtils;
import com.shuqiang.captain.qr.mvp.MVPHelper;
import com.shuqiang.captain.qr.mvp.MVPView;
import com.shuqiang.captain.qr.mvpmodule.MorseMVPModule;
import com.shuqiang.captain.qr.mvpmodule.MorseMessageData;
import com.shuqiang.captain.qr.mvppresenter.MorseMVPPresenter;
import com.shuqiang.captain.qr.mvppresenter.MorseMessageItemActionData;
import com.shuqiang.captain.qr.utils.Utils;
import com.shuqiang.captain.qr.widgets.PasswordPopupWindow;
import com.google.zxing.client.android.QRScanActivity;
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
    // 当前密码
    private String currentPwd;
    // 生成新二维码图片密码
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
                gotoCaptureActivity();
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

        handleInitQRMessage();
    }

    /**
     * 处理初始密码二维码信息，优从本地持久缓存获取，没有则打开默认文件夹扫描
     */
    private void handleInitQRMessage() {
        // 本次持久存储密码二维码
        String qrMessage = Utils.readQRMessage(QRActivity.this);
        if (!TextUtils.isEmpty(qrMessage)) {
            decodeQRMessage(qrMessage);
        } else {
            // 打开最新默认图片
            openLastQRImage();
        }
    }

    private void gotoCaptureActivity() {
        gotoCaptureActivity(null);
    }
    private void gotoCaptureActivity(String qrImagePath) {
        Intent intent = new Intent(QRActivity.this, QRScanActivity.class);
        intent.setAction(Intents.Scan.ACTION);
        if (qrImagePath != null) {
            intent.putExtra(QRScanActivity.QR_IMAGE_PATH, qrImagePath);
        }
        startActivityForResult(intent, MORSE_MESSAGE_REQUEST_CODE);
    }

    private void openLastQRImage() {
        PictureFileUtils.loadLastQRImgPath(this, new PictureFileUtils.LastQRImgPathLoader() {
            @Override
            public void loadPath(LocalMedia lastLocalMedia) {
                if(lastLocalMedia != null) {
                    gotoCaptureActivity(lastLocalMedia.getRealPath());
                }
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
        showPasswordPopupWindow(firstPasswordPopupWindow,"请输入密码", "和当前密码一样不用二次确认密码", new PasswordPopupWindow.OnPasswordCompleteListener() {
            @Override
            public void onComplete(String password) {
                producePwd = password;
                if (producePwd == null) {
                    return;
                }
                if (!TextUtils.isEmpty(producePwd)) {
                    if (TextUtils.equals(currentPwd, producePwd)) {
                        handleEncodeSaveBitmap(password);
                    } else {
                        showPasswordPopupWindow(secondPasswordPopupWindow, "请输入二次确认密码", null, new PasswordPopupWindow.OnPasswordCompleteListener() {
                            @Override
                            public void onComplete(String password) {
                                firstPasswordPopupWindow.dismiss();
                                if (password == null) {
                                    return;
                                }
                                if (!TextUtils.equals(producePwd, password)) {
                                    Utils.showMessage(mvpView, "密码无效，两次输入密码不一致!");
                                } else {
                                    handleEncodeSaveBitmap(password);
                                }
                            }
                        });
                    }
                } else {
                    Utils.showMessage(mvpView, "密码为空，无效!");
                }
//                Log.e("SSU", "password=" + producePwd);
            }
        }, false);
    }

    private void handleEncodeSaveBitmap(String password) {
        try {
            Context context = QRActivity.this;
            String morseMessageDecodeStr = mvpPresenter.getMorseMessageStr();
            String encodeStr = null;
            if (!TextUtils.isEmpty(password)) {
                encodeStr = Utils.encode(password, morseMessageDecodeStr);
                Bitmap qrCodeBitmap = Utils.createQRCodeBitmap(context, encodeStr);
                Utils.saveBitmap(context, mvpView, qrCodeBitmap);
            } else {
                Utils.showMessage(mvpView, "密码为空，无效");
            }
            // Log.e("SSU", "password=" + password + ", morseMessageDecodeStr=" + morseMessageDecodeStr + ", encodeStr=" + encodeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (MORSE_MESSAGE_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                String qrMessage = data.getStringExtra(Intents.Scan.RESULT);
                decodeQRMessage(qrMessage);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void decodeQRMessage(final String qrMessage) {
        showPasswordPopupWindow(firstPasswordPopupWindow, "请输入密码", null, new PasswordPopupWindow.OnPasswordCompleteListener() {
            @Override
            public void onComplete(String password) {
                if (password == null) {
                    return;
                }
                String qrMessageDecodeStr = null;
                if (!TextUtils.isEmpty(password)) {
                    try {
                        qrMessageDecodeStr = Utils.decode(password, qrMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.showMessage(mvpView, "密码校验失败");
                        return;
                    }
                    MorseMessageData morseMessageData = MorseMessageData.toDeserialize(qrMessageDecodeStr);
                    if (morseMessageData != null) {
                        // 本次持久存储密码二维码
                        currentPwd = password;
                        Utils.saveQRMessage(QRActivity.this, qrMessageDecodeStr);
                        mvpPresenter.handleMorseMessageData(morseMessageData);
                    } else {
                        Utils.showMessage(mvpView, "密码无效");
                    }
                } else {
                    Utils.showMessage(mvpView, "密码为空，无效");
                }
                Log.e("SSU", "password=" + password + ", morseMessageDecodeStr=" + qrMessageDecodeStr);
            }
        });
    }

    private void showPasswordPopupWindow(final PasswordPopupWindow passwordPopupWindow, String title, String subTitle, PasswordPopupWindow.OnPasswordCompleteListener onPasswordCompleteListener) {
        showPasswordPopupWindow(passwordPopupWindow, title, subTitle, onPasswordCompleteListener, true);
    }
    private void showPasswordPopupWindow(final PasswordPopupWindow passwordPopupWindow, String title, String subTitle, PasswordPopupWindow.OnPasswordCompleteListener onPasswordCompleteListener, boolean isPasswordCompleteDismiss) {
        passwordPopupWindow.setTitle(title);
        passwordPopupWindow.setSubTitle(subTitle);
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
    protected void onPause() {
        super.onPause();
        // 本次持久存储密码二维码
        String morseMessageDecodeStr = mvpPresenter.getMorseMessageStr();
        if (!TextUtils.isEmpty(currentPwd) && !TextUtils.isEmpty(morseMessageDecodeStr)) {
            Utils.saveQRMessage(QRActivity.this, Utils.encode(currentPwd, morseMessageDecodeStr));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
