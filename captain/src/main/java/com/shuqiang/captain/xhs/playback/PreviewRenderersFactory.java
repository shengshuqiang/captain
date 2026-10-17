package com.shuqiang.captain.xhs.playback;

import android.content.Context;
import android.os.Handler;

import androidx.media3.common.Format;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.DecoderCounters;
import androidx.media3.exoplayer.DecoderReuseEvaluation;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.audio.AudioRendererEventListener;
import androidx.media3.exoplayer.metadata.MetadataOutput;
import androidx.media3.exoplayer.text.TextOutput;
import androidx.media3.exoplayer.video.VideoRendererEventListener;

/** GSY 14 的 Exo 适配器未转发首帧事件；只补该通知，完整保留 Media3 的原事件链。 */
@androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
final class PreviewRenderersFactory extends DefaultRenderersFactory {
    private final Runnable firstFrame;

    PreviewRenderersFactory(Context context, Runnable firstFrame) {
        super(context);
        this.firstFrame = firstFrame;
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER);
    }

    @Override public Renderer[] createRenderers(Handler handler, VideoRendererEventListener video,
            AudioRendererEventListener audio, TextOutput text, MetadataOutput metadata) {
        return super.createRenderers(handler, forwarding(video, firstFrame), audio, text, metadata);
    }

    static VideoRendererEventListener forwarding(VideoRendererEventListener delegate, Runnable firstFrame) {
        return new VideoRendererEventListener() {
            @Override public void onVideoEnabled(DecoderCounters counters) { delegate.onVideoEnabled(counters); }
            @Override public void onVideoDecoderInitialized(String name, long timestamp, long duration) {
                delegate.onVideoDecoderInitialized(name, timestamp, duration);
            }
            @Override public void onVideoInputFormatChanged(Format format, DecoderReuseEvaluation evaluation) {
                delegate.onVideoInputFormatChanged(format, evaluation);
            }
            @Override public void onDroppedFrames(int count, long elapsed) { delegate.onDroppedFrames(count, elapsed); }
            @Override public void onVideoFrameProcessingOffset(long offset, int count) {
                delegate.onVideoFrameProcessingOffset(offset, count);
            }
            @Override public void onVideoSizeChanged(VideoSize size) { delegate.onVideoSizeChanged(size); }
            @Override public void onRenderedFirstFrame(Object output, long time) {
                delegate.onRenderedFirstFrame(output, time);
                firstFrame.run();
            }
            @Override public void onVideoDecoderReleased(String name) { delegate.onVideoDecoderReleased(name); }
            @Override public void onVideoDisabled(DecoderCounters counters) { delegate.onVideoDisabled(counters); }
            @Override public void onVideoCodecError(Exception error) { delegate.onVideoCodecError(error); }
        };
    }
}
