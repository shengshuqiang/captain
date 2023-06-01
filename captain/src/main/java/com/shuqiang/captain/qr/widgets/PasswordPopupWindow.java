package com.shuqiang.captain.qr.widgets;

import android.content.Context;
import android.view.ViewGroup;

import com.shuqiang.popupwindow.MongoliaPopupWindow;

/**
 * Created by shengshuqiang on 2017/5/7.
 */

public class PasswordPopupWindow extends MongoliaPopupWindow implements PasswordEditText.IPasswordListener {

    private PasswordInputView passwordInputView;
    private String password;
    // 密码输入完成弹窗是否消失
    private boolean isPasswordCompleteDismiss = true;
    private OnPasswordCompleteListener onPasswordCompleteListener;

    public PasswordPopupWindow(Context context) {
        super(context);

        passwordInputView = new PasswordInputView(context);
        passwordInputView.setPasswordListener(this);
        setContentView(passwordInputView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setTitle(String title) {
        passwordInputView.setTitle(title);
    }

    public void setSubTitle(String subTitle) {
        passwordInputView.setSubTitle(subTitle);
    }

    public void setPasswordCompleteDismiss(boolean passwordCompleteDismiss) {
        isPasswordCompleteDismiss = passwordCompleteDismiss;
    }

    public void setOnPasswordCompleteListener(OnPasswordCompleteListener onPasswordCompleteListener) {
        this.onPasswordCompleteListener = onPasswordCompleteListener;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void onComplete(PasswordEditText passwordEditText, String pwd) {
        password = pwd;
        if (isPasswordCompleteDismiss) {
            dismiss();
        }
        if (onPasswordCompleteListener != null) {
            onPasswordCompleteListener.onComplete(password);
        }
    }

    public void clear() {
        passwordInputView.clear();
        password = null;
    }

    public interface OnPasswordCompleteListener {
        void onComplete(String password);
    }
}
