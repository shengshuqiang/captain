package com.captain.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

public class LoadingView extends LinearLayout {
    public static final int DEFAULT_ANIM_DURATION_MILLS = 1000;
    private View mLeft;
    private View mRight;
    private int mOffset;
    private LoadingView.TranslateXReverseAnimation mTranslateXReverseAnimationLeft;
    private LoadingView.TranslateXReverseAnimation mTranslateXReverseAnimationRight;
    private int mAnimDurationMills = 1000;

    public LoadingView(Context context) {
        super(context);
        this.init(context, (AttributeSet)null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    @TargetApi(21)
    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init(context, attrs);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        setVisibility(GONE);
        mAnimDurationMills = DEFAULT_ANIM_DURATION_MILLS;
        setOrientation(HORIZONTAL);
        int size = DensityUtil.dip2px(this.getContext(), 8.0F);
        int distance = DensityUtil.dip2px(this.getContext(), 10.0F);
        this.mOffset = size + distance;
        this.mLeft = new TextView(this.getContext());
        LayoutParams lp = new LayoutParams(size, size);
        lp.gravity = 17;
        this.mLeft.setLayoutParams(lp);
        this.mLeft.setBackgroundResource(R.drawable.loading_dot);
        this.mLeft.setSelected(false);
        this.mRight = new TextView(this.getContext());
        LayoutParams params = new LayoutParams(size, size);
        params.leftMargin = distance;
        params.gravity = 17;
        this.mRight.setLayoutParams(params);
        this.mRight.setBackgroundResource(R.drawable.loading_dot);
        this.mRight.setSelected(true);
        this.addView(this.mLeft);
        this.addView(this.mRight);
    }

    public void startAnimation() {
        setVisibility(VISIBLE);
        if (this.mTranslateXReverseAnimationLeft == null) {
            this.mTranslateXReverseAnimationLeft = new TranslateXReverseAnimation(this.mOffset);
            this.mTranslateXReverseAnimationLeft.setRepeatCount(-1);
            this.mTranslateXReverseAnimationLeft.setRepeatMode(1);
            this.mTranslateXReverseAnimationLeft.setDuration(TimeUnit.MILLISECONDS.toMillis((long)this.mAnimDurationMills));
            this.mTranslateXReverseAnimationRight = new TranslateXReverseAnimation(-this.mOffset);
            this.mTranslateXReverseAnimationRight.setRepeatCount(-1);
            this.mTranslateXReverseAnimationRight.setRepeatMode(1);
            this.mTranslateXReverseAnimationRight.setDuration(TimeUnit.MILLISECONDS.toMillis((long)this.mAnimDurationMills));
        }

        this.mLeft.startAnimation(this.mTranslateXReverseAnimationLeft);
        this.mRight.startAnimation(this.mTranslateXReverseAnimationRight);
    }

    public void stopAnimation() {
        setVisibility(GONE);
        this.mLeft.clearAnimation();
        this.mRight.clearAnimation();
    }

    public void updateColorWithe() {
        if (this.mLeft != null && this.mRight != null) {
            this.mLeft.setBackgroundResource(R.drawable.loading_dot_white);
            this.mRight.setBackgroundResource(R.drawable.loading_dot_white);
        }

    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.stopAnimation();
    }

    private static class TranslateXReverseAnimation extends Animation {
        private int mDx;

        private TranslateXReverseAnimation(int dx) {
            this.mDx = dx;
        }

        protected void applyTransformation(float interpolatedTime, Transformation transformation) {
            Matrix matrix = transformation.getMatrix();
            matrix.setTranslate(this.linear(this.mDx, interpolatedTime), 0.0F);
        }

        private float linear(int dx, float time) {
            return (double)time > 0.5D ? (float)dx * (1.0F - time) * 2.0F : (float)dx * time * 2.0F;
        }
    }
}
