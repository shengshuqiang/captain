package com.shuqiang.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.example.keyboard.R;

/**
 * 带清除按钮的EditText
 */
public class MtEditTextWithClearButton extends EditText {

    /**
     * 清除按钮的图片
     */
    private Drawable mDrawableClearButton = getResources().getDrawable(
            R.drawable.trip_hplus_customizekeyboard_review_ic_search_clear);

    /**
     * 在没有清除按钮的情况下，放一个大小相等的空图片，主要是解决清除按钮状态切换时EditText的background闪烁
     */
    private Drawable mDrawableEmpty;

    /**
     * 标记是否需要处理点击事件
     */
    private boolean isClearButtonClickable;

    public MtEditTextWithClearButton(Context context) {
        super(context);
        init();
    }

    public MtEditTextWithClearButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MtEditTextWithClearButton(Context context, AttributeSet attrs,
                                     int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private OnTouchListener mMtOnTouchListener;
    private OnFocusChangeListener mMTonFocusChangeListener;

    public void setMtOnTouchListener(OnTouchListener listener) {
        mMtOnTouchListener = listener;
    }

    public void setMtOnFocusListener(OnFocusChangeListener listener) {
        mMTonFocusChangeListener = listener;
    }

    private void init() {

        // 初始化两种状态下的清除按钮
        mDrawableClearButton.setBounds(0, 0,
                mDrawableClearButton.getIntrinsicWidth(),
                mDrawableClearButton.getIntrinsicHeight());
        mDrawableEmpty = new Drawable() {

            @Override
            public void setColorFilter(ColorFilter cf) {
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public int getOpacity() {
                return PixelFormat.UNKNOWN;
            }

            @Override
            public void draw(Canvas canvas) {
            }
        };
        mDrawableEmpty.setBounds(mDrawableClearButton.getBounds());

        handleClearButton();

        // 添加输入变化的监听
        addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                MtEditTextWithClearButton.this.handleClearButton();
            }
        });

        // 添加焦点变化的监听
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mMTonFocusChangeListener != null) {
                    mMTonFocusChangeListener.onFocusChange(v, hasFocus);
                }
                MtEditTextWithClearButton.this.handleClearButton();

            }
        });
    }

    private final int leftDrawables = 0;
    private final int topDrawables = 1;
    private final int bottomDrawables = 3;

    /**
     * 处理清除按钮显示与否的函数
     */
    private void handleClearButton() {
        // 如果当前EditText获取了焦点并且内容不为空，则显示清除按钮；其他情况都不显示清空按钮
        if (isFocused() && !TextUtils.isEmpty(getText().toString())) {
            setCompoundDrawables(getCompoundDrawables()[leftDrawables],
                    getCompoundDrawables()[topDrawables], mDrawableClearButton,
                    getCompoundDrawables()[bottomDrawables]);
            isClearButtonClickable = true;
        } else {
            setCompoundDrawables(getCompoundDrawables()[leftDrawables],
                    getCompoundDrawables()[topDrawables], mDrawableEmpty,
                    getCompoundDrawables()[bottomDrawables]);
            isClearButtonClickable = false;
        }
    }

    /**
     * 此函数只用来替换清除按钮的大小和样式
     *
     * @param id 图片的id
     */
    public void setClearButton(int id) {
        try {
            mDrawableClearButton = getResources().getDrawable(id);
        } catch (Exception e) {
            mDrawableClearButton = getResources().getDrawable(
                    R.drawable.trip_hplus_customizekeyboard_review_ic_search_clear);
        } finally {
            init();
        }
    }

    /**
     * 搜索页为了hint能显示全，设置不要这个站位
     */
    public void removeDrawableEmpty() {
        mDrawableEmpty.setBounds(0, 0, 0, 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        handleTouchClearButton(event);
        return super.dispatchTouchEvent(event);

    }

    private void handleTouchClearButton(MotionEvent event) {
        if (mMtOnTouchListener != null) {
            mMtOnTouchListener.onTouch(this, event);
        }

        MtEditTextWithClearButton editText = MtEditTextWithClearButton.this;

        // 如果清除按钮为空，不处理点击事件
        if (editText.getCompoundDrawables()[2] == null) {
            return;
        }

        if (event.getAction() != MotionEvent.ACTION_UP) {
            return;
        }

        // 如果清除按钮处在不显示状态，不处理点击事件
        if (!isClearButtonClickable) {
            return;
        }

        // 按点击位置处理点击事件
        if (event.getX() > editText.getWidth()
                - editText.getPaddingRight()
                - mDrawableClearButton.getIntrinsicWidth()) {
            editText.setText("");
            MtEditTextWithClearButton.this.handleClearButton();
        }

        return;
    }
}
