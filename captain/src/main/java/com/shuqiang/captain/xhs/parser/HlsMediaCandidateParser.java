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
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * 只从视频标签、明确 HLS meta 和 VideoObject 中提取播放清单，拒绝随机脚本 URL。
 */
public final class HlsMediaCandidateParser {
    public static final String SOURCE_KEY = "generic_static_hls";
    public static final String SOURCE_DIRECT_KEY = "generic_static_direct";

    private HlsMediaCandidateParser() {
    }

    public static XhsParseResult parse(String html, String pageUrl, String entrySource, String requestStrategy) {
        if (html == null || html.trim().isEmpty()) {
            return null;
        }
        Document document = Jsoup.parse(html, pageUrl);
        LinkedHashMap<String, Candidate> candidates = new LinkedHashMap<>();
        collectVideoElements(document, candidates);
        collectHeadVideo(document, candidates);
        collectVideoObjects(document, candidates);
        if (candidates.isEmpty()) {
            return null;
        }

        ArrayList<Candidate> sorted = new ArrayList<>(candidates.values());
        Collections.sort(sorted, new Comparator<Candidate>() {
            @Override
            public int compare(Candidate left, Candidate right) {
                return Integer.compare(right.qualityHeight, left.qualityHeight);
            }
        });
        String canonicalUrl = firstNonEmpty(
                document.select("link[rel=canonical]").attr("abs:href"),
                document.select("meta[property=og:url]").attr("content"),
                pageUrl
        );
        String cover = normalize(document.select("meta[property=og:image]").attr("abs:content"));
        String title = firstNonEmpty(document.select("meta[property=og:title]").attr("content"),
                document.title(), "网页视频");
        String host = extractHost(canonicalUrl);
        String noteId = "web_" + Integer.toHexString(canonicalUrl.hashCode());
        XhsRequestMode requestMode = requestModeOf(requestStrategy);
        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>();
        for (int index = 0; index < sorted.size(); index++) {
            Candidate candidate = sorted.get(index);
            mediaItems.add(new XhsMediaItem(
                    noteId + "_hls_" + (candidate.qualityHeight > 0 ? candidate.qualityHeight : index + 1),
                    XhsMediaType.VIDEO,
                    candidate.url,
                    cover,
                    0,
                    candidate.qualityHeight,
                    0,
                    "mp4",
                    index == 0,
                    XhsMediaTransport.HLS_STREAM,
                    canonicalUrl,
                    requestMode,
                    SOURCE_KEY,
                    candidate.qualityHeight
            ));
        }
        Candidate direct = findDirectMp4(document);
        if (direct != null) {
            mediaItems.add(new XhsMediaItem(noteId + "_mp4_" + direct.qualityHeight,
                    XhsMediaType.VIDEO, direct.url, cover, 0, direct.qualityHeight, 0, "mp4", false,
                    XhsMediaTransport.DIRECT_FILE, canonicalUrl, requestMode, SOURCE_DIRECT_KEY,
                    direct.qualityHeight));
        }
        return new XhsParseResult(noteId, pageUrl, canonicalUrl, host, title, cover,
                requestStrategy + " · HLS 高置信识别", entrySource, mediaItems);
    }

    public static XhsMediaItem findResolvedItem(String html, String pageUrl,
                                                String sourceKey, int qualityHeight) {
        XhsParseResult result = parse(html, pageUrl, "download_refresh", "desktop");
        if (result == null || result.getMediaItems().isEmpty()) {
            return null;
        }
        for (XhsMediaItem item : result.getMediaItems()) {
            if (sourceKey.equals(item.getSourceKey())
                    && (qualityHeight <= 0 || item.getQualityHeight() == qualityHeight)) {
                return item;
            }
        }
        return null;
    }

    private static Candidate findDirectMp4(Document document) {
        Candidate best = null;
        Elements elements = document.select("video[src],video source[src],meta[property=og:video]");
        for (Element element : elements) {
            String attribute = "meta".equals(element.tagName()) ? "abs:content" : "abs:src";
            String url = normalize(element.attr(attribute));
            String lower = url == null ? "" : url.toLowerCase(Locale.US);
            int query = lower.indexOf('?');
            if (!(query < 0 ? lower : lower.substring(0, query)).endsWith(".mp4")
                    || !XhsNetworkPolicy.isAllowedMediaUrl(url)) {
                continue;
            }
            int quality = parseQuality(element);
            if (best == null || quality > best.qualityHeight) {
                best = new Candidate(url, quality);
            }
        }
        return best;
    }

    private static void collectVideoElements(Document document, LinkedHashMap<String, Candidate> candidates) {
        Elements elements = document.select("video[src],video source[src]");
        for (Element element : elements) {
            String url = normalize(element.attr("abs:src"));
            String type = firstNonEmpty(element.attr("type"),
                    element.parent() == null ? null : element.parent().attr("type"));
            if (isHls(url, type)) {
                addCandidate(candidates, url, parseQuality(element));
            }
        }
    }

    private static void collectHeadVideo(Document document, LinkedHashMap<String, Candidate> candidates) {
        String declaredType = document.select("meta[property=og:video:type]").attr("content");
        for (Element element : document.select("meta[property=og:video],meta[property=og:video:secure_url]")) {
            String url = normalize(element.attr("abs:content"));
            if (isHls(url, declaredType)) {
                int height = parsePositiveInt(document.select("meta[property=og:video:height]").attr("content"));
                addCandidate(candidates, url, height);
            }
        }
    }

    private static void collectVideoObjects(Document document, LinkedHashMap<String, Candidate> candidates) {
        for (Element script : document.select("script[type=application/ld+json]")) {
            try {
                collectVideoObjectNode(new JsonParser().parse(script.data()), candidates);
            } catch (RuntimeException ignored) {
                // 单个无效 JSON-LD 不影响其他高置信来源。
            }
        }
    }

    private static void collectVideoObjectNode(JsonElement node, LinkedHashMap<String, Candidate> candidates) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonArray()) {
            for (JsonElement child : node.getAsJsonArray()) {
                collectVideoObjectNode(child, candidates);
            }
            return;
        }
        if (!node.isJsonObject()) {
            return;
        }
        JsonObject object = node.getAsJsonObject();
        if (isVideoObject(object.get("@type"))) {
            String url = getString(object, "contentUrl");
            String encoding = getString(object, "encodingFormat");
            if (isHls(url, encoding)) {
                addCandidate(candidates, url, parsePositiveInt(getString(object, "height")));
            }
        }
        for (String key : new String[]{"@graph", "mainEntity", "video"}) {
            collectVideoObjectNode(object.get(key), candidates);
        }
    }

    private static boolean isVideoObject(JsonElement type) {
        if (type == null || type.isJsonNull()) {
            return false;
        }
        if (type.isJsonArray()) {
            JsonArray types = type.getAsJsonArray();
            for (JsonElement item : types) {
                if (isVideoObject(item)) {
                    return true;
                }
            }
            return false;
        }
        return "videoobject".equalsIgnoreCase(type.getAsString());
    }

    private static void addCandidate(LinkedHashMap<String, Candidate> candidates, String url, int height) {
        String normalized = XhsNetworkPolicy.forceHttps(normalize(url));
        if (normalized != null && XhsNetworkPolicy.isAllowedMediaUrl(normalized) && !candidates.containsKey(normalized)) {
            candidates.put(normalized, new Candidate(normalized, height));
        }
    }

    private static int parseQuality(Element element) {
        int quality = parsePositiveInt(firstNonEmpty(element.attr("data-quality"), element.attr("res"),
                element.attr("label"), element.attr("size")));
        return quality > 0 ? quality : parsePositiveInt(element.id());
    }

    static boolean isHls(String url, String type) {
        String lowerType = type == null ? "" : type.toLowerCase(Locale.US);
        return hasHlsPath(url) || lowerType.contains("mpegurl");
    }

    private static boolean hasHlsPath(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return false;
        }
        try {
            String path = new URI(rawUrl).getPath();
            return path != null && path.toLowerCase(Locale.US).endsWith(".m3u8");
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String getString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }

    private static int parsePositiveInt(String value) {
        if (value == null) {
            return 0;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d{3,4})").matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static XhsRequestMode requestModeOf(String strategy) {
        String lower = strategy == null ? "" : strategy.toLowerCase(Locale.US);
        return lower.contains("webview") ? XhsRequestMode.WEBVIEW
                : (lower.contains("mobile") ? XhsRequestMode.MOBILE_RETRY : XhsRequestMode.DESKTOP);
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
            return new URI(url).getHost();
        } catch (Exception ignored) {
            return "unknown";
        }
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
