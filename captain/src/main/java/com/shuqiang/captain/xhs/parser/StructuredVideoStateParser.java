package com.shuqiang.captain.xhs.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jsoup.nodes.Document;

import java.util.Locale;
import java.util.Map;

/**
 * 从常见页面 JSON 状态中按字段语义提取第一组主视频，避免依赖 URL 文件后缀。
 */
final class StructuredVideoStateParser {
    private static final int MAX_DEPTH = 24;
    private static final int MAX_VISITED_NODES = 12_000;
    private static final String[] STRONG_VIDEO_ADDRESS_KEYS = {
            "playaddr",
            "downloadaddr",
            "videourl"
    };
    private static final String[] CONTEXTUAL_VIDEO_ADDRESS_KEYS = {
            "playurl",
            "downloadurl",
            "streamurl",
            "playurlinfo"
    };
    private static final String[] COVER_KEYS = {
            "cover",
            "origincover",
            "dynamiccover",
            "poster",
            "thumbnail",
            "thumbnailurl",
            "coverurl",
            "image"
    };

    private StructuredVideoStateParser() {
    }

    static Result extract(Document document) {
        for (JsonObject state : XhsStateJsonParser.extractStructuredStates(document)) {
            TraversalBudget budget = new TraversalBudget();
            Result result = findFirstVideo(state, 0, budget, false);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * 只返回第一组高置信视频地址，避免把页面里的相关推荐视频一并当成当前作品。
     */
    private static Result findFirstVideo(JsonElement element,
                                         int depth,
                                         TraversalBudget budget,
                                         boolean videoContext) {
        if (element == null || element.isJsonNull() || depth > MAX_DEPTH || !budget.consume()) {
            return null;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                Result result = findFirstVideo(child, depth + 1, budget, videoContext);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        boolean isVideoObject = isVideoObject(object);
        boolean currentVideoContext = videoContext || isVideoObject;
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (!isVideoAddressKey(entry.getKey(), currentVideoContext, isVideoObject)) {
                continue;
            }
            String videoUrl = findFirstUrl(entry.getValue(), depth + 1, budget, true);
            if (videoUrl != null) {
                return new Result(videoUrl, findCoverUrl(object, depth + 1, budget), normalizeKey(entry.getKey()));
            }
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String childKey = normalizeKey(entry.getKey());
            boolean childVideoContext = currentVideoContext || isExplicitVideoContainer(childKey);
            if (isExplicitNonVideoContainer(childKey)) {
                childVideoContext = false;
            }
            Result result = findFirstVideo(entry.getValue(), depth + 1, budget, childVideoContext);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static String findCoverUrl(JsonObject videoObject, int depth, TraversalBudget budget) {
        for (Map.Entry<String, JsonElement> entry : videoObject.entrySet()) {
            if (!matches(entry.getKey(), COVER_KEYS)) {
                continue;
            }
            String coverUrl = findFirstUrl(entry.getValue(), depth + 1, budget, false);
            if (coverUrl != null) {
                return coverUrl;
            }
        }
        return null;
    }

    private static String findFirstUrl(JsonElement element, int depth, TraversalBudget budget, boolean videoCandidate) {
        if (element == null || element.isJsonNull() || depth > MAX_DEPTH || !budget.consume()) {
            return null;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return normalizeMediaUrl(element.getAsString(), videoCandidate);
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) {
                String url = findFirstUrl(child, depth + 1, budget, videoCandidate);
                if (url != null) {
                    return url;
                }
            }
            return null;
        }
        if (!element.isJsonObject()) {
            return null;
        }
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            String url = findFirstUrl(entry.getValue(), depth + 1, budget, videoCandidate);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    private static String normalizeMediaUrl(String rawUrl, boolean videoCandidate) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return null;
        }
        String normalizedUrl = rawUrl.trim()
                .replace("\\u002F", "/")
                .replace("\\u002f", "/")
                .replace("\\/", "/")
                .replace("&amp;", "&");
        normalizedUrl = XhsNetworkPolicy.forceHttps(normalizedUrl);
        if (!XhsNetworkPolicy.isAllowedMediaUrl(normalizedUrl)) {
            return null;
        }
        String lowerUrl = normalizedUrl.toLowerCase(Locale.US);
        if (videoCandidate && (isHls(lowerUrl) || isObviousImage(lowerUrl))) {
            return null;
        }
        return normalizedUrl;
    }

    private static boolean isVideoAddressKey(String key, boolean videoContext, boolean isVideoObject) {
        String normalizedKey = normalizeKey(key);
        if ("contenturl".equals(normalizedKey)) {
            return isVideoObject;
        }
        if (!videoContext) {
            return false;
        }
        return matches(normalizedKey, STRONG_VIDEO_ADDRESS_KEYS)
                || matches(normalizedKey, CONTEXTUAL_VIDEO_ADDRESS_KEYS)
                || normalizedKey.startsWith("playaddr")
                || normalizedKey.startsWith("downloadaddr")
                || normalizedKey.endsWith("videourl");
    }

    private static boolean isVideoObject(JsonObject object) {
        if (object == null || !object.has("@type") || !object.get("@type").isJsonPrimitive()) {
            return false;
        }
        return "videoobject".equals(normalizeKey(object.get("@type").getAsString()));
    }

    private static boolean isExplicitVideoContainer(String normalizedKey) {
        return "video".equals(normalizedKey)
                || "videoinfo".equals(normalizedKey)
                || "videoinfores".equals(normalizedKey)
                || "videodata".equals(normalizedKey)
                || "videoobject".equals(normalizedKey);
    }

    private static boolean isExplicitNonVideoContainer(String normalizedKey) {
        return "audio".equals(normalizedKey)
                || "music".equals(normalizedKey)
                || "image".equals(normalizedKey)
                || "images".equals(normalizedKey)
                || "avatar".equals(normalizedKey)
                || "cover".equals(normalizedKey)
                || "origincover".equals(normalizedKey)
                || "dynamiccover".equals(normalizedKey)
                || "poster".equals(normalizedKey)
                || "thumbnail".equals(normalizedKey)
                || "thumbnailurl".equals(normalizedKey)
                || "coverurl".equals(normalizedKey);
    }

    private static boolean matches(String key, String[] candidates) {
        String normalizedKey = normalizeKey(key);
        for (String candidate : candidates) {
            if (candidate.equals(normalizedKey)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
    }

    private static boolean isHls(String lowerUrl) {
        return lowerUrl.contains(".m3u8")
                || lowerUrl.contains("application/vnd.apple.mpegurl")
                || lowerUrl.contains("application/x-mpegurl");
    }

    private static boolean isObviousImage(String lowerUrl) {
        int queryIndex = lowerUrl.indexOf('?');
        String path = queryIndex >= 0 ? lowerUrl.substring(0, queryIndex) : lowerUrl;
        return path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".png")
                || path.endsWith(".webp")
                || path.endsWith(".gif")
                || path.endsWith(".avif")
                || path.endsWith(".heic");
    }

    static final class Result {
        final String videoUrl;
        final String coverUrl;
        final String sourceKey;

        private Result(String videoUrl, String coverUrl, String sourceKey) {
            this.videoUrl = videoUrl;
            this.coverUrl = coverUrl;
            this.sourceKey = sourceKey;
        }
    }

    private static final class TraversalBudget {
        private int visitedNodes;

        private boolean consume() {
            visitedNodes++;
            return visitedNodes <= MAX_VISITED_NODES;
        }
    }
}
