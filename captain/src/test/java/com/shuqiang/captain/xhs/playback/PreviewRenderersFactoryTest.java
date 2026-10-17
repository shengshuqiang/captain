package com.shuqiang.captain.xhs.playback;

import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.video.VideoRendererEventListener;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/** 补首帧回调时仍把尺寸、掉帧及错误事件交回 Media3，防止破坏原渲染链。 */
public class PreviewRenderersFactoryTest {
    @Test public void forwardsRendererEventsBeforeNotifyingPreview() {
        List<String> events = new ArrayList<>();
        VideoRendererEventListener delegate = new VideoRendererEventListener() {
            @Override public void onVideoSizeChanged(VideoSize size) { events.add("size"); }
            @Override public void onDroppedFrames(int count, long elapsed) { events.add("dropped"); }
            @Override public void onVideoCodecError(Exception error) { events.add("error"); }
            @Override public void onRenderedFirstFrame(Object output, long time) { events.add("frame"); }
        };
        VideoRendererEventListener forwarding = PreviewRenderersFactory.forwarding(delegate,
                () -> events.add("preview"));
        forwarding.onVideoSizeChanged(new VideoSize(1920, 1080));
        forwarding.onDroppedFrames(2, 100);
        forwarding.onVideoCodecError(new IllegalStateException("decoder"));
        forwarding.onRenderedFirstFrame(new Object(), 42);
        assertEquals(Arrays.asList("size", "dropped", "error", "frame", "preview"), events);
    }
}
