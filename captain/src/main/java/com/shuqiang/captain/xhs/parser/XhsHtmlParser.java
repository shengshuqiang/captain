package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析 SSR HTML，优先使用 meta 标签，必要时回退到页面状态树。
 */
public final class XhsHtmlParser {
    private static final Pattern NOTE_ID_PATTERN = Pattern.compile("/(explore|discovery/item)/([0-9a-zA-Z]+)");

    private XhsHtmlParser() {
    }

    public static boolean hasUsefulContent(String html) {
        if (html == null || html.trim().isEmpty()) {
            return false;
        }
        return html.contains("og:video") || html.contains("og:image") || html.contains("window.__INITIAL_STATE__");
    }

    public static XhsParseResult parse(String html, String pageUrl, String entrySource, String requestStrategy)
            throws XhsParserException {
        if (!hasUsefulContent(html)) {
            throw new XhsParserException(XhsParseError.NOTE_UNAVAILABLE);
        }
        Document document = Jsoup.parse(html, pageUrl);
        String canonicalUrl = firstNonEmpty(
                normalizeResourceUrl(extractMeta(document, "og:url")),
                pageUrl
        );
        String ogImage = normalizeResourceUrl(extractMeta(document, "og:image"));
        String ogVideo = normalizeResourceUrl(extractMeta(document, "og:video"));
        String ogTitle = cleanMetaTitle(extractMeta(document, "og:title"));
        int metaDuration = parseDuration(extractMeta(document, "og:videotime"));

        JsonObject initialState = extractInitialState(document);
        JsonObject noteObject = findNoteObject(initialState, 0);

        String noteId = firstNonEmpty(
                getString(noteObject, "noteId"),
                extractNoteId(canonicalUrl),
                extractNoteId(pageUrl)
        );
        String title = firstNonEmpty(
                getString(noteObject, "title"),
                ogTitle
        );
        String author = firstNonEmpty(
                extractUserName(noteObject),
                ogTitle
        );

        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>();
        String parseStrategy = requestStrategy + " · meta";
        String videoUrl = firstNonEmpty(ogVideo, extractVideoUrl(noteObject));
        if (videoUrl != null && XhsNetworkPolicy.isAllowedMediaUrl(videoUrl)) {
            String videoCoverUrl = firstNonEmpty(
                    ogImage,
                    extractVideoCoverUrl(noteObject, videoUrl)
            );
            mediaItems.add(new XhsMediaItem(
                    buildMediaId(noteId, mediaItems.size() + 1),
                    XhsMediaType.VIDEO,
                    videoUrl,
                    videoCoverUrl,
                    0,
                    0,
                    metaDuration > 0 ? metaDuration : extractVideoDuration(noteObject),
                    guessExtension(videoUrl, XhsMediaType.VIDEO),
                    true
            ));
            if (videoCoverUrl != null && XhsNetworkPolicy.isAllowedMediaUrl(videoCoverUrl)) {
                mediaItems.add(new XhsMediaItem(
                        buildMediaId(noteId, mediaItems.size() + 1),
                        XhsMediaType.IMAGE,
                        videoCoverUrl,
                        videoCoverUrl,
                        0,
                        0,
                        0,
                        guessExtension(videoCoverUrl, XhsMediaType.IMAGE),
                        true
                ));
            }
            parseStrategy = noteObject != null ? requestStrategy + " · meta + initialState" : requestStrategy + " · meta";
        } else if (noteObject != null) {
            mediaItems.addAll(extractImageItems(noteObject, noteId));
            parseStrategy = requestStrategy + " · meta + initialState";
        }
        if (mediaItems.isEmpty() && ogImage != null && XhsNetworkPolicy.isAllowedMediaUrl(ogImage)) {
            mediaItems.add(new XhsMediaItem(
                    buildMediaId(noteId, 1),
                    XhsMediaType.IMAGE,
                    ogImage,
                    ogImage,
                    0,
                    0,
                    0,
                    guessExtension(ogImage, XhsMediaType.IMAGE),
                    true
            ));
        }
        if (mediaItems.isEmpty()) {
            throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
        }
        String coverUrl = firstNonEmpty(ogImage, mediaItems.get(0).getCoverUrl(), mediaItems.get(0).getMediaUrl());
        return new XhsParseResult(
                noteId == null || noteId.isEmpty() ? "unknown" : noteId,
                pageUrl,
                canonicalUrl,
                author,
                title,
                coverUrl,
                parseStrategy,
                entrySource,
                mediaItems
        );
    }

    private static String extractMeta(Document document, String key) {
        String selector = "meta[property=" + key + "],meta[name=" + key + "]";
        Elements elements = document.select(selector);
        if (elements.isEmpty()) {
            return null;
        }
        for (Element element : elements) {
            String content = element.attr("content");
            if (content != null && !content.trim().isEmpty()) {
                return content.trim();
            }
        }
        return null;
    }

    private static JsonObject extractInitialState(Document document) {
        for (Element script : document.getElementsByTag("script")) {
            String data = firstNonEmpty(script.data(), script.html());
            if (data == null || !data.contains("window.__INITIAL_STATE__=")) {
                continue;
            }
            String rawState = data.substring(data.indexOf("window.__INITIAL_STATE__=") + "window.__INITIAL_STATE__=".length());
            rawState = sanitizeStateJson(rawState);
            if (rawState.isEmpty()) {
                return null;
            }
            try {
                return new JsonParser().parse(rawState).getAsJsonObject();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static String sanitizeStateJson(String rawState) {
        if (rawState == null) {
            return "";
        }
        String sanitized = rawState.trim();
        if (sanitized.endsWith(";")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        sanitized = sanitized.replace(":undefined", ":null")
                .replace(":void 0", ":null")
                .replace(":!0", ":true")
                .replace(":!1", ":false");
        return sanitized;
    }

    private static JsonObject findNoteObject(JsonElement node, int depth) {
        if (node == null || node.isJsonNull() || depth > 12) {
            return null;
        }
        if (node.isJsonObject()) {
            JsonObject object = node.getAsJsonObject();
            if (isNoteObject(object)) {
                return object;
            }
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                JsonObject childObject = findNoteObject(entry.getValue(), depth + 1);
                if (childObject != null) {
                    return childObject;
                }
            }
        } else if (node.isJsonArray()) {
            JsonArray array = node.getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject childObject = findNoteObject(element, depth + 1);
                if (childObject != null) {
                    return childObject;
                }
            }
        }
        return null;
    }

    private static boolean isNoteObject(JsonObject object) {
        if (object == null) {
            return false;
        }
        boolean hasMedia = object.has("imageList") || object.has("video");
        boolean hasIdentity = object.has("noteId") || object.has("title") || object.has("user");
        return hasMedia && hasIdentity;
    }

    private static ArrayList<XhsMediaItem> extractImageItems(JsonObject noteObject, String noteId) {
        ArrayList<XhsMediaItem> imageItems = new ArrayList<>();
        JsonArray imageList = getArray(noteObject, "imageList");
        if (imageList == null) {
            return imageItems;
        }
        for (int i = 0; i < imageList.size(); i++) {
            JsonObject imageObject = getObject(imageList.get(i));
            if (imageObject == null) {
                continue;
            }
            String imageUrl = pickImageUrl(imageObject);
            if (imageUrl == null || !XhsNetworkPolicy.isAllowedMediaUrl(imageUrl)) {
                continue;
            }
            imageItems.add(new XhsMediaItem(
                    buildMediaId(noteId, i + 1),
                    XhsMediaType.IMAGE,
                    imageUrl,
                    imageUrl,
                    getInt(imageObject, "width"),
                    getInt(imageObject, "height"),
                    0,
                    guessExtension(imageUrl, XhsMediaType.IMAGE),
                    true
            ));
        }
        return imageItems;
    }

    private static String pickImageUrl(JsonObject imageObject) {
        String directUrl = normalizeResourceUrl(firstNonEmpty(
                getString(imageObject, "urlDefault"),
                getString(imageObject, "urlPre"),
                getString(imageObject, "url")
        ));
        if (directUrl != null) {
            return directUrl;
        }
        JsonArray infoList = getArray(imageObject, "infoList");
        if (infoList == null) {
            return null;
        }
        for (int i = infoList.size() - 1; i >= 0; i--) {
            JsonObject infoObject = getObject(infoList.get(i));
            if (infoObject == null) {
                continue;
            }
            String url = normalizeResourceUrl(getString(infoObject, "url"));
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    private static String extractUserName(JsonObject noteObject) {
        if (noteObject == null) {
            return null;
        }
        JsonObject user = getObject(noteObject, "user");
        if (user == null) {
            return null;
        }
        return firstNonEmpty(
                getString(user, "nickname"),
                getString(user, "nickName"),
                getString(user, "name")
        );
    }

    private static String extractVideoUrl(JsonObject noteObject) {
        if (noteObject == null) {
            return null;
        }
        return findFirstMediaUrl(noteObject.get("video"), true, 0);
    }

    private static int extractVideoDuration(JsonObject noteObject) {
        if (noteObject == null) {
            return 0;
        }
        JsonObject videoObject = getObject(noteObject.get("video"));
        if (videoObject != null) {
            int duration = getInt(videoObject, "duration");
            if (duration > 0) {
                return duration;
            }
        }
        return 0;
    }

    private static String extractVideoCoverUrl(JsonObject noteObject, String videoUrl) {
        if (noteObject == null) {
            return null;
        }
        return findFirstCoverUrl(noteObject.get("video"), videoUrl, "", 0);
    }

    /**
     * 只接受字段语义或 URL 形态明显像封面的候选，避免把播放清单误当成图片资源。
     */
    private static String findFirstCoverUrl(JsonElement node, String videoUrl, String fieldPath, int depth) {
        if (node == null || node.isJsonNull() || depth > 12) {
            return null;
        }
        if (node.isJsonObject()) {
            JsonObject object = node.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String childPath = fieldPath.isEmpty() ? entry.getKey() : fieldPath + "." + entry.getKey();
                String childUrl = findFirstCoverUrl(entry.getValue(), videoUrl, childPath, depth + 1);
                if (childUrl != null) {
                    return childUrl;
                }
            }
        } else if (node.isJsonArray()) {
            JsonArray array = node.getAsJsonArray();
            for (JsonElement element : array) {
                String childUrl = findFirstCoverUrl(element, videoUrl, fieldPath, depth + 1);
                if (childUrl != null) {
                    return childUrl;
                }
            }
        } else if (node.isJsonPrimitive()) {
            String candidate = normalizeResourceUrl(node.getAsString());
            if (isLikelyVideoCoverUrl(candidate, videoUrl, fieldPath)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isLikelyVideoCoverUrl(String candidate, String videoUrl, String fieldPath) {
        if (candidate == null || !XhsNetworkPolicy.isAllowedMediaUrl(candidate) || candidate.equals(videoUrl)) {
            return false;
        }
        String lowerUrl = candidate.toLowerCase(Locale.US);
        if (lowerUrl.contains(".mp4") || lowerUrl.contains(".m3u8") || lowerUrl.contains(".mpd")) {
            return false;
        }
        return isLikelyCoverField(fieldPath) || isLikelyImageUrl(lowerUrl);
    }

    private static boolean isLikelyCoverField(String fieldPath) {
        if (fieldPath == null || fieldPath.isEmpty()) {
            return false;
        }
        String lowerPath = fieldPath.toLowerCase(Locale.US);
        return lowerPath.contains("cover")
                || lowerPath.contains("poster")
                || lowerPath.contains("image")
                || lowerPath.contains("thumbnail")
                || lowerPath.contains("thumb");
    }

    private static boolean isLikelyImageUrl(String lowerUrl) {
        return lowerUrl.contains(".jpg")
                || lowerUrl.contains(".jpeg")
                || lowerUrl.contains(".png")
                || lowerUrl.contains(".webp")
                || lowerUrl.contains(".gif")
                || lowerUrl.contains(".heic")
                || lowerUrl.contains(".avif");
    }

    private static String findFirstMediaUrl(JsonElement node, boolean preferMp4, int depth) {
        if (node == null || node.isJsonNull() || depth > 12) {
            return null;
        }
        if (node.isJsonObject()) {
            JsonObject object = node.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive()) {
                    String candidate = normalizeResourceUrl(value.getAsString());
                    if (candidate != null && XhsNetworkPolicy.isAllowedMediaUrl(candidate)) {
                        if (!preferMp4 || candidate.toLowerCase(Locale.US).contains(".mp4")) {
                            return candidate;
                        }
                    }
                }
                String childUrl = findFirstMediaUrl(value, preferMp4, depth + 1);
                if (childUrl != null) {
                    return childUrl;
                }
            }
        } else if (node.isJsonArray()) {
            JsonArray array = node.getAsJsonArray();
            for (JsonElement element : array) {
                String childUrl = findFirstMediaUrl(element, preferMp4, depth + 1);
                if (childUrl != null) {
                    return childUrl;
                }
            }
        } else if (node.isJsonPrimitive()) {
            String candidate = normalizeResourceUrl(node.getAsString());
            if (candidate != null && XhsNetworkPolicy.isAllowedMediaUrl(candidate)) {
                if (!preferMp4 || candidate.toLowerCase(Locale.US).contains(".mp4")) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static String cleanMetaTitle(String ogTitle) {
        if (ogTitle == null) {
            return null;
        }
        return ogTitle.replace(" - 小红书", "").trim();
    }

    private static int parseDuration(String durationText) {
        if (durationText == null || durationText.trim().isEmpty()) {
            return 0;
        }
        String[] items = durationText.split(":");
        if (items.length == 2) {
            return parseSafeInt(items[0]) * 60 + parseSafeInt(items[1]);
        }
        if (items.length == 3) {
            return parseSafeInt(items[0]) * 3600 + parseSafeInt(items[1]) * 60 + parseSafeInt(items[2]);
        }
        return 0;
    }

    private static int parseSafeInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String normalizeResourceUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String normalizedUrl = url.trim()
                .replace("\\u002F", "/")
                .replace("\\/", "/");
        normalizedUrl = XhsNetworkPolicy.forceHttps(normalizedUrl);
        if (normalizedUrl.startsWith("https://http")) {
            normalizedUrl = normalizedUrl.substring("https://".length());
        }
        return normalizedUrl;
    }

    private static String extractNoteId(String url) {
        if (url == null) {
            return null;
        }
        Matcher matcher = NOTE_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private static String buildMediaId(String noteId, int index) {
        return (noteId == null || noteId.isEmpty() ? "note" : noteId) + "_" + index;
    }

    private static String guessExtension(String url, XhsMediaType mediaType) {
        if (url == null || url.isEmpty()) {
            return mediaType == XhsMediaType.VIDEO ? "mp4" : "jpg";
        }
        String lowerUrl = url.toLowerCase(Locale.US);
        int dotIndex = lowerUrl.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < lowerUrl.length() - 1) {
            String extension = lowerUrl.substring(dotIndex + 1);
            int queryIndex = extension.indexOf('?');
            if (queryIndex > 0) {
                extension = extension.substring(0, queryIndex);
            }
            if (extension.length() <= 5) {
                return extension;
            }
        }
        return mediaType == XhsMediaType.VIDEO ? "mp4" : "jpg";
    }

    private static String firstNonEmpty(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            if (candidate != null && !candidate.trim().isEmpty()) {
                return candidate.trim();
            }
        }
        return null;
    }

    private static JsonObject getObject(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return null;
        }
        return element.getAsJsonObject();
    }

    private static JsonObject getObject(JsonObject parent, String memberName) {
        if (parent == null || !parent.has(memberName)) {
            return null;
        }
        return getObject(parent.get(memberName));
    }

    private static JsonArray getArray(JsonObject parent, String memberName) {
        if (parent == null || !parent.has(memberName)) {
            return null;
        }
        JsonElement element = parent.get(memberName);
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return null;
        }
        return element.getAsJsonArray();
    }

    private static String getString(JsonObject parent, String memberName) {
        if (parent == null || !parent.has(memberName)) {
            return null;
        }
        JsonElement element = parent.get(memberName);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            return element.getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int getInt(JsonObject parent, String memberName) {
        if (parent == null || !parent.has(memberName)) {
            return 0;
        }
        JsonElement element = parent.get(memberName);
        if (element == null || element.isJsonNull()) {
            return 0;
        }
        try {
            return element.getAsInt();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
