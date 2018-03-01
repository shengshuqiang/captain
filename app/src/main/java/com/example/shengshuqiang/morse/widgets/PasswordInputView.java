package com.example.shengshuqiang.morse.widgets;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.keyboard.KeyboardActionListener;

/**
 * Created by shengshuqiang on 2017/5/7.
 */

public class PasswordInputView extends LinearLayout implements KeyboardActionListener.IFoucusEditTextProvider {
    private EditText editText;
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
        editText = (EditText) findViewById(com.example.popupwindow.R.id.edit_view);
        keyboardView = (KeyboardView) findViewById(com.example.popupwindow.R.id.keyboard_view);

        Keyboard keyboard = new Keyboard(context, com.example.popupwindow.R.layout.trip_hplus_customizekeyboard_number_card_keyboard_layout);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(new KeyboardActionListener(this));
    }

    @Override
    public EditText getForcusEditText() {
        return editText;
    }

    public String getPassword() {
        return editText.getText().toString();
    }
}
