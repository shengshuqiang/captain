package com.shuqiang.captain.xhs.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 淘宝分享短链先返回 JS 中转页，真实视频通常在商品详情 API 的嵌套 JSON 里。
 */
final class TaobaoShareParser {
    private static final Pattern JS_REDIRECT_PATTERN = Pattern.compile(
            "(?:var\\s+url|window\\.location\\.href|location\\.href)\\s*=\\s*['\"]([^'\"]+)['\"]",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern LOCATION_REPLACE_PATTERN = Pattern.compile(
            "location\\.replace\\(['\"]([^'\"]+)['\"]\\)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ITEM_ID_QUERY_PATTERN = Pattern.compile("(?:[?&](?:id|itemId|itemNumId)=)(\\d+)");
    private static final Pattern ITEM_ID_PATH_PATTERN = Pattern.compile("/i(\\d+)\\.htm");
    private static final String DEFAULT_TITLE = "淘宝商品视频";
    private static final int MAX_JSON_DEPTH = 16;

    private TaobaoShareParser() {
    }

    static boolean isSupportedPage(String pageUrl) {
        try {
            URI uri = new URI(XhsNetworkPolicy.forceHttps(pageUrl));
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            String lowerHost = host.toLowerCase(Locale.US);
            return lowerHost.equals("e.tb.cn")
                    || lowerHost.equals("m.tb.cn")
                    || lowerHost.endsWith(".taobao.com")
                    || lowerHost.endsWith(".tmall.com");
        } catch (Exception ignored) {
            return false;
        }
    }

    static String extractJsRedirectUrl(String html) {
        String url = extractFirstMatch(JS_REDIRECT_PATTERN, html);
        if (url == null) {
            url = extractFirstMatch(LOCATION_REPLACE_PATTERN, html);
        }
        url = normalizeUrl(null, url);
        return isSupportedPage(url) ? url : null;
    }

    static String describeUrlForLog(String pageUrl) {
        String normalizedUrl = normalizeUrl(null, pageUrl);
        if (normalizedUrl == null) {
            return "empty";
        }
        try {
            URI uri = new URI(normalizedUrl);
            String host = uri.getHost();
            String itemId = extractItemId(normalizedUrl);
            return (host == null ? "unknown" : host)
                    + (itemId == null ? "" : "?id=" + itemId);
        } catch (Exception ignored) {
            return "invalid";
        }
    }

    static boolean hasDetailMediaHint(String html) {
        if (html == null || html.trim().isEmpty()) {
            return false;
        }
        String lowerHtml = html.toLowerCase(Locale.US);
        return lowerHtml.contains("apistack")
                || lowerHtml.contains("videourl")
                || lowerHtml.contains(".mp4")
                || lowerHtml.contains(".mov")
                || lowerHtml.contains(".m4v")
                || lowerHtml.contains(".webm");
    }

    static boolean isLikelyDownloadableVideoUrl(String mediaUrl) {
        if (mediaUrl == null || !XhsNetworkPolicy.isAllowedMediaUrl(mediaUrl)) {
            return false;
        }
        String lowerUrl = mediaUrl.toLowerCase(Locale.US);
        if (lowerUrl.contains(".m3u8") || lowerUrl.startsWith("blob:") || lowerUrl.startsWith("data:")) {
            return false;
        }
        if (lowerUrl.contains(".mp4") || lowerUrl.contains(".mov")
                || lowerUrl.contains(".m4v") || lowerUrl.contains(".webm")) {
            return true;
        }
        try {
            URI uri = new URI(mediaUrl);
            String host = uri.getHost();
            String path = uri.getPath();
            String lowerHost = host == null ? "" : host.toLowerCase(Locale.US);
            String lowerPath = path == null ? "" : path.toLowerCase(Locale.US);
            return lowerHost.contains("video")
                    && (lowerPath.contains("/play") || lowerPath.contains("/video"));
        } catch (Exception ignored) {
            return false;
        }
    }

    static String normalizeSniffedUrl(String pageUrl, String rawUrl) {
        return normalizeUrl(pageUrl, rawUrl);
    }

    static XhsParseResult buildSniffedVideoResult(String mediaUrl,
                                                 String pageUrl,
                                                 String entrySource,
                                                 String requestStrategy) {
        String normalizedMediaUrl = normalizeUrl(pageUrl, mediaUrl);
        if (!isLikelyDownloadableVideoUrl(normalizedMediaUrl)) {
            return null;
        }
        String noteId = firstNonEmpty(extractItemId(pageUrl), extractItemId(normalizedMediaUrl), buildPageId(pageUrl));
        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>();
        mediaItems.add(new XhsMediaItem(
                noteId + "_webview_1",
                XhsMediaType.VIDEO,
                normalizedMediaUrl,
                normalizedMediaUrl,
                0,
                0,
                0,
                guessVideoExtension(normalizedMediaUrl),
                true
        ));
        return new XhsParseResult(
                noteId,
                pageUrl,
                pageUrl,
                "淘宝",
                DEFAULT_TITLE,
                normalizedMediaUrl,
                requestStrategy + " · 淘宝 WebView 视频嗅探",
                entrySource,
                mediaItems
        );
    }

    static XhsParseResult parseDetailResponse(String responseJson,
                                              String pageUrl,
                                              String entrySource,
                                              String requestStrategy) {
        JsonObject root = parseObject(extractJsonPayload(responseJson));
        if (root == null) {
            return null;
        }
        JsonObject data = firstNonNull(XhsStateJsonParser.getObject(root, "data"), root);
        JsonObject detailData = firstNonNull(extractApiStackData(data), data);
        String noteId = firstNonEmpty(
                extractItemId(pageUrl),
                getNestedString(detailData, "item", "itemId"),
                getNestedString(detailData, "item", "itemNumId"),
                getNestedString(detailData, "item", "id"),
                buildPageId(pageUrl)
        );
        String title = firstNonEmpty(
                cleanTitle(getNestedString(detailData, "item", "title")),
                cleanTitle(getNestedString(detailData, "item", "name")),
                cleanTitle(getNestedString(detailData, "item", "titleText")),
                DEFAULT_TITLE
        );
        String authorName = firstNonEmpty(
                cleanTitle(getNestedString(detailData, "seller", "shopName")),
                cleanTitle(getNestedString(detailData, "seller", "sellerNick")),
                cleanTitle(getNestedString(detailData, "shop", "shopName")),
                "淘宝"
        );
        String defaultCoverUrl = firstNonEmpty(
                extractFirstCoverUrl(detailData, pageUrl, 0),
                extractFirstCoverUrl(data, pageUrl, 0)
        );

        LinkedHashMap<String, VideoCandidate> candidates = new LinkedHashMap<>();
        collectVideoCandidates(detailData, candidates, defaultCoverUrl, "", pageUrl, 0);
        if (candidates.isEmpty() && data != detailData) {
            collectVideoCandidates(data, candidates, defaultCoverUrl, "", pageUrl, 0);
        }
        if (candidates.isEmpty()) {
            return null;
        }

        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>();
        int index = 1;
        for (VideoCandidate candidate : candidates.values()) {
            mediaItems.add(new XhsMediaItem(
                    noteId + "_" + index,
                    XhsMediaType.VIDEO,
                    candidate.mediaUrl,
                    candidate.coverUrl,
                    0,
                    0,
                    0,
                    guessVideoExtension(candidate.mediaUrl),
                    true
            ));
            index++;
        }
        String coverUrl = firstNonEmpty(mediaItems.get(0).getCoverUrl(), defaultCoverUrl, mediaItems.get(0).getMediaUrl());
        return new XhsParseResult(
                noteId,
                pageUrl,
                pageUrl,
                authorName,
                title,
                coverUrl,
                requestStrategy + " · 淘宝商品视频",
                entrySource,
                mediaItems
        );
    }

    static XhsParseResult parsePageHtml(String html,
                                        String pageUrl,
                                        String entrySource,
                                        String requestStrategy) {
        String detailJson = extractEmbeddedDetailJson(html);
        return detailJson == null ? null : parseDetailResponse(detailJson, pageUrl, entrySource, requestStrategy);
    }

    private static JsonObject extractApiStackData(JsonObject data) {
        JsonArray apiStack = XhsStateJsonParser.getArray(data, "apiStack");
        if (apiStack == null) {
            return null;
        }
        for (JsonElement element : apiStack) {
            JsonObject stackItem = XhsStateJsonParser.getObject(element);
            String value = XhsStateJsonParser.getString(stackItem, "value");
            JsonObject parsedValue = parseObject(value);
            JsonObject globalData = getNestedObject(parsedValue, "global", "data");
            if (globalData != null) {
                return globalData;
            }
            if (parsedValue != null) {
                return parsedValue;
            }
        }
        return null;
    }

    private static void collectVideoCandidates(JsonElement element,
                                               LinkedHashMap<String, VideoCandidate> candidates,
                                               String inheritedCover,
                                               String path,
                                               String pageUrl,
                                               int depth) {
        if (element == null || element.isJsonNull() || depth > MAX_JSON_DEPTH) {
            return;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                collectVideoCandidates(array.get(i), candidates, inheritedCover, path + "[]", pageUrl, depth + 1);
            }
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        String coverUrl = firstNonEmpty(extractObjectCoverUrl(object, pageUrl), inheritedCover);
        addVideoCandidate(object, candidates, coverUrl, path, pageUrl, "videoUrl");
        addVideoCandidate(object, candidates, coverUrl, path, pageUrl, "playUrl");
        addVideoCandidate(object, candidates, coverUrl, path, pageUrl, "video");
        addVideoCandidate(object, candidates, coverUrl, path, pageUrl, "src");
        addVideoCandidate(object, candidates, coverUrl, path, pageUrl, "url");
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            collectVideoCandidates(
                    entry.getValue(),
                    candidates,
                    coverUrl,
                    appendPath(path, entry.getKey()),
                    pageUrl,
                    depth + 1
            );
        }
    }

    private static void addVideoCandidate(JsonObject object,
                                          LinkedHashMap<String, VideoCandidate> candidates,
                                          String coverUrl,
                                          String path,
                                          String pageUrl,
                                          String fieldName) {
        String rawUrl = XhsStateJsonParser.getString(object, fieldName);
        String mediaUrl = normalizeUrl(pageUrl, rawUrl);
        if (!isLikelyVideoUrl(mediaUrl, path, fieldName, object)) {
            return;
        }
        String normalizedCover = normalizeUrl(pageUrl, coverUrl);
        if (!candidates.containsKey(mediaUrl)) {
            candidates.put(mediaUrl, new VideoCandidate(mediaUrl, normalizedCover));
        }
    }

    private static boolean isLikelyVideoUrl(String mediaUrl,
                                            String path,
                                            String fieldName,
                                            JsonObject owner) {
        if (mediaUrl == null || !XhsNetworkPolicy.isAllowedMediaUrl(mediaUrl)) {
            return false;
        }
        String lowerUrl = mediaUrl.toLowerCase(Locale.US);
        if (lowerUrl.contains(".m3u8")) {
            return false;
        }
        if (lowerUrl.contains(".mp4") || lowerUrl.contains(".mov")
                || lowerUrl.contains(".m4v") || lowerUrl.contains(".webm")) {
            return true;
        }
        String lowerField = fieldName == null ? "" : fieldName.toLowerCase(Locale.US);
        String lowerPath = path == null ? "" : path.toLowerCase(Locale.US);
        if (lowerField.contains("video") || lowerField.contains("play")) {
            return true;
        }
        return lowerField.equals("url")
                && (lowerPath.contains("video") || XhsStateJsonParser.getString(owner, "videoThumbnailURL") != null)
                && isVideoLikeHost(mediaUrl);
    }

    private static String extractFirstCoverUrl(JsonElement element, String pageUrl, int depth) {
        if (element == null || element.isJsonNull() || depth > MAX_JSON_DEPTH) {
            return null;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) {
                String coverUrl = extractFirstCoverUrl(child, pageUrl, depth + 1);
                if (coverUrl != null) {
                    return coverUrl;
                }
            }
            return null;
        }
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        String coverUrl = extractObjectCoverUrl(object, pageUrl);
        if (coverUrl != null) {
            return coverUrl;
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String nestedCoverUrl = extractFirstCoverUrl(entry.getValue(), pageUrl, depth + 1);
            if (nestedCoverUrl != null) {
                return nestedCoverUrl;
            }
        }
        return null;
    }

    private static String extractObjectCoverUrl(JsonObject object, String pageUrl) {
        return firstNonEmpty(
                normalizeUrl(pageUrl, XhsStateJsonParser.getString(object, "videoThumbnailURL")),
                normalizeUrl(pageUrl, XhsStateJsonParser.getString(object, "thumbnail")),
                normalizeUrl(pageUrl, XhsStateJsonParser.getString(object, "cover")),
                normalizeUrl(pageUrl, XhsStateJsonParser.getString(object, "coverUrl")),
                normalizeUrl(pageUrl, XhsStateJsonParser.getString(object, "poster")),
                normalizeUrl(pageUrl, XhsStateJsonParser.getString(object, "picUrl")),
                normalizeUrl(pageUrl, XhsStateJsonParser.getString(object, "imageUrl"))
        );
    }

    private static String extractItemId(String url) {
        String normalizedUrl = normalizeUrl(null, url);
        if (normalizedUrl == null) {
            return null;
        }
        String itemId = extractFirstMatch(ITEM_ID_QUERY_PATTERN, normalizedUrl);
        return itemId == null ? extractFirstMatch(ITEM_ID_PATH_PATTERN, normalizedUrl) : itemId;
    }

    private static JsonObject parseObject(String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            return null;
        }
        try {
            JsonElement element = new JsonParser().parse(rawJson.trim());
            return XhsStateJsonParser.getObject(element);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String extractJsonPayload(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return trimmed;
        }
        return trimmed.substring(start, end + 1);
    }

    private static String extractEmbeddedDetailJson(String html) {
        if (html == null || !html.contains("apiStack")) {
            return null;
        }
        int markerIndex = html.indexOf("apiStack");
        int startIndex = html.lastIndexOf('{', markerIndex);
        while (startIndex >= 0) {
            String json = extractBalancedJsonObject(html, startIndex);
            if (parseObject(json) != null) {
                return json;
            }
            startIndex = html.lastIndexOf('{', startIndex - 1);
        }
        return null;
    }

    private static String extractBalancedJsonObject(String text, int startIndex) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = startIndex; i < text.length(); i++) {
            char current = text.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = inString;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return text.substring(startIndex, i + 1);
                }
            }
        }
        return null;
    }

    private static String normalizeUrl(String pageUrl, String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return null;
        }
        String url = rawUrl.trim()
                .replace("&amp;", "&")
                .replace("\\u002F", "/")
                .replace("\\/", "/");
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return XhsNetworkPolicy.forceHttps(url);
        }
        if (pageUrl != null && url.startsWith("/")) {
            try {
                URI baseUri = new URI(XhsNetworkPolicy.forceHttps(pageUrl));
                return baseUri.getScheme() + "://" + baseUri.getHost() + url;
            } catch (Exception ignored) {
                return null;
            }
        }
        return url;
    }

    private static String cleanTitle(String title) {
        if (title == null) {
            return null;
        }
        String cleanTitle = title.replace('\n', ' ').replace('\r', ' ').trim();
        return cleanTitle.isEmpty() ? null : cleanTitle;
    }

    private static String getNestedString(JsonObject object, String... keys) {
        JsonObject current = object;
        for (int i = 0; i < keys.length; i++) {
            if (current == null) {
                return null;
            }
            if (i == keys.length - 1) {
                return XhsStateJsonParser.getString(current, keys[i]);
            }
            current = XhsStateJsonParser.getObject(current, keys[i]);
        }
        return null;
    }

    private static JsonObject getNestedObject(JsonObject object, String... keys) {
        JsonObject current = object;
        for (String key : keys) {
            current = XhsStateJsonParser.getObject(current, key);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private static String extractFirstMatch(Pattern pattern, String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static boolean isVideoLikeHost(String mediaUrl) {
        try {
            URI uri = new URI(mediaUrl);
            String host = uri.getHost();
            return host != null && host.toLowerCase(Locale.US).contains("video");
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String appendPath(String path, String fieldName) {
        if (path == null || path.isEmpty()) {
            return fieldName;
        }
        return path + "." + fieldName;
    }

    private static String buildPageId(String pageUrl) {
        return Integer.toHexString(pageUrl == null ? 0 : pageUrl.hashCode());
    }

    private static String guessVideoExtension(String mediaUrl) {
        if (mediaUrl == null) {
            return "mp4";
        }
        String lowerUrl = mediaUrl.toLowerCase(Locale.US);
        if (lowerUrl.contains(".webm")) {
            return "webm";
        }
        if (lowerUrl.contains(".mov")) {
            return "mov";
        }
        if (lowerUrl.contains(".m4v")) {
            return "m4v";
        }
        return "mp4";
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static final class VideoCandidate {
        private final String mediaUrl;
        private final String coverUrl;

        private VideoCandidate(String mediaUrl, String coverUrl) {
            this.mediaUrl = mediaUrl;
            this.coverUrl = coverUrl;
        }
    }
}
