package com.shuqiang.keyboard;

import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.widget.EditText;

/**
 * 自定义键盘按键监听
 */
public class KeyboardActionListener implements KeyboardView.OnKeyboardActionListener {
    /* 删除按键key*/
    private static final int DEL_KEY = 60001;

    private IFoucusEditTextProvider foucusEditTextProvider;

    public KeyboardActionListener(IFoucusEditTextProvider foucusEditTextProvider) {
        this.foucusEditTextProvider = foucusEditTextProvider;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        EditText editText = foucusEditTextProvider.getForcusEditText();
        if (null == editText) {
            return;
        }

        Editable editable = editText.getText();
        if (null == editable) {
            return;
        }

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (-1 == start || -1 == end) {
            // 选中状态异常
            return;
        }

        if (start < end) {
            // 选中多个
            if (primaryCode == DEL_KEY) {
                editable.delete(start, end);
            } else {
                editable.replace(start, end, Character.toString((char) primaryCode));
            }
        } else if (start == end) {
            // 未选中
            if (primaryCode == DEL_KEY) {
                if (start > 0) {
                    editable.delete(start - 1, end);
                }
            } else {
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }
    }

    @Override
    public void onPress(int arg0) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    public interface IFoucusEditTextProvider {
        EditText getForcusEditText();
    }
}