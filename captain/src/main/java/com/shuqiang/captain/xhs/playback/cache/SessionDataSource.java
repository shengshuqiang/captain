package com.shuqiang.captain.xhs.playback.cache;

import android.net.Uri;

import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.TransferListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** 读取期间持有缓存引用，页面关闭时不会删除播放器或下载器正在读取的文件。 */
@androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
final class SessionDataSource implements DataSource {
    private final PreviewPlaybackCache.Session session;
    private final DataSource delegate;
    private boolean acquired;

    SessionDataSource(PreviewPlaybackCache.Session session, DataSource delegate) {
        this.session = session;
        this.delegate = delegate;
    }

    @Override public void addTransferListener(TransferListener listener) {
        delegate.addTransferListener(listener);
    }

    @Override public long open(DataSpec dataSpec) throws IOException {
        if (acquired) throw new IOException("缓存数据源已打开");
        session.acquireReader();
        acquired = true;
        try {
            return delegate.open(dataSpec);
        } catch (IOException | RuntimeException failure) {
            try { delegate.close(); } catch (IOException ignored) { /* 保留原始请求错误。 */ }
            release();
            throw failure;
        }
    }

    @Override public int read(byte[] buffer, int offset, int length) throws IOException {
        return delegate.read(buffer, offset, length);
    }
    @Override public Uri getUri() { return delegate.getUri(); }
    @Override public Map<String, List<String>> getResponseHeaders() {
        return delegate.getResponseHeaders();
    }
    @Override public void close() throws IOException {
        try { delegate.close(); } finally { release(); }
    }
    private void release() {
        if (acquired) {
            acquired = false;
            session.releaseReader();
        }
    }
}
