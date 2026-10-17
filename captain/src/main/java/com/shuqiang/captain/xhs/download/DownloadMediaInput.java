package com.shuqiang.captain.xhs.download;

import android.net.Uri;

import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.HttpDataSource;

import com.shuqiang.captain.xhs.playback.cache.PreviewPlaybackCache;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

/** 下载引擎共享的字节入口；可选缓存不改变 HTTP 失败、取消和文件校验语义。 */
@androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
public final class DownloadMediaInput implements AutoCloseable {
    private final InputStream input;
    private final long length;
    private final String contentType;

    private DownloadMediaInput(InputStream input, long length, String contentType) {
        this.input = input;
        this.length = length;
        this.contentType = contentType;
    }

    static DownloadMediaInput open(DownloadHttpSession http, ResolvedMediaSource source,
                                  String url, PreviewPlaybackCache.ExportLease cache) throws IOException {
        if (cache == null) {
            Response response = http.execute(url, source.getUserAgent(), source.getReferer(), DownloadHttpSession.ACCEPT_ANY);
            if (!response.isSuccessful() || response.body() == null) {
                int code = response.code();
                response.close();
                throw new DownloadHttpSession.HttpStatusException(code);
            }
            return new DownloadMediaInput(new FilterInputStream(response.body().byteStream()) {
                @Override public void close() { response.close(); }
            }, response.body().contentLength(), response.header("Content-Type", ""));
        }
        DataSource dataSource = cache.createDataSourceFactory(
                http.mediaDataSourceFactory(source)).createDataSource();
        try {
            // CacheDataSource.open 返回整个逻辑请求长度，不误用某一 Range 的响应长度。
            long length = dataSource.open(new DataSpec.Builder().setUri(Uri.parse(url)).build());
            InputStream translated = new InputStream() {
                private final byte[] singleByte = new byte[1];
                @Override public int read() throws IOException {
                    int count = read(singleByte, 0, 1);
                    return count == -1 ? -1 : singleByte[0] & 0xff;
                }
                @Override public int read(byte[] bytes, int offset, int count) throws IOException {
                    try { return dataSource.read(bytes, offset, count); }
                    catch (IOException error) { throw translate(error); }
                }
                @Override public void close() throws IOException { dataSource.close(); }
            };
            return new DownloadMediaInput(translated, length,
                    header(dataSource.getResponseHeaders(), "Content-Type"));
        } catch (IOException error) {
            try { dataSource.close(); } catch (IOException ignored) { }
            throw translate(error);
        }
    }

    private static String header(Map<String, List<String>> headers, String name) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey()) && !entry.getValue().isEmpty()) {
                return entry.getValue().get(0);
            }
        }
        return "";
    }

    private static IOException translate(IOException error) {
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof HttpDataSource.InvalidResponseCodeException) {
                DownloadHttpSession.HttpStatusException result = new DownloadHttpSession.HttpStatusException(
                        ((HttpDataSource.InvalidResponseCodeException) cause).responseCode);
                result.initCause(error);
                return result;
            }
            cause = cause.getCause();
        }
        return error;
    }

    public InputStream stream() { return input; }
    public long length() { return length; }
    public String contentType() { return contentType; }
    @Override public void close() throws IOException { input.close(); }
}
