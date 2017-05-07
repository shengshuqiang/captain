// Copyright (C) 2015 Meituan
// All rights reserved
package com.example.keyboard;

import android.app.Activity;

import com.meituan.widget.R;

/**
 * 身份证输入法绑定器
 *
 * User: shengshuqiang(shengshuqiang@meituan.com)
 * Date: 2015-11-02
 * Time: 11:55
 */
public class IDCardKeyboardBinder extends KeyBoardBinder {
    public IDCardKeyboardBinder(Activity activity) {
        super(activity);
    }

    @Override
    protected int getKeyboardResId() {
        return R.layout.trip_hplus_customizekeyboard_id_card_keyboard_layout;
    }
}
