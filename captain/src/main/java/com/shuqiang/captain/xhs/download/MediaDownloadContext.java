package com.shuqiang.captain.xhs.download;

import android.content.Context;

import com.shuqiang.captain.xhs.storage.XhsMediaPublisher;
import com.shuqiang.captain.xhs.playback.cache.PreviewPlaybackCache;

import java.io.File;
import java.io.IOException;

/**
 * 单项下载的资源所有者：会话、临时目录、阶段和取消检查均在任务结束时统一回收。
 */
public final class MediaDownloadContext {
    public interface ProgressListener {
        void onProgress(DownloadStage stage, int current, int total);
    }

    private final Context appContext;
    private final String noteId;
    private final int selectedIndex;
    private final DownloadHttpSession httpSession;
    private final MediaSourceResolver sourceResolver;
    private final XhsMediaPublisher publisher;
    private final File taskDirectory;
    private final ProgressListener progressListener;
    private final PreviewPlaybackCache.ExportLease playbackCache;
    private volatile DownloadMediaInput activeInput;
    private volatile boolean cancelled;
    private DownloadStage stage = DownloadStage.RESOLVING;

    MediaDownloadContext(Context context, String noteId, XhsDownloadItem item, int selectedIndex,
                         ProgressListener progressListener, DownloadHttpSession httpSession,
                         RuntimeMediaSession runtimeSession) throws IOException {
        this(context, noteId, item, selectedIndex, progressListener, httpSession, runtimeSession, null);
    }

    MediaDownloadContext(Context context, String noteId, XhsDownloadItem item, int selectedIndex,
                         ProgressListener progressListener, DownloadHttpSession httpSession,
                         RuntimeMediaSession runtimeSession,
                         PreviewPlaybackCache.ExportLease playbackCache) throws IOException {
        this.appContext = context.getApplicationContext();
        this.noteId = noteId;
        this.selectedIndex = selectedIndex;
        this.progressListener = progressListener;
        this.playbackCache = playbackCache;
        this.httpSession = httpSession == null ? new DownloadHttpSession() : httpSession;
        this.sourceResolver = new MediaSourceResolver(this.httpSession, runtimeSession);
        this.publisher = new XhsMediaPublisher();
        this.taskDirectory = new File(context.getCacheDir(), "captain_media/" + sanitize(item.getId()));
        if (!taskDirectory.exists() && !taskDirectory.mkdirs()) {
            throw new IOException("创建临时目录失败");
        }
    }

    public void transition(DownloadStage next, int current, int total) throws IOException {
        throwIfCancelled();
        stage = next;
        if (progressListener != null) {
            progressListener.onProgress(next, current, total);
        }
    }

    public void throwIfCancelled() throws IOException {
        if (cancelled || Thread.currentThread().isInterrupted()) {
            throw new DownloadCancelledException();
        }
    }

    public void cancel() {
        cancelled = true;
        httpSession.cancel();
        DownloadMediaInput input = activeInput;
        if (input != null) {
            try { input.close(); } catch (IOException ignored) { }
        }
    }

    /** 只有媒体字节可读写缓存，清单及密钥继续使用原来的内存 HTTP 路径。 */
    public DownloadMediaInput openMedia(ResolvedMediaSource source, String url, boolean cacheable)
            throws IOException {
        throwIfCancelled();
        DownloadMediaInput input = DownloadMediaInput.open(httpSession, source, url,
                cacheable ? playbackCache : null);
        activeInput = input;
        if (cancelled) {
            input.close();
            throw new DownloadCancelledException();
        }
        return input;
    }

    public Context getAppContext() {
        return appContext;
    }

    public String getNoteId() {
        return noteId;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public DownloadHttpSession getHttpSession() {
        return httpSession;
    }

    public MediaSourceResolver getSourceResolver() {
        return sourceResolver;
    }

    public XhsMediaPublisher getPublisher() {
        return publisher;
    }

    public File file(String name) {
        return new File(taskDirectory, name);
    }

    public DownloadStage getStage() {
        return stage;
    }

    public void cleanUp() {
        DownloadMediaInput input = activeInput;
        activeInput = null;
        if (input != null) {
            try { input.close(); } catch (IOException ignored) { }
        }
        deleteRecursively(taskDirectory);
        File parent = taskDirectory.getParentFile();
        if (parent != null) {
            parent.delete();
        }
    }

    private void deleteRecursively(File target) {
        if (target == null || !target.exists()) {
            return;
        }
        File[] children = target.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        target.delete();
    }

    private String sanitize(String value) {
        String safe = value == null ? "media" : value.replaceAll("[^A-Za-z0-9_-]", "_");
        return safe.length() > 96 ? safe.substring(0, 96) : safe;
    }
}
