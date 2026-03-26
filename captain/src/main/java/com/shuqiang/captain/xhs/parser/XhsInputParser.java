package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseInput;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 分享文案归一化，只保留当前功能真正需要的公开链接。
 */
public final class XhsInputParser {
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://[^\\s]+)");
    private static final String TRAILING_TRIM_CHARS = "。！？!?,，；;）)]}>\"'";

    private XhsInputParser() {
    }

    public static XhsParseInput parse(String rawText, String entrySource) throws XhsParserException {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new XhsParserException(XhsParseError.INVALID_INPUT);
        }
        String directUrl = null;
        String shortUrl = null;
        Matcher matcher = URL_PATTERN.matcher(rawText);
        while (matcher.find()) {
            String normalizedUrl = normalizeUrl(matcher.group(1));
            if (normalizedUrl == null || normalizedUrl.isEmpty()) {
                continue;
            }
            if (isDirectLink(normalizedUrl)) {
                directUrl = normalizedUrl;
                break;
            }
            if (shortUrl == null && isShortLink(normalizedUrl)) {
                shortUrl = normalizedUrl;
            }
        }
        String extractedUrl = directUrl != null ? directUrl : shortUrl;
        if (extractedUrl == null || extractedUrl.isEmpty()) {
            throw new XhsParserException(XhsParseError.INVALID_INPUT);
        }
        boolean shortLink = isShortLink(extractedUrl);
        extractedUrl = XhsNetworkPolicy.forceHttps(extractedUrl);
        return new XhsParseInput(rawText, extractedUrl, entrySource, shortLink);
    }

    public static boolean isDirectLink(String url) {
        String host = extractHost(url);
        return host != null && host.endsWith("xiaohongshu.com");
    }

    public static boolean isShortLink(String url) {
        String host = extractHost(url);
        return "xhslink.com".equals(host) || (host != null && host.endsWith(".xhslink.com"));
    }

    private static String normalizeUrl(String url) {
        if (url == null) {
            return null;
        }
        String normalizedUrl = url.trim().replace("&amp;", "&");
        while (!normalizedUrl.isEmpty() && TRAILING_TRIM_CHARS.indexOf(normalizedUrl.charAt(normalizedUrl.length() - 1)) >= 0) {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
        }
        return normalizedUrl;
    }

    private static String extractHost(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return host == null ? null : host.toLowerCase(Locale.US);
        } catch (URISyntaxException ignored) {
            return null;
        }
    }
}
