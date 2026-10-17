package com.shuqiang.captain.xhs.playback;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

/** 只补充船长需要的倍速入口和首帧封面；播放状态及全屏切换仍归 GSY。 */
public final class CaptainVideoPlayer extends StandardGSYVideoPlayer {
    private static final float[] SPEEDS = {0.5f, 1f, 1.25f, 1.5f, 2f};
    private static final String[] SPEED_LABELS = {"0.5×", "1×", "1.25×", "1.5×", "2×"};
    private static final int MIN_TOUCH_DP = 44;
    private static final int DEFAULT_SPEED_INDEX = 1;
    private static final String SPEED_TITLE = "播放速度";
    private TextView speedView;
    private boolean firstFrameRendered;
    private Runnable firstFrameCallback;
    private GlideUrl posterSource;

    // GSY 通过这两个构造方法反射创建全屏视图，必须保留。
    public CaptainVideoPlayer(Context context) { super(context); configureDefaults(); }
    public CaptainVideoPlayer(Context context, Boolean fullFlag) { super(context, fullFlag); configureDefaults(); }
    public CaptainVideoPlayer(Context context, AttributeSet attrs) { super(context, attrs); configureDefaults(); }

    @Override protected void init(Context context) {
        super.init(context);
        speedView = new TextView(context);
        speedView.setTextColor(Color.WHITE);
        speedView.setGravity(Gravity.CENTER);
        int touchSize = (int) (MIN_TOUCH_DP * getResources().getDisplayMetrics().density + 0.5f);
        speedView.setMinWidth(touchSize);
        speedView.setMinHeight(touchSize);
        speedView.setContentDescription(SPEED_TITLE);
        speedView.setText(SPEED_LABELS[DEFAULT_SPEED_INDEX]);
        mBottomContainer.addView(speedView, Math.max(0, mBottomContainer.getChildCount() - 1),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        speedView.setOnClickListener(view -> showSpeedChoices());
    }

    /** 必须在 super 构造完成后设置，GSY 父类的字段初始值会覆盖 init 中的配置。 */
    private void configureDefaults() {
        setIsTouchWiget(false);
        setIsTouchWigetFull(true);
        setNeedLockFull(true);
        setRotateViewAuto(false);
        setNeedShowWifiTip(false);
    }

    private void showSpeedChoices() {
        int current = speedIndex();
        new AlertDialog.Builder(getContext()).setTitle(SPEED_TITLE)
                .setSingleChoiceItems(SPEED_LABELS, current, (dialog, which) -> {
                    setSpeed(SPEEDS[which], false, true);
                    refreshSpeedLabel();
                    dialog.dismiss();
                }).setNegativeButton("取消", null).show();
    }

    private int speedIndex() {
        for (int i = 0; i < SPEEDS.length; i++) if (SPEEDS[i] == mSpeed) return i;
        return DEFAULT_SPEED_INDEX;
    }

    private void refreshSpeedLabel() { speedView.setText(SPEED_LABELS[speedIndex()]); }

    public void setFirstFrameCallback(Runnable callback) { firstFrameCallback = callback; }

    /** 全屏视图单独绑定同一封面请求，不搬走原页 View，也不丢失尚未完成的图片加载。 */
    public void setPreviewPoster(GlideUrl source) {
        posterSource = source;
        ImageView poster = new ImageView(getContext());
        poster.setScaleType(ImageView.ScaleType.FIT_CENTER);
        setThumbImageView(poster);
        if (source != null) Glide.with(poster).load(source).into(poster);
        mThumbImageViewLayout.setVisibility(firstFrameRendered ? View.INVISIBLE : View.VISIBLE);
    }

    @Override public void onInfo(int what, int extra) {
        super.onInfo(what, extra);
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            firstFrameRendered = true;
            mThumbImageViewLayout.setVisibility(View.INVISIBLE);
            if (firstFrameCallback != null) firstFrameCallback.run();
        }
    }

    @Override protected void setStateAndUi(int state) {
        if (state == CURRENT_STATE_PREPAREING) firstFrameRendered = false;
        super.setStateAndUi(state);
    }

    @Override protected void setViewShowState(View view, int visibility) {
        // 准备、暂停和隐藏控制条都不能提前撤掉封面，只有真正渲染首帧才能撤。
        super.setViewShowState(view, view != null && view == mThumbImageViewLayout && !firstFrameRendered
                ? View.VISIBLE : visibility);
    }

    @Override protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        CaptainVideoPlayer source = (CaptainVideoPlayer) from;
        CaptainVideoPlayer target = (CaptainVideoPlayer) to;
        target.firstFrameRendered = source.firstFrameRendered;
        super.cloneParams(from, to);
        target.firstFrameCallback = source.firstFrameCallback;
        target.setPreviewPoster(source.posterSource);
        if (source.posterSource == null && source.mThumbImageView instanceof ImageView) {
            ((ImageView) target.mThumbImageView).setImageDrawable(((ImageView) source.mThumbImageView).getDrawable());
        }
        target.refreshSpeedLabel();
    }
}
