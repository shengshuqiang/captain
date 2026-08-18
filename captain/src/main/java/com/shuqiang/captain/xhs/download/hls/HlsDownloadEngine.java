package com.shuqiang.captain.xhs.download.hls;

import android.util.Log;

import com.shuqiang.captain.xhs.download.DirectMediaDownloadEngine;
import com.shuqiang.captain.xhs.download.DownloadHttpSession;
import com.shuqiang.captain.xhs.download.DownloadStage;
import com.shuqiang.captain.xhs.download.MediaDownloadContext;
import com.shuqiang.captain.xhs.download.MediaDownloadEngine;
import com.shuqiang.captain.xhs.download.ResolvedMediaSource;
import com.shuqiang.captain.xhs.download.XhsDownloadItem;
import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsSaveItemResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

import okhttp3.Response;

/**
 * VOD HLS 下载引擎：能力预检后串行拉取 TS，按需 AES-128 解密，再无损转封装并发布。
 */
public final class HlsDownloadEngine implements MediaDownloadEngine {
    private static final String TAG = "HlsDownloadEngine";
    private static final int MAX_SOURCE_REFRESH = 2;
    private static final int MAX_REQUEST_ATTEMPTS = 3;
    private static final int MAX_SEGMENT_BYTES = 64 * 1024 * 1024;

    private final HlsPlaylistParser playlistParser = new HlsPlaylistParser();
    private final HlsSupportValidator supportValidator = new HlsSupportValidator();
    private final HlsSegmentCrypto segmentCrypto = new HlsSegmentCrypto();
    private final SystemHlsRemuxer remuxer = new SystemHlsRemuxer();
    private final DirectMediaDownloadEngine directEngine;

    public HlsDownloadEngine(DirectMediaDownloadEngine directEngine) {
        this.directEngine = directEngine;
    }

    @Override
    public boolean supports(XhsDownloadItem item) {
        return item.getTransport() == XhsMediaTransport.HLS_STREAM;
    }

    @Override
    public XhsSaveItemResult download(MediaDownloadContext context, XhsDownloadItem item) throws IOException {
        Log.i(TAG, "hls task started, itemId=" + item.getId() + ", quality=" + item.getQualityHeight());
        IOException lastFailure = null;
        for (int refresh = 0; refresh <= MAX_SOURCE_REFRESH; refresh++) {
            try {
                context.transition(DownloadStage.RESOLVING, refresh, MAX_SOURCE_REFRESH);
                ResolvedMediaSource source = context.getSourceResolver().resolve(item);
                XhsSaveItemResult result = downloadResolved(context, item, source);
                Log.i(TAG, "hls task completed, itemId=" + item.getId()
                        + ", quality=" + item.getQualityHeight());
                return result;
            } catch (DownloadHttpSession.HttpStatusException exception) {
                lastFailure = exception;
                if (item.requiresRuntimeSession() && exception.indicatesExpiredSource()) {
                    throw new IOException("浏览会话已失效，请重新验证并提取");
                }
                if (!exception.indicatesExpiredSource() || refresh >= MAX_SOURCE_REFRESH) {
                    throw exception;
                }
                Log.w(TAG, "hls source expired, itemId=" + item.getId()
                        + ", refresh=" + (refresh + 1) + ", httpCode=" + exception.getStatusCode());
                clearWorkFiles(context);
            }
        }
        throw lastFailure == null ? new IOException("HLS 下载失败") : lastFailure;
    }

    private XhsSaveItemResult downloadResolved(MediaDownloadContext context, XhsDownloadItem item,
                                               ResolvedMediaSource source) throws IOException {
        context.transition(DownloadStage.DOWNLOADING_PLAYLIST, 0, 0);
        HlsPlaylist mediaPlaylist = resolveMediaPlaylist(context, source, item.getQualityHeight());
        supportValidator.validateMediaPlaylist(mediaPlaylist);
        Map<HlsPlaylist.Key, byte[]> keyCache = new IdentityHashMap<>();
        try {
            File probeTs = context.file("probe.ts");
            downloadProbe(context, source, mediaPlaylist, keyCache, probeTs);
            context.transition(DownloadStage.PROBING, 0, 0);
            if (!remuxer.probe(probeTs, context.file("probe.mp4"), item.getQualityHeight())) {
                if (!source.hasDirectFallback()) {
                    throw new IOException("当前系统不支持该 HLS 的无损转封装，且页面没有明确 MP4 回退");
                }
                ResolvedMediaSource fallback = new ResolvedMediaSource(source.getDirectFallbackUrl(),
                        XhsMediaTransport.DIRECT_FILE, source.getUserAgent(), source.getReferer(),
                        source.getDirectFallbackHeight(), null, 0);
                XhsDownloadItem fallbackItem = item.withResolvedTransport(
                        XhsMediaTransport.DIRECT_FILE, source.getDirectFallbackHeight());
                return directEngine.downloadResolved(context, fallbackItem, fallback, true);
            }
            File combinedTs = context.file("combined.ts");
            if (combinedTs.exists() && !combinedTs.delete()) {
                throw new IOException("无法清理旧的 HLS 临时文件");
            }
            downloadAllSegments(context, source, mediaPlaylist, keyCache, combinedTs);
            context.transition(DownloadStage.REMUXING, 0, 0);
            File mp4 = context.file("output.mp4");
            remuxer.remuxAndVerify(combinedTs, mp4, item.getQualityHeight(), mediaPlaylist.getTotalDurationSec());
            context.transition(DownloadStage.VERIFYING, 0, 0);
            context.transition(DownloadStage.PUBLISHING, 0, 0);
            return context.getPublisher().publish(context.getAppContext(), context.getNoteId(), item,
                    context.getSelectedIndex(), mp4);
        } finally {
            wipeKeys(keyCache);
        }
    }

    private HlsPlaylist resolveMediaPlaylist(MediaDownloadContext context, ResolvedMediaSource source,
                                             int expectedHeight) throws IOException {
        String playlistUrl = source.getUrl();
        for (int depth = 0; depth < 3; depth++) {
            String content = readTextWithRetry(context, source, playlistUrl);
            HlsPlaylist playlist = playlistParser.parse(playlistUrl, content);
            if (playlist.getType() == HlsPlaylist.Type.MEDIA) {
                return playlist;
            }
            HlsPlaylist.Variant selected = supportValidator.selectVariant(playlist, expectedHeight);
            playlistUrl = selected.getUrl();
        }
        throw new IOException("HLS 清单嵌套层级过深");
    }

    private String readTextWithRetry(MediaDownloadContext context, ResolvedMediaSource source,
                                     String playlistUrl) throws IOException {
        IOException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_REQUEST_ATTEMPTS; attempt++) {
            context.throwIfCancelled();
            try {
                return context.getHttpSession().readText(playlistUrl, source.getUserAgent(), source.getReferer(),
                        "application/vnd.apple.mpegurl,application/x-mpegURL,text/plain,*/*");
            } catch (DownloadHttpSession.HttpStatusException exception) {
                if (exception.indicatesExpiredSource()) {
                    throw exception;
                }
                lastFailure = exception;
            } catch (IOException exception) {
                lastFailure = exception;
            }
            Log.w(TAG, "playlist retry, attempt=" + attempt
                    + ", error=" + lastFailure.getClass().getSimpleName());
        }
        throw lastFailure == null ? new IOException("HLS 清单下载失败") : lastFailure;
    }

    private void downloadProbe(MediaDownloadContext context, ResolvedMediaSource source,
                               HlsPlaylist playlist, Map<HlsPlaylist.Key, byte[]> keyCache,
                               File probe) throws IOException {
        if (probe.exists() && !probe.delete()) {
            throw new IOException("无法清理 HLS 能力探测文件");
        }
        int probeCount = Math.min(2, playlist.getSegments().size());
        try (FileOutputStream output = new FileOutputStream(probe, false)) {
            for (int index = 0; index < probeCount; index++) {
                byte[] bytes = downloadSegment(context, source, playlist.getSegments().get(index), keyCache, index);
                output.write(bytes);
                Arrays.fill(bytes, (byte) 0);
            }
        }
    }

    private void downloadAllSegments(MediaDownloadContext context, ResolvedMediaSource source,
                                     HlsPlaylist playlist, Map<HlsPlaylist.Key, byte[]> keyCache,
                                     File combinedTs) throws IOException {
        int total = playlist.getSegments().size();
        try (FileOutputStream output = new FileOutputStream(combinedTs, false)) {
            for (int index = 0; index < total; index++) {
                context.transition(DownloadStage.DOWNLOADING_SEGMENTS, index, total);
                HlsPlaylist.Segment segment = playlist.getSegments().get(index);
                byte[] bytes = downloadSegment(context, source, segment, keyCache, index);
                output.write(bytes);
                Arrays.fill(bytes, (byte) 0);
            }
            output.flush();
        }
    }

    private byte[] downloadSegment(MediaDownloadContext context, ResolvedMediaSource source,
                                   HlsPlaylist.Segment segment, Map<HlsPlaylist.Key, byte[]> keyCache,
                                   int segmentIndex) throws IOException {
        byte[] encrypted = downloadBytesWithRetry(context, source, segment.getUrl(), segmentIndex);
        if (segment.getKey() == null) {
            validateTransportStream(encrypted);
            return encrypted;
        }
        context.transition(DownloadStage.DECRYPTING, segmentIndex, 0);
        byte[] key = getKey(context, source, segment.getKey(), keyCache);
        byte[] decrypted = segmentCrypto.decrypt(encrypted, key, segment.getKey().getIvHex(), segment.getSequence());
        Arrays.fill(encrypted, (byte) 0);
        validateTransportStream(decrypted);
        return decrypted;
    }

    private byte[] getKey(MediaDownloadContext context, ResolvedMediaSource source,
                          HlsPlaylist.Key keySpec, Map<HlsPlaylist.Key, byte[]> keyCache) throws IOException {
        byte[] cached = keyCache.get(keySpec);
        if (cached != null) {
            return cached;
        }
        byte[] key = downloadBytesWithRetry(context, source, keySpec.getUrl(), -1);
        if (key.length != 16) {
            Arrays.fill(key, (byte) 0);
            throw new IOException("AES-128 密钥长度无效");
        }
        keyCache.put(keySpec, key);
        return key;
    }

    private byte[] downloadBytesWithRetry(MediaDownloadContext context, ResolvedMediaSource source,
                                          String url, int segmentIndex) throws IOException {
        IOException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_REQUEST_ATTEMPTS; attempt++) {
            context.throwIfCancelled();
            try (Response response = context.getHttpSession().execute(url, source.getUserAgent(),
                    source.getReferer(), "*/*")) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new DownloadHttpSession.HttpStatusException(response.code());
                }
                long contentLength = response.body().contentLength();
                if (contentLength > MAX_SEGMENT_BYTES) {
                    throw new IOException("HLS 分片超过安全大小限制");
                }
                return readBounded(response.body().byteStream());
            } catch (DownloadHttpSession.HttpStatusException exception) {
                if (exception.indicatesExpiredSource()) {
                    throw exception;
                }
                lastFailure = exception;
            } catch (IOException exception) {
                lastFailure = exception;
            }
            Log.w(TAG, "segment retry, index=" + segmentIndex + ", attempt=" + attempt
                    + ", error=" + lastFailure.getClass().getSimpleName());
        }
        throw lastFailure == null ? new IOException("HLS 分片下载失败") : lastFailure;
    }

    private byte[] readBounded(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[32 * 1024];
        int total = 0;
        int count;
        while ((count = input.read(buffer)) != -1) {
            total += count;
            if (total > MAX_SEGMENT_BYTES) {
                throw new IOException("HLS 分片超过安全大小限制");
            }
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }

    private void validateTransportStream(byte[] bytes) throws IOException {
        if (bytes.length < 188 || (bytes[0] & 0xff) != 0x47) {
            throw new IOException("HLS 分片不是 MPEG-TS");
        }
        for (int offset = 188; offset < Math.min(bytes.length, 188 * 4); offset += 188) {
            if ((bytes[offset] & 0xff) != 0x47) {
                throw new IOException("MPEG-TS 分片同步字节校验失败");
            }
        }
    }

    private void wipeKeys(Map<HlsPlaylist.Key, byte[]> keyCache) {
        for (byte[] key : keyCache.values()) {
            Arrays.fill(key, (byte) 0);
        }
        keyCache.clear();
    }

    private void clearWorkFiles(MediaDownloadContext context) {
        for (String name : new String[]{"probe.ts", "probe.mp4", "combined.ts", "output.mp4"}) {
            File file = context.file(name);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
