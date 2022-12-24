package com.shuqiang.captain.widgets;

import android.content.Context;
import android.view.ViewGroup;

import com.shuqiang.popupwindow.MongoliaPopupWindow;

/**
 * Created by shengshuqiang on 2017/5/7.
 */

public class PasswordPopupWindow extends MongoliaPopupWindow {

    private PasswordInputView passwordInputView;
    private String password;

    public PasswordPopupWindow(Context context) {
        super(context);

        passwordInputView = new PasswordInputView(context);
        passwordInputView.setPasswordListener(new PasswordEditText.IPasswordListener() {
            @Override
            public void onComplete(PasswordEditText passwordEditText, String pwd) {
                password = pwd;
                dismiss();
            }
        });
        setContentView(passwordInputView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setTitle(String title) {
        passwordInputView.setTitle(title);
    }

    public String getPassword() {
        return password;
    }

    public void clear() {
        passwordInputView.clear();
        password = null;
    }
}
