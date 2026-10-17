package com.shuqiang.captain.xhs.playback.cache;

import android.content.Context;
import android.util.Log;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsMediaType;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 本次预览唯一的缓存所有者；播放、异步导出及正在读取的数据源共享引用生命周期。 */
@androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
public final class PreviewPlaybackCache {
    static final long MAX_CACHE_BYTES = 512L * 1024 * 1024;
    static final String CACHE_DIRECTORY = "preview-playback";
    private static final String TAG = "PreviewPlaybackCache";
    private static final Object LOCK = new Object();
    private static final Map<String, Session> SESSIONS = new HashMap<>();
    private static final ExecutorService CLEANUP = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "preview-cache-cleanup");
        thread.setDaemon(true);
        return thread;
    });

    private PreviewPlaybackCache() { }

    /** 不持有 Activity；SimpleCache 在内部线程初始化索引，清理也不占用主线程。 */
    public static Session open(Context context) throws IOException {
        File root = new File(context.getApplicationContext().getCacheDir(), CACHE_DIRECTORY);
        Session session;
        synchronized (LOCK) {
            session = new Session(new File(root, UUID.randomUUID().toString()));
            SESSIONS.put(session.id, session);
        }
        CLEANUP.execute(() -> removeOrphans(root));
        Log.i(TAG, "event=open session=" + session.id);
        return session;
    }

    /** GSY 的 MediaSource 拦截器按缓存目录定位会话；不属于本预览时交回默认处理。 */
    public static MediaSource playbackSource(String url, File cacheDir) {
        if (url == null || cacheDir == null) return null;
        Session session;
        PreviewNetworkSource source;
        synchronized (LOCK) {
            session = SESSIONS.get(cacheDir.getName());
            if (session == null || session.closed || !session.directory.equals(cacheDir)) return null;
            source = session.sourcesByUrl.get(url);
            if (source == null) return null;
        }
        DataSource.Factory upstream = source.factory(session.network);
        MediaItem media = MediaItem.fromUri(url);
        if (source.hls) {
            return new HlsMediaSource.Factory(type -> session.hlsDataSource(type, upstream))
                    .createMediaSource(media);
        }
        return new ProgressiveMediaSource.Factory(session.cachedFactory(upstream))
                .createMediaSource(media);
    }

    /** 在启动 Service 之前预留引用，避免页面立即退出导致缓存先被删。 */
    public static boolean reserveExport(String sessionId, String itemId) {
        synchronized (LOCK) {
            Session session = SESSIONS.get(sessionId);
            if (session == null || session.closed || session.exportItem != null
                    || !session.items.containsKey(itemId)) return false;
            session.exportItem = itemId;
            session.reserved = true;
            session.references++;
            return true;
        }
    }

    /** Service 启动失败时撤销尚未领取的引用；不会取消已在执行的导出。 */
    public static void cancelExportReservation(String sessionId) {
        synchronized (LOCK) {
            Session session = SESSIONS.get(sessionId);
            if (session != null && session.reserved) {
                session.reserved = false;
                session.exportItem = null;
                releaseLocked(session);
            }
        }
    }

    /** 领取一次性预留；校验资源身份，不能用另一项资源消费本次缓存交接。 */
    public static ExportLease claimExport(String sessionId, String itemId) {
        synchronized (LOCK) {
            Session session = SESSIONS.get(sessionId);
            if (session == null || !session.reserved || !itemIdEquals(session.exportItem, itemId)
                    || !session.items.containsKey(itemId)) return null;
            session.reserved = false;
            return new ExportLease(session);
        }
    }

    private static boolean itemIdEquals(String expected, String actual) {
        return expected != null && expected.equals(actual);
    }

    /** 完整地址参与摘要，签名变化自然未命中；索引不记录源 URL 或调用方的 key。 */
    static String cacheKey(String sessionId, String url) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest((sessionId + "\n" + url).getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) hash.append(String.format(java.util.Locale.ROOT, "%02x", value & 0xff));
            return hash.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 不可用", impossible);
        }
    }

    private static void releaseLocked(Session session) {
        if (--session.references != 0) return;
        SESSIONS.remove(session.id);
        CLEANUP.execute(() -> {
            session.cache.release();
            session.network.dispatcher().cancelAll();
            session.network.connectionPool().evictAll();
            boolean deleted = deleteTree(session.directory);
            Log.i(TAG, "event=release session=" + session.id + " deleted=" + deleted);
        });
    }

    /** 只在下次打开时扫描孤儿，跳过本进程活动缓存，绝不与 SimpleCache 的锁竞争。 */
    private static void removeOrphans(File root) {
        File[] children = root.listFiles();
        if (children == null) return;
        Set<File> active = new HashSet<>();
        synchronized (LOCK) {
            for (Session value : SESSIONS.values()) active.add(value.directory);
        }
        for (File child : children) {
            if (!active.contains(child) && !SimpleCache.isCacheFolderLocked(child)
                    && !deleteTree(child)) {
                Log.w(TAG, "event=orphan_cleanup_failed reason=delete");
            }
        }
    }

    private static boolean deleteTree(File file) {
        File[] children = file.listFiles();
        boolean success = true;
        if (children != null) for (File child : children) success &= deleteTree(child);
        return (!file.exists() || file.delete()) && success;
    }

    /** 页面持有的一份所有权；注册失败只影响该项，不影响其他预览资源。 */
    public static final class Session implements Closeable {
        private final String id;
        private final File directory;
        final Cache cache;
        private final okhttp3.OkHttpClient network = PreviewNetworkSource.newClient();
        private final Map<String, PreviewNetworkSource> items = new HashMap<>();
        private final Map<String, PreviewNetworkSource> sourcesByUrl = new HashMap<>();
        private int references = 1;
        private boolean closed;
        private String exportItem;
        private boolean reserved;

        @SuppressWarnings("deprecation")
        private Session(File directory) {
            this.directory = directory;
            id = directory.getName();
            // 使用会话目录内的文件索引，避免全局数据库留下已经关闭会话的媒体索引。
            cache = new PrivateMediaCache(new SimpleCache(directory,
                    new LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES)));
        }
        public String getId() { return id; }
        public File getDirectory() { return directory; }

        public boolean register(XhsMediaItem item, Map<String, String> headers) {
            if (item == null || item.getId() == null || item.getMediaType() != XhsMediaType.VIDEO) {
                Log.w(TAG, "event=register_failed reason=invalid_item session=" + id);
                return false;
            }
            PreviewNetworkSource source = PreviewNetworkSource.from(item.getMediaUrl(),
                    item.getTransport() == XhsMediaTransport.HLS_STREAM, headers);
            if (source == null) {
                Log.w(TAG, "event=register_failed reason=invalid_resource session=" + id);
                return false;
            }
            synchronized (LOCK) {
                if (closed) return false;
                items.put(item.getId(), source);
                sourcesByUrl.put(source.url, source);
                return true;
            }
        }

        // HLS 清单、密钥只在内存读取；仅媒体与初始化分片允许持久化。
        DataSource hlsDataSource(int type, DataSource.Factory upstream) {
            return (type == C.DATA_TYPE_MEDIA || type == C.DATA_TYPE_MEDIA_INITIALIZATION
                    ? cachedFactory(upstream) : upstream).createDataSource();
        }

        DataSource.Factory cachedFactory(DataSource.Factory upstream) {
            CacheDataSource.Factory factory = new CacheDataSource.Factory().setCache(cache)
                    .setUpstreamDataSourceFactory(upstream)
                    .setCacheKeyFactory(spec -> cacheKey(id, spec.uri.toString()))
                    // 播放可能暂停并长期持有写锁；保存遇到在途分片直接补齐，不能等待播放关闭。
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
            return () -> new SessionDataSource(this, factory.createDataSource());
        }
        void acquireReader() throws IOException {
            synchronized (LOCK) {
                if (references == 0) throw new IOException("预览缓存已释放");
                references++;
            }
        }
        void releaseReader() {
            synchronized (LOCK) { releaseLocked(this); }
        }
        @Override public void close() {
            synchronized (LOCK) {
                if (closed) return;
                closed = true;
                releaseLocked(this);
            }
        }
    }

    /** 导出独占权；仅包裹直链/媒体分片，调用方须保持密钥和清单使用原始上游。 */
    public static final class ExportLease implements Closeable {
        private final Session session;
        private boolean closed;
        private ExportLease(Session session) { this.session = session; }

        public DataSource.Factory createDataSourceFactory(DataSource.Factory upstream) {
            synchronized (LOCK) {
                if (closed) throw new IllegalStateException("导出缓存已释放");
                return session.cachedFactory(upstream);
            }
        }
        @Override public void close() {
            synchronized (LOCK) {
                if (closed) return;
                closed = true;
                session.exportItem = null;
                releaseLocked(session);
            }
        }
    }
}
