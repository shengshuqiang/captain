package com.shuqiang.captain.qr.mvp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by shengshuqiang on 2017/4/29.
 */

public abstract class MVPView extends ViewGroup implements IMVPContract.IMVPView {

    protected IMVPContract.IMVPPresenter mvpPresenter;

    public MVPView(Context context) {
        this(context, null);
    }

    public MVPView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthUsed = getPaddingLeft() + getPaddingRight();
        int heightUsed = getPaddingTop() + getPaddingBottom();
        final int size = getChildCount();
        for (int i = 0; i < size; ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        final int size = getChildCount();
        for (int i = 0; i < size; ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.layout(paddingLeft, paddingTop,
                        paddingLeft + child.getMeasuredWidth(), paddingTop + child.getMeasuredHeight());
            }
        }
    }

    @Override
    public void setMVPPresenter(IMVPContract.IMVPPresenter mvpPresenter) {
        this.mvpPresenter = mvpPresenter;
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams params) {
        return (params instanceof MarginLayoutParams);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }
}
