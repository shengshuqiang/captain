package com.example.shengshuqiang.morse.widgets;

import android.content.Context;
import android.view.ViewGroup;

import com.example.popupwindow.MongoliaPopupWindow;

/**
 * Created by shengshuqiang on 2017/5/7.
 */

public class PasswordPopupWindow extends MongoliaPopupWindow {

    private PasswordInputView passwordInputView;

    public PasswordPopupWindow(Context context) {
        super(context);

        passwordInputView = new PasswordInputView(context);
        setContentView(passwordInputView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public String getPassword() {
        return passwordInputView.getPassword();
    }

    public void clear() {
        passwordInputView.clear();
    }
}