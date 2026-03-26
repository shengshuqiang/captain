package com.shuqiang.captain.xhs.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * 下载页只允许访问小红书及其媒体白名单域名，避免被任意链接利用。
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
        return isAllowedUrl(url, MEDIA_HOST_SUFFIXES);
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
