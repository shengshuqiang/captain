package com.shuqiang.captain.xhs.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * 页面抓取仍保留小红书直连判断，媒体下载放宽到通用 http/https 资源。
 */
public final class XhsNetworkPolicy {
    private static final String[] PAGE_HOST_SUFFIXES = {
            "xiaohongshu.com",
            "xhslink.com"
    };
    private static final String[] MEDIA_HOST_SUFFIXES = {
            "xiaohongshu.com",
            "xhslink.com",
            "xhscdn.com",
            "xhscdn.net"
    };

    private XhsNetworkPolicy() {
    }

    public static boolean isAllowedPageUrl(String url) {
        return isAllowedUrl(url, PAGE_HOST_SUFFIXES);
    }

    public static boolean isAllowedMediaUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(forceHttps(url));
            String scheme = uri.getScheme();
            String host = uri.getHost();
            return host != null && scheme != null
                    && ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme));
        } catch (URISyntaxException ignored) {
            return false;
        }
    }

    public static String forceHttps(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        if (url.startsWith("http://")) {
            return "https://" + url.substring("http://".length());
        }
        return url;
    }

    private static boolean isAllowedUrl(String url, String[] hostSuffixes) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(forceHttps(url));
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null || !"https".equalsIgnoreCase(scheme)) {
                return false;
            }
            String lowerHost = host.toLowerCase(Locale.US);
            for (String hostSuffix : hostSuffixes) {
                if (lowerHost.equals(hostSuffix) || lowerHost.endsWith("." + hostSuffix)) {
                    return true;
                }
            }
        } catch (URISyntaxException ignored) {
            return false;
        }
        return false;
    }
}
