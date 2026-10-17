package com.shuqiang.captain.xhs.playback;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.TransferListener;
import androidx.media3.exoplayer.source.MediaSource;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.playback.cache.PreviewPlaybackCache;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.exo2.ExoMediaSourceInterceptListener;
import tv.danmaku.ijk.media.exo2.ExoSourceManager;

/** 预览页唯一播放接线；缓存初始化在工作线程，页面不感知 Media3 或缓存细节。 */
@androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
public final class PreviewVideoPlayback implements AutoCloseable {
    private static final String TAG = "PreviewPlayback";
    private static final ExecutorService CACHE_INIT = Executors.newSingleThreadExecutor();
    private final Activity activity;
    private final String playTag = "CaptainPreview:" + UUID.randomUUID();
    private final SparseArray<Binding> bindings = new SparseArray<>();
    private PreviewPlaybackCache.Session session;
    private int activePosition = -1;
    private boolean paused;
    private boolean closed;
    private boolean initializationFailed;

    public PreviewVideoPlayback(Activity activity) {
        this.activity = activity;
        installMedia3(activity.getApplicationContext());
        Handler main = new Handler(Looper.getMainLooper());
        CACHE_INIT.execute(() -> {
            PreviewPlaybackCache.Session created = null;
            try { created = PreviewPlaybackCache.open(activity.getApplicationContext()); }
            catch (IOException error) { Log.w(TAG, "cache initialization failed, reason=" + error.getClass().getSimpleName()); }
            PreviewPlaybackCache.Session result = created;
            main.post(() -> {
                if (closed) { if (result != null) result.close(); return; }
                session = result;
                initializationFailed = result == null;
                for (int i = 0; i < bindings.size(); i++) configure(bindings.valueAt(i));
                if (initializationFailed) showCacheFailure();
            });
        });
    }

    private static synchronized void installMedia3(android.content.Context appContext) {
        Debuger.disable();
        PlayerFactory.setPlayManager(Exo2PlayerManager.class);
        GSYVideoManager.instance().setPlayerInitSuccessListener((mediaPlayer, model) -> {
            if (mediaPlayer instanceof tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer) {
                GSYVideoManager manager = GSYVideoManager.instance();
                ((tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer) mediaPlayer).setRendererFactory(
                        new PreviewRenderersFactory(appContext, () ->
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    if (manager.getPlayer() != null
                                            && manager.getPlayer().getMediaPlayer() == mediaPlayer
                                            && manager.listener() != null) {
                                        manager.listener().onInfo(android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START, 0);
                                    }
                                })));
            }
        });
        // 只使用会话管理的缓存；GSY 内置 cacheWithPlay 关闭，避免第二个缓存 owner。
        ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
            @Override public MediaSource getMediaSource(String url, boolean preview, boolean cache,
                                                        boolean looping, File directory) {
                return PreviewPlaybackCache.playbackSource(url, directory);
            }
            @Override public DataSource.Factory getHttpDataSourceFactory(String userAgent,
                    TransferListener listener, int connectTimeout, int readTimeout,
                    Map<String, String> headers, boolean allowRedirects) { return null; }
            @Override public androidx.media3.datasource.DataSink.Factory cacheWriteDataSinkFactory(
                    String cachePath, String url) { return null; }
        });
    }

    public void bind(int position, CaptainVideoPlayer player, XhsMediaItem item,
                     Map<String, String> headers, View loading, Runnable firstFrame) {
        Binding binding = new Binding(position, player, item, headers, loading, firstFrame);
        bindings.put(position, binding);
        if (session != null || initializationFailed) configure(binding);
    }

    private void configure(Binding binding) {
        if (session == null) { binding.loading.setVisibility(View.GONE); return; }
        try {
            if (!session.register(binding.item, binding.headers)) {
                throw new IllegalArgumentException("当前资源无法绑定浏览会话");
            }
            CaptainVideoPlayer player = binding.player;
            player.setPlayTag(playTag);
            player.setPlayPosition(binding.position);
            // Keep the completed item and its cache available for the Save action.
            player.setLooping(false);
            player.setFirstFrameCallback(() -> {
                binding.firstFrame.run();
                if (!binding.reportedFirstFrame) {
                    binding.reportedFirstFrame = true;
                    Log.i(TAG, "first frame, index=" + binding.position + ", elapsedMs="
                            + (android.os.SystemClock.elapsedRealtime() - binding.startMs));
                }
            });
            player.getBackButton().setVisibility(View.GONE);
            player.getTitleTextView().setVisibility(View.GONE);
            player.getFullscreenButton().setOnClickListener(view ->
                    player.startWindowFullscreen(activity, true, true));
            player.setVideoAllCallBack(new GSYSampleCallBack() {
                @Override public void onPlayError(String url, Object... objects) {
                    binding.loading.setVisibility(View.GONE);
                    Log.w(TAG, "play failed, index=" + binding.position
                            + ", transport=" + binding.item.getTransport() + ", reason=player_error");
                    Toast.makeText(activity, "播放失败，可返回网页重试", Toast.LENGTH_SHORT).show();
                }
            });
            player.setUp(binding.item.getMediaUrl(), false, session.getDirectory(), binding.headers, "");
            binding.configured = true;
            if (binding.position == activePosition && !paused) play(binding);
        } catch (RuntimeException error) {
            binding.loading.setVisibility(View.GONE);
            Log.w(TAG, "bind failed, index=" + binding.position + ", reason=" + error.getClass().getSimpleName());
            Toast.makeText(activity, "当前视频无法播放，请返回网页重新查看", Toast.LENGTH_SHORT).show();
        }
    }

    public void select(int position) {
        pause();
        activePosition = position;
        paused = false;
        Binding binding = bindings.get(position);
        if (binding != null && binding.configured) play(binding);
    }

    private void play(Binding binding) {
        CaptainVideoPlayer player = (CaptainVideoPlayer) binding.player.getCurrentPlayer();
        player.setStartAfterPrepared(true);
        if (!ownsPlayer(binding) || player.getCurrentState() == CaptainVideoPlayer.CURRENT_STATE_NORMAL
                || player.getCurrentState() == CaptainVideoPlayer.CURRENT_STATE_ERROR) {
            player.setSeekOnStart(binding.resumePosition);
            binding.startMs = android.os.SystemClock.elapsedRealtime();
            binding.reportedFirstFrame = false;
            player.startPlayLogic();
        } else if (player.getCurrentState() == CaptainVideoPlayer.CURRENT_STATE_PAUSE
                || player.getCurrentState() == CaptainVideoPlayer.CURRENT_STATE_PREPAREING) {
            player.onVideoResume(false);
        }
    }

    public void pause() {
        paused = true;
        for (int i = 0; i < bindings.size(); i++) {
            Binding binding = bindings.valueAt(i);
            CaptainVideoPlayer player = (CaptainVideoPlayer) binding.player.getCurrentPlayer();
            if (ownsPlayer(binding)) binding.resumePosition = player.getCurrentPositionWhenPlaying();
            player.setStartAfterPrepared(false);
        }
        if (playTag.equals(GSYVideoManager.instance().getPlayTag())) GSYVideoManager.onPause();
    }

    public boolean exitFullscreen() { return GSYVideoManager.backFromWindowFull(activity); }

    /** Service 启动前预留引用，防止用户立即返回时缓存被清理。 */
    public String reserveSave(int position) {
        Binding binding = bindings.get(position);
        if (session != null && binding != null
                && PreviewPlaybackCache.reserveExport(session.getId(), binding.item.getId())) return session.getId();
        return null;
    }

    public void cancelSaveReservation(String id) { PreviewPlaybackCache.cancelExportReservation(id); }

    public void remove(int position) {
        Binding binding = bindings.get(position);
        if (binding != null && ownsPlayer(binding)) {
            GSYVideoManager.releaseAllVideos();
        }
        bindings.remove(position);
    }

    /** GSY 共用一个底层播放器；翻回旧页必须重新绑定，不能误恢复另一页的视频。 */
    private boolean ownsPlayer(Binding binding) {
        return playTag.equals(GSYVideoManager.instance().getPlayTag())
                && GSYVideoManager.instance().getPlayPosition() == binding.position;
    }

    private void showCacheFailure() {
        Toast.makeText(activity, "无法创建播放缓存，请检查可用空间后重新预览", Toast.LENGTH_LONG).show();
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        if (playTag.equals(GSYVideoManager.instance().getPlayTag())) GSYVideoManager.releaseAllVideos();
        bindings.clear();
        if (session != null) session.close();
    }

    private static final class Binding {
        final int position;
        final CaptainVideoPlayer player;
        final XhsMediaItem item;
        final Map<String, String> headers;
        final View loading;
        final Runnable firstFrame;
        boolean configured;
        long startMs;
        boolean reportedFirstFrame;
        long resumePosition;
        Binding(int position, CaptainVideoPlayer player, XhsMediaItem item, Map<String, String> headers,
                View loading, Runnable firstFrame) {
            this.position = position; this.player = player; this.item = item;
            this.headers = headers; this.loading = loading; this.firstFrame = firstFrame;
        }
    }
}
