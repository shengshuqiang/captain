package com.shuqiang.captain.widgets;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shuqiang.keyboard.KeyboardActionListener;

/**
 * Created by shengshuqiang on 2017/5/7.
 */

public class PasswordInputView extends LinearLayout implements KeyboardActionListener.IFoucusEditTextProvider {
    private PasswordEditText passwordEditText;
    private TextView titleTxtView;
    private KeyboardView keyboardView;

    public PasswordInputView(Context context) {
        this(context, null);
    }

    public PasswordInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);

        inflate(context, com.example.popupwindow.R.layout.password_popup_window, this);
        passwordEditText = (PasswordEditText) findViewById(com.example.popupwindow.R.id.edit_view);
        titleTxtView = (TextView) findViewById(com.example.popupwindow.R.id.title);
        keyboardView = (KeyboardView) findViewById(com.example.popupwindow.R.id.keyboard_view);

        Keyboard keyboard = new Keyboard(context, com.example.popupwindow.R.layout.trip_hplus_customizekeyboard_number_card_keyboard_layout);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(new KeyboardActionListener(this));
    }

    public void setTitle(String title) {
        titleTxtView.setText(title);
    }

    public void setPasswordListener(PasswordEditText.IPasswordListener passwordListener) {
        passwordEditText.setPasswordListener(passwordListener);
    }

    @Override
    public EditText getForcusEditText() {
        return passwordEditText;
    }

    public String getPassword() {
        return passwordEditText.getText().toString();
    }

    public void clear() {
        passwordEditText.setText(null);
    }

}
