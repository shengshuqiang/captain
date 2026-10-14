package com.shuqiang.captain.xhs.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * 页面抓取仍保留小红书直连判断，媒体下载放宽到通用 http/https 资源。
 */
public final class XhsNetworkPolicy {
    // 平台路由与短链跳转使用同一组域名，防止新增短链只在某一个入口生效。
    static final String SHORT_LINK_COM_HOST = "xhslink.com";
    static final String SHORT_LINK_CN_HOST = "xhslink.cn";
    private static final String[] PAGE_HOST_SUFFIXES = {
            "xiaohongshu.com",
            SHORT_LINK_COM_HOST,
            SHORT_LINK_CN_HOST
    };
    private static final String[] MEDIA_HOST_SUFFIXES = {
            "xiaohongshu.com",
            SHORT_LINK_COM_HOST,
            "xhscdn.com",
            "xhscdn.net"
    };

    private XhsNetworkPolicy() {
    }

    public static boolean isAllowedPageUrl(String url) {
        return isAllowedUrl(url, PAGE_HOST_SUFFIXES);
    }

    public static boolean isAllowedMediaUrl(String url) {
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
