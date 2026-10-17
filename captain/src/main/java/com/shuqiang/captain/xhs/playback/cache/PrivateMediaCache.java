package com.shuqiang.captain.xhs.playback.cache;

import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheSpan;
import androidx.media3.datasource.cache.ContentMetadata;
import androidx.media3.datasource.cache.ContentMetadataMutations;

import java.io.File;
import java.util.NavigableSet;
import java.util.Set;

/** 只保存长度元数据，防止 CacheDataSource 把重定向后的签名地址持久化。 */
@androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
final class PrivateMediaCache implements Cache {
    private final Cache delegate;

    PrivateMediaCache(Cache delegate) {
        this.delegate = delegate;
    }

    @Override public long getUid() { return delegate.getUid(); }
    @Override public void release() { delegate.release(); }
    @Override public NavigableSet<CacheSpan> addListener(String key, Listener listener) {
        return delegate.addListener(key, listener);
    }
    @Override public void removeListener(String key, Listener listener) {
        delegate.removeListener(key, listener);
    }
    @Override public NavigableSet<CacheSpan> getCachedSpans(String key) {
        return delegate.getCachedSpans(key);
    }
    @Override public Set<String> getKeys() { return delegate.getKeys(); }
    @Override public long getCacheSpace() { return delegate.getCacheSpace(); }
    @Override public CacheSpan startReadWrite(String key, long position, long length)
            throws InterruptedException, CacheException {
        return delegate.startReadWrite(key, position, length);
    }
    @Override public CacheSpan startReadWriteNonBlocking(String key, long position, long length)
            throws CacheException {
        return delegate.startReadWriteNonBlocking(key, position, length);
    }
    @Override public File startFile(String key, long position, long length) throws CacheException {
        return delegate.startFile(key, position, length);
    }
    @Override public void commitFile(File file, long length) throws CacheException {
        delegate.commitFile(file, length);
    }
    @Override public void releaseHoleSpan(CacheSpan span) { delegate.releaseHoleSpan(span); }
    @Override public void removeResource(String key) { delegate.removeResource(key); }
    @Override public void removeSpan(CacheSpan span) { delegate.removeSpan(span); }
    @Override public boolean isCached(String key, long position, long length) {
        return delegate.isCached(key, position, length);
    }
    @Override public long getCachedLength(String key, long position, long length) {
        return delegate.getCachedLength(key, position, length);
    }
    @Override public long getCachedBytes(String key, long position, long length) {
        return delegate.getCachedBytes(key, position, length);
    }
    @Override public void applyContentMetadataMutations(String key, ContentMetadataMutations input)
            throws CacheException {
        ContentMetadataMutations safe = new ContentMetadataMutations();
        if (input.getRemovedValues().contains(ContentMetadata.KEY_CONTENT_LENGTH)) {
            safe.remove(ContentMetadata.KEY_CONTENT_LENGTH);
        }
        Object length = input.getEditedValues().get(ContentMetadata.KEY_CONTENT_LENGTH);
        if (length instanceof Long) safe.set(ContentMetadata.KEY_CONTENT_LENGTH, (Long) length);
        safe.remove(ContentMetadata.KEY_REDIRECTED_URI);
        delegate.applyContentMetadataMutations(key, safe);
    }
    @Override public ContentMetadata getContentMetadata(String key) {
        return delegate.getContentMetadata(key);
    }
}
