package com.example.keyboard;

import android.app.Activity;
import android.widget.EditText;

/**
 * 自定义键盘绑定接口实现类
 * <p>
 * User: shengshuqiang(shengshuqiang@meituan.com)
 * Date: 2015-12-08
 * Time: 16:20
 */
public class ImplKeyboardBinder implements IKeyboardBinder {
    private Activity activity;
    /* 自定义输入法绑定器*/
    private IDCardKeyboardBinder idCardKeyboardBinder;
    /*键盘完成按钮颜色配置*/
    private int completeColor;


    public ImplKeyboardBinder(Activity activity) {
        this.activity = activity;
    }

    public void setCompleteColor(int completeColor) {
        this.completeColor = completeColor;
    }

    /**
     * EditText一定是在Activity上面的，Dialog或者Popupwindow上面的EditText目前不支持。
     *
     * @param editText
     */
    @Override
    public void registerEditText(EditText editText) {
        if (null == idCardKeyboardBinder) {
            idCardKeyboardBinder = new IDCardKeyboardBinder(activity);
        }
        if (completeColor != 0) {
            idCardKeyboardBinder.setCompleteColor(completeColor);
        }
        idCardKeyboardBinder.registerEditText(editText);
    }

    @Override
    public void unregisterEditText(EditText editText) {
        if (null != idCardKeyboardBinder) {
            idCardKeyboardBinder.unregisterEditText(editText);
        }
    }

    /**
     * 是否拦截 返回键
     * 需要在Acitvity的OnBackPressed方法中调用
     *
     * @return
     */
    public boolean interceptorOnBackPressed() {
        if (null != idCardKeyboardBinder && idCardKeyboardBinder.isShow()) {
            // 当自定义键盘显示时，按返回键使键盘消失
            idCardKeyboardBinder.dismiss();
            return true;
        }

        return false;
    }
}
