package com.shuqiang.captain.xhs.parser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsRequestMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;
/**
 * 从 Pornhub 播放器的 mediaDefinitions 中提取主 HLS，并保留页面声明的 MP4 低清回退。
 */
public final class PornhubMediaParser {
    public static final String SOURCE_HLS = "pornhub_media_definitions_hls";
    public static final String SOURCE_DIRECT = "pornhub_og_video_direct";
    private static final String MEDIA_DEFINITIONS = "mediaDefinitions";
    private PornhubMediaParser() {
    }
    public static boolean isSupportedPage(String pageUrl) {
        String host = extractHost(pageUrl);
        return host != null && ("pornhub.com".equals(host) || host.endsWith(".pornhub.com"));
    }

    public static XhsParseResult parse(String html, String pageUrl, String entrySource, String requestStrategy) {
        if (!isSupportedPage(pageUrl) || html == null || html.trim().isEmpty()) {
            return null;
        }
        Document document = Jsoup.parse(html, pageUrl);
        Candidate bestHls = findBestHlsCandidate(html);
        if (bestHls == null) {
            return null;
        }
        String canonicalUrl = firstNonEmpty(
                document.select("link[rel=canonical]").attr("abs:href"),
                document.select("meta[property=og:url]").attr("content"),
                pageUrl
        );
        String coverUrl = normalize(document.select("meta[property=og:image]").attr("abs:content"));
        String title = firstNonEmpty(
                document.select("meta[property=og:title]").attr("content"),
                document.title(),
                "网页视频"
        );
        String siteName = firstNonEmpty(
                document.select("meta[property=og:site_name]").attr("content"),
                extractHost(canonicalUrl)
        );
        int durationSec = parsePositiveInt(document.select("meta[property=video:duration]").attr("content"));
        String canonicalViewKey = findQueryParameter(canonicalUrl, "viewkey");
        String sourcePageUrl = canonicalViewKey == null ? pageUrl : canonicalUrl;
        String noteId = firstNonEmpty(canonicalViewKey, findQueryParameter(pageUrl, "viewkey"),
                buildPageId(canonicalUrl));
        XhsRequestMode requestMode = requestModeOf(requestStrategy);
        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>();
        mediaItems.add(new XhsMediaItem(
                noteId + "_hls_" + bestHls.qualityHeight,
                XhsMediaType.VIDEO,
                bestHls.url,
                coverUrl,
                0,
                bestHls.qualityHeight,
                durationSec,
                "mp4",
                true,
                XhsMediaTransport.HLS_STREAM,
                sourcePageUrl,
                requestMode,
                SOURCE_HLS,
                bestHls.qualityHeight
        ));
        Candidate directCandidate = findDirectCandidate(document);
        if (directCandidate != null && !directCandidate.url.equals(bestHls.url)) {
            mediaItems.add(new XhsMediaItem(
                    noteId + "_mp4_" + directCandidate.qualityHeight,
                    XhsMediaType.VIDEO,
                    directCandidate.url,
                    coverUrl,
                    0,
                    directCandidate.qualityHeight,
                    durationSec,
                    "mp4",
                    false,
                    XhsMediaTransport.DIRECT_FILE,
                    sourcePageUrl,
                    requestMode,
                    SOURCE_DIRECT,
                    directCandidate.qualityHeight
            ));
        }
        return new XhsParseResult(
                noteId,
                pageUrl,
                canonicalUrl,
                siteName,
                title,
                coverUrl,
                requestStrategy + " · 播放器媒体定义",
                entrySource,
                mediaItems
        );
    }
    public static XhsMediaItem findResolvedItem(String html, String pageUrl, String sourceKey, int qualityHeight) {
        XhsParseResult result = parse(html, pageUrl, "download_refresh", "desktop");
        if (result == null) {
            return null;
        }
        XhsMediaItem fallback = null;
        for (XhsMediaItem item : result.getMediaItems()) {
            if (!sourceKey.equals(item.getSourceKey())) {
                continue;
            }
            if (fallback == null) {
                fallback = item;
            }
            if (qualityHeight <= 0 || qualityHeight == item.getQualityHeight()) {
                return item;
            }
        }
        return qualityHeight <= 0 ? fallback : null;
    }
    private static Candidate findBestHlsCandidate(String html) {
        JsonArray definitions = extractMediaDefinitions(html);
        Candidate best = null;
        if (definitions == null) {
            return null;
        }
        for (JsonElement element : definitions) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject definition = element.getAsJsonObject();
            String format = getString(definition, "format");
            String url = normalize(getString(definition, "videoUrl"));
            if (url == null || !("hls".equalsIgnoreCase(format) || isHlsUrl(url))
                    || !XhsNetworkPolicy.isAllowedMediaUrl(url)) {
                continue;
            }
            int quality = parsePositiveInt(getString(definition, "quality"));
            if (best == null || quality > best.qualityHeight) {
                best = new Candidate(url, quality);
            }
        }
        return best;
    }
    private static Candidate findDirectCandidate(Document document) {
        Element ogVideo = document.selectFirst("meta[property=og:video]");
        String url = ogVideo == null ? null : normalize(ogVideo.attr("abs:content"));
        if (url == null || !isMp4Url(url) || !XhsNetworkPolicy.isAllowedMediaUrl(url)) {
            return null;
        }
        int height = parsePositiveInt(document.select("meta[property=og:video:height]").attr("content"));
        return new Candidate(url, height);
    }
    private static JsonArray extractMediaDefinitions(String html) {
        JsonArray collected = new JsonArray();
        int marker = html.indexOf(MEDIA_DEFINITIONS);
        while (marker >= 0) {
            int arrayStart = html.indexOf('[', marker + MEDIA_DEFINITIONS.length());
            if (arrayStart < 0) {
                break;
            }
            int arrayEnd = findMatchingBracket(html, arrayStart, '[', ']');
            if (arrayEnd > arrayStart) {
                try {
                    JsonElement parsed = new JsonParser().parse(html.substring(arrayStart, arrayEnd + 1));
                    if (parsed.isJsonArray()) {
                        for (JsonElement definition : parsed.getAsJsonArray()) {
                            collected.add(definition);
                        }
                    }
                } catch (RuntimeException ignored) {
                    // 页面可能同时包含无关的同名文本，继续寻找下一个定义。
                }
            }
            marker = html.indexOf(MEDIA_DEFINITIONS, marker + MEDIA_DEFINITIONS.length());
        }
        return collected.size() == 0 ? null : collected;
    }
    static int findMatchingBracket(String value, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < value.length(); index++) {
            char current = value.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }
            if (current == '"') {
                inString = true;
            } else if (current == open) {
                depth++;
            } else if (current == close && --depth == 0) {
                return index;
            }
        }
        return -1;
    }
    private static String findQueryParameter(String rawUrl, String name) {
        try {
            String query = new URI(rawUrl).getRawQuery();
            if (query == null) {
                return null;
            }
            for (String pair : query.split("&")) {
                int equals = pair.indexOf('=');
                if (equals > 0 && name.equals(pair.substring(0, equals))) {
                    return URLDecoder.decode(pair.substring(equals + 1), "UTF-8");
                }
            }
        } catch (Exception ignored) {
            // 使用哈希 ID 降级。
        }
        return null;
    }
    private static String getString(JsonObject object, String name) {
        JsonElement value = object == null ? null : object.get(name);
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }

    private static boolean isHlsUrl(String url) {
        return url.toLowerCase(Locale.US).contains(".m3u8");
    }

    private static boolean isMp4Url(String url) {
        String lower = url.toLowerCase(Locale.US);
        int query = lower.indexOf('?');
        return (query < 0 ? lower : lower.substring(0, query)).endsWith(".mp4");
    }

    private static int parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value == null ? "" : value.replaceAll("[^0-9]", ""));
            return Math.max(parsed, 0);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static XhsRequestMode requestModeOf(String strategy) {
        String normalized = strategy == null ? "" : strategy.toLowerCase(Locale.US);
        if (normalized.contains("webview")) {
            return XhsRequestMode.WEBVIEW;
        }
        if (normalized.contains("mobile")) {
            return XhsRequestMode.MOBILE_RETRY;
        }
        return XhsRequestMode.DESKTOP;
    }

    private static String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim().replace("\\/", "/");
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String extractHost(String url) {
        try {
            String host = url == null ? null : new URI(url).getHost();
            return host == null ? null : host.toLowerCase(Locale.US);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String buildPageId(String url) {
        return "web_" + Integer.toHexString((url == null ? "" : url).hashCode());
    }

    private static final class Candidate {
        private final String url;
        private final int qualityHeight;

        private Candidate(String url, int qualityHeight) {
            this.url = url;
            this.qualityHeight = qualityHeight;
        }
    }
}
