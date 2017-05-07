// Copyright (C) 2015 Meituan
// All rights reserved
package com.meituan.widget.popupwindow;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.meituan.widget.R;

/**
 * 背景为蒙层的PopupWindow,
 * 相对于某个view上面显示、某个view下面显示、屏幕正中显示
 * <p>
 * User: shengshuqiang(shengshuqiang@meituan.com)
 * Date: 2015-10-27
 * Time: 20:08
 */
public class MongoliaPopupWindow {
    protected Context context;
    /* 最终显示的PopupWindow*/
    protected PopupWindow popupWindow;
    /* 用于放置外部传入view的内容区*/
    protected View contentView;
    /* 点击PopupWindow的监听，如果没有被子view消费监听，则调用该监听*/
    protected View.OnClickListener onClickListener;
    protected PopupWindow.OnDismissListener onDismissListener;
    /* PopupWindow消失动画*/
    protected Animation outAnim;
    protected boolean isDismissing;
    protected View maskView;
    protected Animation maskFadeIn;
    protected Animation maskFadeOut;

    public MongoliaPopupWindow(Context context) {
        this.context = context;
        maskFadeIn = setUpFadeIn();
        maskFadeOut = setUpFadeOut();
        popupWindow = new PopupWindow() {
            @Override
            public void dismiss() {
                if (isDismissing) {
                    return;
                }

                isDismissing = true;

                if (maskView != null) {
                    maskView.clearAnimation();
                }
                contentView.clearAnimation();
                if (maskFadeOut != null) {
                    maskView.startAnimation(maskFadeOut);
                }
                // 执行退出动画，动画执行后再dismiss窗口
                if (null != outAnim) {
                    outAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    dismissPopupWindow();
                                }
                            });
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    contentView.startAnimation(outAnim);
                } else {
                    dismissPopupWindow();
                }

                if (null != onDismissListener) {
                    onDismissListener.onDismiss();
                }
            }

            // 为了实现退出动画，所以阻断dismiss
            private void dismissPopupWindow() {
                super.dismiss();
                isDismissing = false;
            }
        };
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    /**
     * 设置内容
     *
     * @param resId 内容区域资源id
     */
    public View setContentView(int resId) {
        FrameLayout frameLayout = buildFrameLayout();
        contentView = LayoutInflater.from(context).inflate(resId, frameLayout, false);
        frameLayout.addView(contentView);
        popupWindow.setContentView(frameLayout);
        return contentView;
    }

    /**
     * 设置内容
     *
     * @param contentView
     */
    public void setContentView(View contentView) {
        setContentView(contentView, contentView.getLayoutParams());
    }

    /**
     * 设置内容
     *
     * @param contentView
     */
    public void setContentView(View contentView, ViewGroup.LayoutParams layoutParams) {
        this.contentView = contentView;
        FrameLayout frameLayout = buildFrameLayout();
        if (null == layoutParams) {
            layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        frameLayout.addView(contentView, layoutParams);
        popupWindow.setContentView(frameLayout);
    }

    /**
     * 构建蒙层
     *
     * @return
     */
    protected FrameLayout buildFrameLayout() {
        FrameLayout frameLayout = new FrameLayout(context);
        if (null != onClickListener) {
            frameLayout.setOnClickListener(onClickListener);
        } else {
            // 默认点击促发dismiss
            frameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        maskView = new View(context);
        maskView.setBackgroundResource(R.color.trip_hplus_mongoliapopupwindow_popup_window_bg);
        frameLayout.addView(maskView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return frameLayout;
    }

    /**
     * 设置是否能点击弹窗外部区域
     * 设置为true时，点击外部，弹窗消失，并且外部区域消费事件，此时问题是点击back不会销毁弹窗
     * 通过设置setFocusable实现
     *
     * @param isCanClickOutSide
     */
    public void setClickOutSide(boolean isCanClickOutSide) {
        popupWindow.setFocusable(!isCanClickOutSide);
    }

    /**
     * 在指定锚点view上方显示
     *
     * @param anchorView
     */
    public void showTop(View anchorView) {
        showTop(anchorView,
                AnimationUtils.loadAnimation(context, R.anim.trip_hplus_mongoliapopupwindow_top_popup_window_in),
                AnimationUtils.loadAnimation(context, R.anim.trip_hplus_mongoliapopupwindow_top_popup_window_out));
    }

    /**
     * 在指定锚点view上方显示
     *
     * @param anchorView
     */
    public void showTop(View anchorView, Animation inAnim, Animation outAnim) {
        if (isShowing()) {
            dismiss();
            return;
        }

        this.outAnim = outAnim;

        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        int height = getLocationYOnScreen(anchorView) - getStatusBarHeight(anchorView);
        popupWindow.setHeight(height);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        if (null != inAnim) {
            contentView.startAnimation(inAnim);
        }
        if (maskView != null && maskFadeIn != null) {
            maskView.startAnimation(maskFadeIn);
        }

        popupWindow.showAtLocation(anchorView, Gravity.TOP, 0, 0);
    }

    /**
     * 在指定锚点view下方显示,contentView的宽度必须是MATCH_PARENT
     *
     * @param anchorView
     */
    public void showBottom(View anchorView) {
        showBottom(anchorView,
                AnimationUtils.loadAnimation(context, R.anim.trip_hplus_mongoliapopupwindow_bottom_popup_window_in),
                AnimationUtils.loadAnimation(context, R.anim.trip_hplus_mongoliapopupwindow_bottom_popup_window_out));
    }

    /**
     * 在指定锚点view下方显示,contentView的宽度必须是MATCH_PARENT
     *
     * @param anchorView
     */
    public void showBottom(View anchorView, Animation inAnim, Animation outAnim) {
        if (isShowing()) {
            dismiss();
            return;
        }

        this.outAnim = outAnim;

        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        int height = getDisplayFrameBottom(anchorView) - getLocationYOnScreen(anchorView) - anchorView.getHeight();
        popupWindow.setHeight(height);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        if (null != inAnim) {
            contentView.startAnimation(inAnim);
        }
        if (maskView != null && maskFadeIn != null) {
            maskView.startAnimation(maskFadeIn);
        }
        popupWindow.showAtLocation(anchorView, Gravity.BOTTOM, 0, 0);
    }

    /**
     * 显示在正中，阴影
     *
     * @param view 传入当前界面任意一个view
     */
    public void show(View view) {
        show(view,
                AnimationUtils.loadAnimation(context, R.anim.trip_hplus_mongoliapopupwindow_center_popup_window_in),
                AnimationUtils.loadAnimation(context, R.anim.trip_hplus_mongoliapopupwindow_center_popup_window_out));
    }

    /**
     * 显示在正中，阴影
     *
     * @param view 传入当前界面任意一个view
     */
    public void show(View view, Animation inAnim, Animation outAnim) {
        if (isShowing()) {
            dismiss();
            return;
        }

        this.outAnim = outAnim;

        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        if (null != inAnim) {
            contentView.startAnimation(inAnim);
        }
        if (maskView != null && maskFadeIn != null) {
            maskView.startAnimation(maskFadeIn);
        }

        popupWindow.showAtLocation(view, Gravity.TOP, 0, 0);
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * 获取view在屏幕中的y坐标
     *
     * @param view
     * @return
     */
    protected int getLocationYOnScreen(View view) {
        final int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);
        return screenLocation[1];
    }

    /**
     * 获取状态栏高度
     *
     * @param view
     * @return
     */
    protected int getStatusBarHeight(View view) {
        final Rect displayFrame = new Rect();
        view.getWindowVisibleDisplayFrame(displayFrame);
        return displayFrame.top;
    }

    /**
     * 获取当前屏幕底部坐标
     *
     * @param view
     * @return
     */
    protected int getDisplayFrameBottom(View view) {
        final Rect displayFrame = new Rect();
        view.getWindowVisibleDisplayFrame(displayFrame);
        return displayFrame.bottom;
    }

    protected Animation setUpFadeIn() {
        return AnimationUtils.loadAnimation(context, R.anim.trip_hplus_mongoliapopupwindow_top_popup_window_fade_in);
    }

    protected Animation setUpFadeOut() {
        return AnimationUtils.loadAnimation(context, R.anim.trip_hplus_mongoliapopupwindow_top_popup_window_fade_out);
    }
}
