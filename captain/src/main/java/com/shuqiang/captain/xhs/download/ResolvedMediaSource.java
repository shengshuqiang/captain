package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsMediaTransport;

/**
 * 只存在于下载线程内的短效媒体地址与请求上下文，禁止序列化或持久化。
 */
public final class ResolvedMediaSource {
    private final String url;
    private final XhsMediaTransport transport;
    private final String userAgent;
    private final String referer;
    private final int qualityHeight;
    private final String directFallbackUrl;
    private final int directFallbackHeight;

    public ResolvedMediaSource(String url, XhsMediaTransport transport, String userAgent, String referer,
                               int qualityHeight, String directFallbackUrl, int directFallbackHeight) {
        this.url = url;
        this.transport = transport;
        this.userAgent = userAgent;
        this.referer = referer;
        this.qualityHeight = qualityHeight;
        this.directFallbackUrl = directFallbackUrl;
        this.directFallbackHeight = directFallbackHeight;
    }

    public String getUrl() {
        return url;
    }

    public XhsMediaTransport getTransport() {
        return transport;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public int getQualityHeight() {
        return qualityHeight;
    }

    public String getDirectFallbackUrl() {
        return directFallbackUrl;
    }

    public int getDirectFallbackHeight() {
        return directFallbackHeight;
    }

    public boolean hasDirectFallback() {
        return directFallbackUrl != null && !directFallbackUrl.trim().isEmpty();
    }
}
