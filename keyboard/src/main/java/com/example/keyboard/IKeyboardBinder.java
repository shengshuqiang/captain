package com.example.keyboard;

import android.widget.EditText;

/**
 * 自定义键盘绑定接口
 * User: shengshuqiang(shengshuqiang@meituan.com)
 * Date: 2015-12-08
 * Time: 16:20
 */
public interface IKeyboardBinder {
    void registerEditText(EditText editText);
    void unregisterEditText(EditText editText);
}
