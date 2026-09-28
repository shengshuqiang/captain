package com.shuqiang.captain.xhs.download;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 单个下载任务共享的内存 Cookie 会话；任务结束即释放，不落盘也不输出日志。
 */
public final class DownloadHttpSession {
    private final MemoryCookieJar cookieJar = new MemoryCookieJar();
    private final OkHttpClient client;

    public DownloadHttpSession() {
        this(null);
    }

    public DownloadHttpSession(final RuntimeMediaSession runtimeSession) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(45, TimeUnit.SECONDS)
                .callTimeout(0, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .cookieJar(cookieJar);
        if (runtimeSession != null) {
            builder.addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    String capturedCookie = runtimeSession.getCookieHeader(request.url().toString());
                    if (capturedCookie == null || capturedCookie.trim().isEmpty()) {
                        return chain.proceed(request);
                    }
                    String mergedCookie = mergeCookies(capturedCookie, request.header("Cookie"));
                    return chain.proceed(request.newBuilder().header("Cookie", mergedCookie).build());
                }
            });
        }
        client = builder.build();
    }

    public Response execute(String url, String userAgent, String referer, String accept) throws IOException {
        Request.Builder request = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", accept == null ? "*/*" : accept)
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .get();
        if (referer != null && !referer.trim().isEmpty()) {
            request.header("Referer", referer);
        }
        return client.newCall(request.build()).execute();
    }

    public String readText(String url, String userAgent, String referer, String accept) throws IOException {
        try (Response response = execute(url, userAgent, referer, accept)) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new HttpStatusException(response.code());
            }
            return response.body().string();
        }
    }

    public void cancel() {
        client.dispatcher().cancelAll();
    }

    public void close() {
        cancel();
        cookieJar.clear();
        client.connectionPool().evictAll();
    }

    static String mergeCookies(String captured, String current) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        addCookies(values, captured);
        addCookies(values, current);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return builder.toString();
    }

    private static void addCookies(Map<String, String> target, String header) {
        if (header == null || header.trim().isEmpty()) {
            return;
        }
        for (String part : header.split(";")) {
            int separator = part.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String name = part.substring(0, separator).trim();
            String value = part.substring(separator + 1).trim();
            if (!name.isEmpty()) {
                target.put(name, value);
            }
        }
    }

    public static final class HttpStatusException extends IOException {
        private final int statusCode;

        public HttpStatusException(int statusCode) {
            super("媒体请求失败，HTTP " + statusCode);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean indicatesExpiredSource() {
            return statusCode == 401 || statusCode == 403 || statusCode == 404 || statusCode == 410;
        }
    }

    private static final class MemoryCookieJar implements CookieJar {
        private final Map<String, List<Cookie>> cookiesByHost = new HashMap<>();

        @Override
        public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            if (cookies == null || cookies.isEmpty()) {
                return;
            }
            List<Cookie> stored = cookiesByHost.get(url.host());
            ArrayList<Cookie> merged = stored == null ? new ArrayList<Cookie>() : new ArrayList<>(stored);
            for (Cookie incoming : cookies) {
                for (int index = merged.size() - 1; index >= 0; index--) {
                    Cookie existing = merged.get(index);
                    if (existing.name().equals(incoming.name())
                            && existing.domain().equals(incoming.domain())
                            && existing.path().equals(incoming.path())) {
                        merged.remove(index);
                    }
                }
                if (incoming.expiresAt() > System.currentTimeMillis()) {
                    merged.add(incoming);
                }
            }
            cookiesByHost.put(url.host(), merged);
        }

        @Override
        public synchronized List<Cookie> loadForRequest(HttpUrl url) {
            ArrayList<Cookie> matched = new ArrayList<>();
            for (List<Cookie> cookies : cookiesByHost.values()) {
                for (Cookie cookie : cookies) {
                    if (cookie.expiresAt() > System.currentTimeMillis() && cookie.matches(url)) {
                        matched.add(cookie);
                    }
                }
            }
            return matched;
        }

        synchronized void clear() {
            cookiesByHost.clear();
        }
    }
}
