package com.shuqiang.captain.qr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.captain.base.BasePermissionActivity;
import com.captain.base.PermissionUtils;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.utils.PictureFileUtils;
import com.luck.picture.lib.utils.ToastUtils;
import com.shuqiang.captain.qr.mvp.MVPHelper;
import com.shuqiang.captain.qr.mvp.MVPView;
import com.shuqiang.captain.qr.mvpmodule.MorseMVPModule;
import com.shuqiang.captain.qr.mvpmodule.MorseMessageData;
import com.shuqiang.captain.qr.mvppresenter.MorseMVPPresenter;
import com.shuqiang.captain.qr.mvppresenter.MorseMessageItemActionData;
import com.shuqiang.captain.qr.utils.QrBiometricPasswordStore;
import com.shuqiang.captain.qr.utils.Utils;
import com.shuqiang.captain.qr.widgets.PasswordPopupWindow;
import com.google.zxing.client.android.QRScanActivity;
import com.google.zxing.client.android.Intents;

import captain.R;

/**
 * 密码二维马页面
 */
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
    private QrBiometricPasswordStore biometricPasswordStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QRActivity activity = QRActivity.this;
        mvpView = (MVPView) findViewById(R.id.mvp_view);
        mvpPresenter = new MorseMVPPresenter(activity);
        firstPasswordPopupWindow = new PasswordPopupWindow(activity);
        secondPasswordPopupWindow = new PasswordPopupWindow(activity);
        biometricPasswordStore = new QrBiometricPasswordStore(activity);
        MVPHelper.init(mvpView, mvpPresenter, new MorseMVPModule());

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionUtils.showPolicyDialog(activity, new PermissionUtils.OnPrivacyAgreeListener() {
                    @Override
                    public void agree() {
                        gotoCaptureActivity();
                    }

                    @Override
                    public void disAgree() {
                        ToastUtils.showToast(activity, "同意《船长App隐私政策》后将为您提供更丰富的功能");
                    }
                });
            }
        });

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionUtils.showPolicyDialog(activity, new PermissionUtils.OnPrivacyAgreeListener() {
                    @Override
                    public void agree() {
                        mvpPresenter.doAction(new MorseMessageItemActionData(MorseMVPPresenter.ADD_ITEM_ACTION_ID, null));
                    }

                    @Override
                    public void disAgree() {
                        ToastUtils.showToast(activity, "同意《船长App隐私政策》后将为您提供更丰富的功能");
                    }
                });
            }
        });

        findViewById(R.id.produce).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionUtils.showPolicyDialog(activity, new PermissionUtils.OnPrivacyAgreeListener() {
                    @Override
                    public void agree() {
                        externalStoragePermissionHandler.handleRequestPermissionAndWork();
                    }

                    @Override
                    public void disAgree() {
                        ToastUtils.showToast(activity, "同意《船长App隐私政策》后将为您提供更丰富的功能");
                    }
                });
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
        showPasswordPopupWindow(firstPasswordPopupWindow,"请输入密码", TextUtils.isEmpty(currentPwd) ? null : "和当前密码一样不用二次确认密码", new PasswordPopupWindow.OnPasswordCompleteListener() {
            @Override
            public void onComplete(String password) {
                producePwd = password;
                if (producePwd == null) {
                    return;
                }
                if (!TextUtils.isEmpty(producePwd)) {
                    if (TextUtils.equals(currentPwd, producePwd)) {
                        firstPasswordPopupWindow.dismiss();
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
                                    currentPwd = producePwd;
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
                Utils.saveQRMessage(QRActivity.this, encodeStr);
                Bitmap qrCodeBitmap = Utils.createQRCodeBitmap(context, encodeStr);
                Utils.saveBitmap(context, mvpView, qrCodeBitmap);
                biometricPasswordStore.savePassword(password, encodeStr);
            } else {
                Utils.showMessage(mvpView, "密码为空，无效");
            }
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
        final boolean canUseBiometricUnlock = biometricPasswordStore.canUseBiometricUnlock(qrMessage);
        final View.OnClickListener biometricActionClickListener = canUseBiometricUnlock ? new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                biometricPasswordStore.authenticate(QRActivity.this, qrMessage, new QrBiometricPasswordStore.Callback() {
                    @Override
                    public void onPasswordReady(String password) {
                        if (handleDecodePassword(qrMessage, password, true)) {
                            firstPasswordPopupWindow.dismiss();
                        }
                    }

                    @Override
                    public void onFailure() {
                        Utils.showMessage(mvpView, "指纹识别失败，请重试");
                    }

                    @Override
                    public void onError(String message) {
                        firstPasswordPopupWindow.setBiometricActionVisible(biometricPasswordStore.canUseBiometricUnlock(qrMessage));
                        Utils.showMessage(mvpView, message);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }
        } : null;
        String subTitle = null;
        if (canUseBiometricUnlock) {
            subTitle = "也可输入密码验证";
        } else if (biometricPasswordStore.supportsBiometric()) {
            subTitle = "首次请输入密码，成功后下次可指纹解锁";
        }
        showPasswordPopupWindow(firstPasswordPopupWindow, "请输入密码", subTitle, new PasswordPopupWindow.OnPasswordCompleteListener() {
            @Override
            public void onComplete(String password) {
                handleDecodePassword(qrMessage, password, false);
            }
        }, true, biometricActionClickListener, canUseBiometricUnlock);
    }

    private boolean handleDecodePassword(String qrMessage, String password, boolean fromBiometric) {
        if (password == null) {
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            Utils.showMessage(mvpView, "密码为空，无效");
            return false;
        }
        final String qrMessageDecodeStr;
        try {
            qrMessageDecodeStr = Utils.decode(password, qrMessage);
        } catch (Exception e) {
            e.printStackTrace();
            if (fromBiometric) {
                Utils.showMessage(mvpView, "当前指纹绑定密码无法解锁此二维码，请改用密码输入");
            } else {
                Utils.showMessage(mvpView, "密码校验失败");
            }
            return false;
        }
        MorseMessageData morseMessageData = MorseMessageData.toDeserialize(qrMessageDecodeStr);
        if (morseMessageData == null) {
            Utils.showMessage(mvpView, fromBiometric ? "当前指纹绑定密码无法解锁此二维码，请改用密码输入" : "密码无效");
            return false;
        }
        currentPwd = password;
        biometricPasswordStore.savePassword(password, qrMessage);
        mvpPresenter.handleMorseMessageData(morseMessageData);
        return true;
    }

    private void showPasswordPopupWindow(final PasswordPopupWindow passwordPopupWindow, String title, String subTitle, PasswordPopupWindow.OnPasswordCompleteListener onPasswordCompleteListener) {
        showPasswordPopupWindow(passwordPopupWindow, title, subTitle, onPasswordCompleteListener, true, null, false);
    }
    private void showPasswordPopupWindow(final PasswordPopupWindow passwordPopupWindow, String title, String subTitle, PasswordPopupWindow.OnPasswordCompleteListener onPasswordCompleteListener, boolean isPasswordCompleteDismiss) {
        showPasswordPopupWindow(passwordPopupWindow, title, subTitle, onPasswordCompleteListener, isPasswordCompleteDismiss, null, false);
    }
    private void showPasswordPopupWindow(final PasswordPopupWindow passwordPopupWindow, String title, String subTitle, PasswordPopupWindow.OnPasswordCompleteListener onPasswordCompleteListener, boolean isPasswordCompleteDismiss, View.OnClickListener onBiometricActionClickListener) {
        showPasswordPopupWindow(passwordPopupWindow, title, subTitle, onPasswordCompleteListener, isPasswordCompleteDismiss, onBiometricActionClickListener, false);
    }
    private void showPasswordPopupWindow(final PasswordPopupWindow passwordPopupWindow, String title, String subTitle, PasswordPopupWindow.OnPasswordCompleteListener onPasswordCompleteListener, boolean isPasswordCompleteDismiss, final View.OnClickListener onBiometricActionClickListener, final boolean autoTriggerBiometric) {
        passwordPopupWindow.setTitle(title);
        passwordPopupWindow.setSubTitle(subTitle);
        passwordPopupWindow.setPasswordCompleteDismiss(isPasswordCompleteDismiss);
        passwordPopupWindow.setOnPasswordCompleteListener(onPasswordCompleteListener);
        passwordPopupWindow.setBiometricActionVisible(onBiometricActionClickListener != null);
        passwordPopupWindow.setOnBiometricActionClickListener(onBiometricActionClickListener);
        passwordPopupWindow.clear();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                passwordPopupWindow.show(mvpView);
                if (autoTriggerBiometric && onBiometricActionClickListener != null) {
                    onBiometricActionClickListener.onClick(mvpView);
                }
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
