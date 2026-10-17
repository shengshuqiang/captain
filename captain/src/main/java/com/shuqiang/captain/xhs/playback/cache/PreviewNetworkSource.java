package com.shuqiang.captain.xhs.playback.cache;

import android.webkit.CookieManager;

import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.okhttp.OkHttpDataSource;

import com.shuqiang.captain.xhs.parser.XhsHttpClient;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/** 注册信息只保留在内存；每个实际网络请求（含重定向）按目标域名取 Cookie。 */
@androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
final class PreviewNetworkSource {
    private static final String COOKIE_HEADER = "Cookie";
    private static final String USER_AGENT_HEADER = "User-Agent";
    final String url;
    final boolean hls;
    private final Map<String, String> headers;

    private PreviewNetworkSource(String url, boolean hls, Map<String, String> headers) {
        this.url = url;
        this.hls = hls;
        this.headers = Collections.unmodifiableMap(headers);
    }

    static PreviewNetworkSource from(String url, boolean hls, Map<String, String> headers) {
        if (url == null || HttpUrl.parse(url) == null) return null;
        Map<String, String> safe = new LinkedHashMap<>();
        safe.put(USER_AGENT_HEADER, XhsHttpClient.MOBILE_USER_AGENT);
        try {
            if (headers != null) {
                for (Map.Entry<String, String> value : headers.entrySet()) {
                    if (COOKIE_HEADER.equalsIgnoreCase(value.getKey())) continue;
                    // 先让 OkHttp 验证头，避免一个坏资源延迟到播放线程才抛错。
                    new Request.Builder().url(url).header(value.getKey(), value.getValue());
                    if (USER_AGENT_HEADER.equalsIgnoreCase(value.getKey())) safe.remove(USER_AGENT_HEADER);
                    safe.put(value.getKey(), value.getValue());
                }
            }
            return new PreviewNetworkSource(url, hls, safe);
        } catch (IllegalArgumentException | NullPointerException invalidHeader) {
            return null;
        }
    }

    static OkHttpClient newClient() {
        return new OkHttpClient.Builder().addNetworkInterceptor(chain -> {
            Request request = chain.request();
            String cookie = CookieManager.getInstance().getCookie(request.url().toString());
            Request.Builder builder = request.newBuilder().removeHeader(COOKIE_HEADER);
            if (cookie != null && !cookie.trim().isEmpty()) builder.header(COOKIE_HEADER, cookie);
            return chain.proceed(builder.build());
        }).build();
    }

    DataSource.Factory factory(OkHttpClient client) {
        return new OkHttpDataSource.Factory(client).setDefaultRequestProperties(headers);
    }
}
