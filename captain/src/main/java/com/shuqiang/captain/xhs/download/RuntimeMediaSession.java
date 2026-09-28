package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsMediaTransport;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 手动浏览提取产生的进程内会话，持有短效地址和按来源隔离的 Cookie，禁止序列化或日志输出。
 */
public final class RuntimeMediaSession {
    private final String id;
    private final String userAgent;
    private final String pageReferer;
    private final long expiresAtMs;
    private final Map<String, Candidate> candidates;
    private final Map<String, String> cookieHeadersByOrigin;
    private boolean closed;

    RuntimeMediaSession(String id, String userAgent, String pageReferer, long expiresAtMs,
                        Map<String, Candidate> candidates, Map<String, String> cookieHeadersByOrigin) {
        this.id = id;
        this.userAgent = userAgent;
        this.pageReferer = pageReferer;
        this.expiresAtMs = expiresAtMs;
        this.candidates = new LinkedHashMap<>(candidates);
        this.cookieHeadersByOrigin = new LinkedHashMap<>(cookieHeadersByOrigin);
    }

    String getId() {
        return id;
    }

    synchronized Candidate resolve(String candidateId) {
        return closed ? null : candidates.get(candidateId);
    }

    synchronized String getCookieHeader(String url) {
        if (closed) {
            return null;
        }
        return cookieHeadersByOrigin.get(originOf(url));
    }

    String getUserAgent() {
        return userAgent;
    }

    String getPageReferer() {
        return pageReferer;
    }

    boolean isExpired(long nowMs) {
        return nowMs >= expiresAtMs;
    }

    public synchronized void close() {
        closed = true;
        candidates.clear();
        cookieHeadersByOrigin.clear();
    }

    static String originOf(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return null;
        }
        try {
            URI uri = new URI(rawUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null) {
                return null;
            }
            String lowerScheme = scheme.toLowerCase(Locale.US);
            if (!"http".equals(lowerScheme) && !"https".equals(lowerScheme)) {
                return null;
            }
            int port = uri.getPort();
            boolean defaultPort = port < 0 || ("http".equals(lowerScheme) && port == 80)
                    || ("https".equals(lowerScheme) && port == 443);
            return lowerScheme + "://" + host.toLowerCase(Locale.US) + (defaultPort ? "" : ":" + port);
        } catch (Exception ignored) {
            return null;
        }
    }

    static final class Candidate {
        private final String url;
        private final XhsMediaTransport transport;
        private final int qualityHeight;
        private final String referer;

        Candidate(String url, XhsMediaTransport transport, int qualityHeight, String referer) {
            this.url = url;
            this.transport = transport;
            this.qualityHeight = qualityHeight;
            this.referer = referer;
        }

        String getUrl() {
            return url;
        }

        XhsMediaTransport getTransport() {
            return transport;
        }

        int getQualityHeight() {
            return qualityHeight;
        }

        String getReferer() {
            return referer;
        }
    }
}
