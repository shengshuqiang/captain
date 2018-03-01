// Copyright (C) 2015 Meituan
// All rights reserved
package com.example.keyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.reflect.Method;

import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * 自定义弹窗键盘
 * 技术点：
 * 1. 使用Android提供的KeyboardView和Keyboard作为键盘的内容
 * 2. 使用PopupWindow作为载体，增加通用性，不依赖于具体布局
 * 3. 增加键盘弹出动画，在键盘会遮挡EditText时，会同时移动Activity内容区域置键盘上方(doKeyboardShowAnim,doKeyboardDismissAnim)
 * 4. 点击键盘外区域，如果是当前EditText，键盘依旧显示，否则，键盘消失（PopupWindow设置setOutsideTouchable为true，并且增加点击拦截KeyBoardViewTouchInterceptor）
 * 5. 去掉点击EditText时触发系统键盘(EditTextSimplyOnTouchListener)
 * <p>
 * 注意：
 * EditText一定是在Activity上面的，Dialog或者Popupwindow上面的EditText目前不支持。
 * 1. 按返回键自定义键盘不会消失，需要主动重写Activity的onBackPressed，处理返回事件（虽然）
 *
 * @Override public void onBackPressed() {
 * if (testBuilder != null && testBuilder.isShow()) {
 * testBuilder.dismiss();
 * } else {
 * this.finish();
 * }
 * }
 * 注：设置popupWindow.setFocusable(true)能达到功能，但是技术点4无效，得不偿失
 * <p>
 * 参考：
 * http://www.fampennings.nl/maarten/android/09keyboard/index.htm
 * <p>
 * User: shengshuqiang(shengshuqiang@meituan.com)
 * Date: 2015-11-02
 * Time: 11:44
 */
public abstract class KeyBoardBinder implements KeyboardActionListener.IFoucusEditTextProvider{
    /* 滚动布局动画时间，和键盘弹出时间相等*/
    private static final int ANIM_DUTION = 250;

    private Activity activity;
    /* 自定义键盘载体*/
    private PopupWindow popupWindow;
    /* 当前Activity的contentview，实际上是contentview父容器*/
    private View mainContentView;
    /* 键盘高度*/
    private int keyboardHeight;
    /* 可复用用于计算坐标数组*/
    private int[] viewLocation;
    /* 当前Activity显示区域*/
    private Rect mainFrameRect;
    /* contentview为了不遮挡输入框而滑动的偏移，用于输入框隐藏后恢复contentview布局*/
    private int deltaY;
    /* 自定义输入框是否显示标志，因为需要做动画，并且系统回主动dismiss popupwindow，所以不用popupwindow.isShow判断*/
    boolean isShow;
    /*自定义键盘完成按钮字体颜色*/
    private int completeColor;

    public KeyBoardBinder(Activity activity) {
        this.activity = activity;

        viewLocation = new int[2];
        mainContentView = activity.findViewById(android.R.id.content);
        mainFrameRect = new Rect();
        mainContentView.getWindowVisibleDisplayFrame(mainFrameRect);
    }


    /**
     * 绑定EditText
     * 注意：被绑定EditText的OnFocusChangeListener和OnTouchListener监听会被覆盖掉
     *
     * @param editText
     */
    public void registerEditText(EditText editText) {
        if (null == editText) {
            return;
        }

        // 监听焦点变化，用于显示/隐藏当前输入法窗口
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    show(view);
                } else {
                    dismiss();
                }
            }
        });

        editText.setOnTouchListener(new EditTextSimplyOnTouchListener());

        // 去掉系统自带键盘
        disableShowSoftInput(editText);
    }

    /**
     * 禁止Edittext弹出软件盘，光标依然正常显示。
     */
    @SuppressLint("NewApi")
    private void disableShowSoftInput(EditText editText) {
        Class<EditText> cls = EditText.class;
        Method method;
        if (Build.VERSION.SDK_INT == ICE_CREAM_SANDWICH_MR1) {
            try {
                method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Build.VERSION.SDK_INT > ICE_CREAM_SANDWICH_MR1 && Build.VERSION.SDK_INT < LOLLIPOP) {
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            editText.setShowSoftInputOnFocus(false);
        }
    }


    /**
     * 去绑定
     * 注意：去绑定EditText的OnFocusChangeListener和OnTouchListener监听会被置空
     *
     * @param editText
     */
    public void unregisterEditText(final EditText editText) {
        if (null == editText) {
            return;
        }

        // 去掉 自定义输入法窗口显示监听
        editText.setOnFocusChangeListener(null);
        editText.setOnTouchListener(null);
    }

    public boolean isShow() {
        return isShow;
    }

    /**
     * 隐藏自定义键盘
     */
    public void dismiss() {
        if (null != popupWindow) {
            popupWindow.dismiss();
        }
    }

    public void setCompleteColor(int completeColor) {
        this.completeColor = completeColor;
    }

    public void show(View view) {
        hideSoftInputMethod(view);

        if (null == popupWindow) {
            // 自定义键盘布局
            View contentView = buildContentView();
            TextView confirmView = (TextView) contentView.findViewById(R.id.complete);
            if (completeColor != 0) {
                confirmView.setTextColor(completeColor);
            }
            confirmView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            KeyboardView keyboardView = (KeyboardView) contentView.findViewById(R.id.keyboardview);
            Keyboard keyboard = new Keyboard(activity, getKeyboardResId());
            keyboardView.setKeyboard(keyboard);
            keyboardView.setPreviewEnabled(false);
            keyboardView.setOnKeyboardActionListener(new KeyboardActionListener(this));

            // 键盘载体PopupWindow
            popupWindow = new PopupWindow(activity);
            popupWindow.setAnimationStyle(R.style.TripHPlusCustomizekeyboardKeyboardAnim);
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(new KeyBoardViewDismissListener());
            popupWindow.setTouchInterceptor(new KeyBoardViewTouchInterceptor());
            popupWindow.setContentView(contentView);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            keyboardHeight = getViewMaxHeigth(contentView);
        }

        if (isShow) {
            return;
        } else {
            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            view.getLocationOnScreen(viewLocation);

            // 计算自定义键盘和待输入EditText是否覆盖，仅覆盖时会移动contentview
            int keyboardLocY = mainFrameRect.bottom - keyboardHeight;
            deltaY = keyboardLocY - viewLocation[1] - view.getMeasuredHeight();
            if (deltaY < 0) {
                doKeyboardShowAnim();
            } else {
                // 不执行动画
                isShow = true;
            }
        }
    }

    /**
     * 获取view最大高度
     *
     * @param view
     * @return
     */
    private static int getViewMaxHeigth(View view) {
        if (null == view) {
            return 0;
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return view.getMeasuredHeight();
    }

    /**
     * 运行 键盘弹出动画
     * TranslateAnimation完成连续效果，
     * scrollBy移动实际布局,使得之后的点击事件生效
     */
    private void doKeyboardShowAnim() {
        // 开启动画
        TranslateAnimation anim = new TranslateAnimation(mainContentView.getScaleX(), mainContentView.getScaleX(),
                mainContentView.getScaleY(),
                mainContentView.getScaleY() + deltaY);
        anim.setDuration(ANIM_DUTION);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainContentView.clearAnimation();
                mainContentView.scrollBy(0, -deltaY);
                isShow = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mainContentView.startAnimation(anim);
    }

    /**
     * 运行 键盘消失动画
     */
    private void doKeyboardDismissAnim() {
        // 滚回原来位置
        if (deltaY >= 0) {
            isShow = false;
            return;
        }
        // 开启动画
        TranslateAnimation anim = new TranslateAnimation(mainContentView.getScaleX(), mainContentView.getScaleX(),
                mainContentView.getScaleY(),
                mainContentView.getScaleY() - deltaY);
        anim.setDuration(ANIM_DUTION);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainContentView.clearAnimation();
                mainContentView.scrollBy(0, deltaY);
                isShow = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mainContentView.startAnimation(anim);
    }

    /**
     * 隐藏系统输入法
     *
     * @param view
     */
    private void hideSoftInputMethod(View view) {
        if (view != null) {
            ((InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 获取当前获得焦点的EditText
     *
     * @return
     */
    @Override
    public EditText getForcusEditText() {
        View focusCurrent = activity.getWindow().getCurrentFocus();
        if (focusCurrent instanceof EditText) {
            EditText edittext = (EditText) focusCurrent;
            return edittext;
        }

        return null;
    }

    /**
     * 构建内容区域布局
     * 可用于子类重载，自定义键盘容器布局
     *
     * @return
     */
    protected View buildContentView() {
        View contentView = LayoutInflater.from(activity).inflate(R.layout.trip_hplus_customizekeyboard_keyboard_layout, null);
        return contentView;
    }

    /**
     * 构建Keyboard布局
     * 可用于子类重载，自定义键盘布局
     *
     * @return
     */
    protected abstract int getKeyboardResId();

    /**
     * 自定义输入法弹窗消失回调
     */
    private class KeyBoardViewDismissListener implements PopupWindow.OnDismissListener {
        @Override
        public void onDismiss() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doKeyboardDismissAnim();
                }
            });
        }
    }

    /**
     * 自定义输入法Touch拦截
     * 完成点击EditText不消失弹窗(默认点击popupwindow外部，弹窗消失)
     */
    private class KeyBoardViewTouchInterceptor implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            EditText editText = getForcusEditText();
            if (null == editText) {
                return false;
            }

            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                int[] location = new int[2];
                editText.getLocationOnScreen(location);
                if (event.getRawX() >= location[0]
                        && event.getRawX() <= (location[0] + editText.getWidth())
                        && event.getRawY() >= location[1]
                        && event.getRawY() <= (location[1] + editText.getHeight())) {
                    // 如果点击的是当今获取焦点的EditText，则消费事件，避免弹窗消失
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * 为了去掉系统自带键盘（不执行onTouchEvent），对Touch事件进行简化，
     * 简化后的功能:
     * 1. 获取焦点
     * 2. 选择游标位置
     */
    private class EditTextSimplyOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            show(view);

            view.onTouchEvent(event);

            hideSoftInputMethod(view);

            return true;
        }
    }
}
