package com.shuqiang.captain.xhs.playback;

import android.app.Activity;
import android.app.Application;
import android.media.MediaPlayer;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/** 不伪造设备播放结果，只验证 GSY 反射构造、全屏状态复制和首帧交互合同。 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Application.class)
public class CaptainVideoPlayerTest {
    @Test public void fullscreenPreservesSpeedAndReturnsToOriginalPlayer() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        FrameLayout root = new FrameLayout(activity);
        activity.setContentView(root);
        CaptainVideoPlayer player = new CaptainVideoPlayer(activity);
        root.addView(player);
        player.setUp("https://example.com/video.mp4", false, "");
        player.setSpeed(1.5f);
        GSYBaseVideoPlayer fullscreen = player.startWindowFullscreen(activity, true, true);
        assertTrue(fullscreen instanceof CaptainVideoPlayer);
        assertEquals(1.5f, fullscreen.getSpeed(), 0f);
        assertTrue(fullscreen.isNeedLockFull());
        assertFalse(player.isRotateViewAuto());
        assertFalse(player.isTouchWiget());
        assertTrue(fullscreen.isTouchWigetFull());
        assertFalse(player.isNeedShowWifiTip());
        activity.finish();
    }

    @Test public void firstFrameNotificationRunsOnlyForRenderedFrame() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        CaptainVideoPlayer player = new CaptainVideoPlayer(activity);
        player.setThumbImageView(new ImageView(activity));
        AtomicInteger callbacks = new AtomicInteger();
        player.setFirstFrameCallback(callbacks::incrementAndGet);
        player.onInfo(MediaPlayer.MEDIA_INFO_BUFFERING_END, 0);
        assertEquals(0, callbacks.get());
        player.onInfo(MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START, 0);
        assertEquals(1, callbacks.get());
        activity.finish();
    }

    @Test public void preparingAndPausedKeepPosterUntilAFrameIsRendered() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        CaptainVideoPlayer player = new CaptainVideoPlayer(activity);
        player.setThumbImageView(new ImageView(activity));
        android.view.View poster = player.findViewById(com.shuyu.gsyvideoplayer.R.id.thumb);
        player.setStateAndUi(CaptainVideoPlayer.CURRENT_STATE_PREPAREING);
        assertEquals(android.view.View.VISIBLE, poster.getVisibility());
        player.setStateAndUi(CaptainVideoPlayer.CURRENT_STATE_PAUSE);
        assertEquals(android.view.View.VISIBLE, poster.getVisibility());
        player.onInfo(MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START, 0);
        assertEquals(android.view.View.INVISIBLE, poster.getVisibility());
        activity.finish();
    }

    @Test public void fullscreenKeepsItsOwnPosterBeforeFirstFrame() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        FrameLayout root = new FrameLayout(activity);
        activity.setContentView(root);
        CaptainVideoPlayer player = new CaptainVideoPlayer(activity);
        root.addView(player);
        ImageView original = new ImageView(activity);
        original.setImageDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.GREEN));
        player.setThumbImageView(original);
        player.setUp("https://example.com/video.mp4", false, "");
        player.setStateAndUi(CaptainVideoPlayer.CURRENT_STATE_PREPAREING);
        GSYBaseVideoPlayer fullscreen = player.startWindowFullscreen(activity, true, true);
        android.view.ViewGroup thumb = fullscreen.findViewById(com.shuyu.gsyvideoplayer.R.id.thumb);
        assertEquals(android.view.View.VISIBLE, thumb.getVisibility());
        assertEquals(1, thumb.getChildCount());
        assertNotSame(original, thumb.getChildAt(0));
        assertNotNull(((ImageView) thumb.getChildAt(0)).getDrawable());
        assertNotNull(original.getParent());
        activity.finish();
    }
}
