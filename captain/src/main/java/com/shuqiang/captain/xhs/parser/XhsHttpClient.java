package com.shuqiang.captain.xhs.parser;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 统一管理解析与下载共用的网络客户端，便于后续补超时和 header。
 */
public final class XhsHttpClient {
    public static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
    public static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Pixel 7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36";

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(18, TimeUnit.SECONDS)
            .writeTimeout(18, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build();

    private XhsHttpClient() {
    }

    public static OkHttpClient getClient() {
        return OK_HTTP_CLIENT;
    }

    public static Request buildPageRequest(String url, String userAgent) {
        return new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .get()
                .build();
    }

    public static Request buildMediaRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("User-Agent", MOBILE_USER_AGENT)
                .header("Accept", "*/*")
                .get()
                .build();
    }
}
