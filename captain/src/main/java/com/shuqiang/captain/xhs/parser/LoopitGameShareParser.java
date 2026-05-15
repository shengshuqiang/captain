package com.shuqiang.captain.xhs.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Loopit 分享页的真实游戏资源在 iframe dist 包里，需要先解析 project_share API。
 */
final class LoopitGameShareParser {
    private static final String PROJECT_SHARE_API =
            "https://api.loopit.me/api/v1/game/project_share?project_id=";
    private static final Pattern MEDIA_URL_PATTERN = Pattern.compile(
            "(https?://[^\\\"'`\\\\\\s<>]+?\\.(?:png|jpe?g|webp|gif|avif|bmp|heic)(?:\\?[^\\\"'`\\\\\\s<>]*)?"
                    + "|(?:\\.\\.?/|/)?(?:public|assets|static|images?|character|sprites?)/[^\\\"'`\\\\\\s<>(),]+?\\.(?:png|jpe?g|webp|gif|avif|bmp|heic)(?:\\?[^\\\"'`\\\\\\s<>(),]*)?)",
            Pattern.CASE_INSENSITIVE
    );

    private LoopitGameShareParser() {
    }

    static boolean isLoopitGameShareUrl(String pageUrl) {
        String projectId = extractProjectId(pageUrl);
        if (projectId == null) {
            return false;
        }
        String host = extractHost(pageUrl);
        if (host == null) {
            return false;
        }
        String lowerHost = host.toLowerCase(Locale.US);
        return lowerHost.equals("share.loopit.me")
                || lowerHost.endsWith(".loopit.me")
                || lowerHost.equals("share.pagesapp.net")
                || lowerHost.endsWith(".pagesapp.net");
    }

    static XhsParseResult parse(String pageUrl, String entrySource) throws XhsParserException {
        String projectId = extractProjectId(pageUrl);
        if (projectId == null) {
            throw new XhsParserException(XhsParseError.INVALID_INPUT);
        }
        ProjectShareInfo projectShareInfo = fetchProjectShareInfo(projectId);
        ArrayList<String> imageUrls = new ArrayList<>();
        addIfPresent(imageUrls, projectShareInfo.frontCover);
        addIfPresent(imageUrls, projectShareInfo.authorAvatarUrl);
        imageUrls.addAll(fetchDistImageUrls(projectShareInfo));
        LinkedHashMap<String, XhsMediaItem> mediaMap = new LinkedHashMap<>();
        int index = 1;
        for (String imageUrl : imageUrls) {
            String normalizedUrl = normalizeUrl(projectShareInfo.indexUrl, imageUrl, null);
            if (normalizedUrl == null || mediaMap.containsKey(normalizedUrl)) {
                continue;
            }
            mediaMap.put(normalizedUrl, new XhsMediaItem(
                    projectId + "_" + index,
                    XhsMediaType.IMAGE,
                    normalizedUrl,
                    normalizedUrl,
                    0,
                    0,
                    0,
                    guessExtension(normalizedUrl),
                    true
            ));
            index++;
        }
        if (mediaMap.isEmpty()) {
            throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
        }
        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>(mediaMap.values());
        return new XhsParseResult(
                projectId,
                pageUrl,
                buildCanonicalUrl(projectId),
                projectShareInfo.authorName,
                firstNonEmpty(projectShareInfo.projectDesc, "Loopit Playable Project"),
                firstNonEmpty(projectShareInfo.frontCover, mediaItems.get(0).getMediaUrl()),
                "loopit project_share + dist assets",
                entrySource,
                mediaItems
        );
    }

    static ProjectShareInfo parseProjectShareResponse(String json) throws XhsParserException {
        try {
            JsonObject root = new JsonParser().parse(json).getAsJsonObject();
            if (!getBoolean(root, "success") || getInt(root, "code") != 0) {
                throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
            }
            JsonObject data = getObject(root, "data");
            if (data == null || !getBoolean(data, "success")) {
                throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
            }
            String projectId = getString(data, "project_id");
            String indexUrl = getString(data, "index_url");
            if (projectId == null || indexUrl == null) {
                throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
            }
            return new ProjectShareInfo(
                    projectId,
                    indexUrl,
                    getString(data, "project_desc"),
                    getString(data, "author_name"),
                    getString(data, "author_avatar_url"),
                    getString(data, "front_cover")
            );
        } catch (IllegalStateException e) {
            throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND, e);
        }
    }

    static List<String> extractDistImageUrls(String indexUrl, String html, Map<String, String> textAssets) {
        LinkedHashSet<String> imageUrls = new LinkedHashSet<>();
        if (html == null || html.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Document document = Jsoup.parse(html, indexUrl);
        for (Element element : document.select("img[src],source[src],link[href]")) {
            String candidateUrl = firstNonEmpty(
                    normalizeUrl(indexUrl, element.attr("src"), null),
                    normalizeUrl(indexUrl, element.attr("href"), null)
            );
            addImageUrl(imageUrls, candidateUrl);
        }
        if (textAssets != null) {
            for (Map.Entry<String, String> assetEntry : textAssets.entrySet()) {
                collectTextAssetImageUrls(imageUrls, indexUrl, assetEntry.getKey(), assetEntry.getValue());
            }
        }
        return new ArrayList<>(imageUrls);
    }

    private static ProjectShareInfo fetchProjectShareInfo(String projectId) throws XhsParserException {
        String apiUrl = PROJECT_SHARE_API + urlEncode(projectId);
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("User-Agent", XhsHttpClient.MOBILE_USER_AGENT)
                .header("Accept", "application/json")
                .header("Origin", "https://share.loopit.me")
                .header("Referer", "https://share.loopit.me/")
                .get()
                .build();
        try (Response response = XhsHttpClient.getClient().newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new XhsParserException(XhsParseError.NETWORK_ERROR);
            }
            return parseProjectShareResponse(response.body().string())
                    .withCookieHeader(buildCookieHeader(response.headers("Set-Cookie")));
        } catch (IOException e) {
            throw new XhsParserException(XhsParseError.NETWORK_ERROR, e);
        }
    }

    private static List<String> fetchDistImageUrls(ProjectShareInfo projectShareInfo) throws XhsParserException {
        if (projectShareInfo.indexUrl == null || projectShareInfo.indexUrl.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String html = fetchText(projectShareInfo.indexUrl, projectShareInfo.cookieHeader);
        Document document = Jsoup.parse(html, projectShareInfo.indexUrl);
        LinkedHashMap<String, String> textAssets = new LinkedHashMap<>();
        for (Element element : document.select("script[src],link[href]")) {
            String assetUrl = firstNonEmpty(
                    normalizeUrl(projectShareInfo.indexUrl, element.attr("src"), null),
                    normalizeUrl(projectShareInfo.indexUrl, element.attr("href"), null)
            );
            if (!isTextAssetUrl(assetUrl)) {
                continue;
            }
            try {
                textAssets.put(assetUrl, fetchText(assetUrl, projectShareInfo.cookieHeader));
            } catch (XhsParserException ignored) {
                // 单个静态包失败不阻断其它资源提取。
            }
        }
        return extractDistImageUrls(projectShareInfo.indexUrl, html, textAssets);
    }

    private static String fetchText(String url, String cookieHeader) throws XhsParserException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("User-Agent", XhsHttpClient.MOBILE_USER_AGENT)
                .header("Accept", "*/*")
                .get();
        if (cookieHeader != null && !cookieHeader.isEmpty()) {
            requestBuilder.header("Cookie", cookieHeader);
        }
        try (Response response = XhsHttpClient.getClient().newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new XhsParserException(XhsParseError.NETWORK_ERROR);
            }
            return response.body().string();
        } catch (IOException e) {
            throw new XhsParserException(XhsParseError.NETWORK_ERROR, e);
        }
    }

    private static void collectTextAssetImageUrls(LinkedHashSet<String> imageUrls,
                                                  String indexUrl,
                                                  String assetUrl,
                                                  String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        Matcher matcher = MEDIA_URL_PATTERN.matcher(content);
        while (matcher.find()) {
            String candidateUrl = matcher.group(1);
            if (candidateUrl == null || candidateUrl.contains("${") || candidateUrl.contains("{")
                    || candidateUrl.contains("}")) {
                continue;
            }
            addImageUrl(imageUrls, normalizeUrl(indexUrl, candidateUrl, assetUrl));
        }
    }

    private static void addImageUrl(LinkedHashSet<String> imageUrls, String candidateUrl) {
        String normalizedUrl = normalizeUrl(null, candidateUrl, null);
        if (normalizedUrl == null || !isImageUrl(normalizedUrl) || !XhsNetworkPolicy.isAllowedMediaUrl(normalizedUrl)) {
            return;
        }
        imageUrls.add(normalizedUrl);
    }

    private static void addIfPresent(List<String> values, String value) {
        if (value != null && !value.trim().isEmpty()) {
            values.add(value.trim());
        }
    }

    private static boolean isTextAssetUrl(String url) {
        if (url == null) {
            return false;
        }
        String lowerUrl = stripQuery(url).toLowerCase(Locale.US);
        return lowerUrl.endsWith(".js") || lowerUrl.endsWith(".css") || lowerUrl.endsWith(".json");
    }

    private static boolean isImageUrl(String url) {
        String lowerUrl = stripQuery(url).toLowerCase(Locale.US);
        return lowerUrl.endsWith(".jpg")
                || lowerUrl.endsWith(".jpeg")
                || lowerUrl.endsWith(".png")
                || lowerUrl.endsWith(".webp")
                || lowerUrl.endsWith(".gif")
                || lowerUrl.endsWith(".avif")
                || lowerUrl.endsWith(".bmp")
                || lowerUrl.endsWith(".heic");
    }

    private static String normalizeUrl(String indexUrl, String candidateUrl, String assetUrl) {
        if (candidateUrl == null || candidateUrl.trim().isEmpty()) {
            return null;
        }
        String normalizedUrl = candidateUrl.trim().replace("&amp;", "&");
        if (normalizedUrl.startsWith("url(") && normalizedUrl.endsWith(")")) {
            normalizedUrl = normalizedUrl.substring(4, normalizedUrl.length() - 1).trim();
        }
        normalizedUrl = stripWrappingQuote(normalizedUrl);
        if (normalizedUrl.startsWith("data:")) {
            return null;
        }
        try {
            if (normalizedUrl.startsWith("//")) {
                return "https:" + normalizedUrl;
            }
            if (normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://")) {
                return normalizedUrl;
            }
            if (indexUrl == null || indexUrl.trim().isEmpty()) {
                return null;
            }
            normalizedUrl = normalizePublicAssetPath(normalizedUrl);
            if (normalizedUrl.startsWith("./") || normalizedUrl.startsWith("../")) {
                String baseUrl = assetUrl == null ? indexUrl : assetUrl;
                return URI.create(baseUrl).resolve(normalizedUrl).toString();
            }
            return URI.create(indexUrl).resolve(normalizedUrl).toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String normalizePublicAssetPath(String url) {
        if (url.startsWith("./public/")) {
            return url.substring("./public/".length());
        }
        if (url.startsWith("/public/")) {
            return url.substring("/public/".length());
        }
        if (url.startsWith("public/")) {
            return url.substring("public/".length());
        }
        return url;
    }

    private static String stripWrappingQuote(String value) {
        if (value.length() < 2) {
            return value;
        }
        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static String stripQuery(String url) {
        if (url == null) {
            return "";
        }
        int queryIndex = url.indexOf('?');
        return queryIndex >= 0 ? url.substring(0, queryIndex) : url;
    }

    private static String guessExtension(String mediaUrl) {
        String lowerUrl = stripQuery(mediaUrl).toLowerCase(Locale.US);
        int dotIndex = lowerUrl.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < lowerUrl.length() - 1) {
            String extension = lowerUrl.substring(dotIndex + 1);
            if (extension.length() <= 5) {
                return extension;
            }
        }
        return "jpg";
    }

    private static String buildCookieHeader(List<String> setCookieHeaders) {
        if (setCookieHeaders == null || setCookieHeaders.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (String setCookie : setCookieHeaders) {
            if (setCookie == null || setCookie.trim().isEmpty()) {
                continue;
            }
            String cookiePair = setCookie.split(";", 2)[0].trim();
            if (cookiePair.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append(cookiePair);
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private static String extractProjectId(String pageUrl) {
        try {
            URI uri = new URI(pageUrl);
            String path = uri.getPath();
            if (path == null) {
                return null;
            }
            String[] parts = path.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                if ("game".equals(parts[i]) && !parts[i + 1].trim().isEmpty()) {
                    return parts[i + 1].trim();
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private static String extractHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String buildCanonicalUrl(String projectId) {
        return "https://share.loopit.me/game/" + projectId;
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception ignored) {
            return value;
        }
    }

    private static JsonObject getObject(JsonObject object, String memberName) {
        JsonElement element = object == null ? null : object.get(memberName);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static String getString(JsonObject object, String memberName) {
        JsonElement element = object == null ? null : object.get(memberName);
        return element != null && !element.isJsonNull() ? element.getAsString() : null;
    }

    private static boolean getBoolean(JsonObject object, String memberName) {
        JsonElement element = object == null ? null : object.get(memberName);
        return element != null && !element.isJsonNull() && element.getAsBoolean();
    }

    private static int getInt(JsonObject object, String memberName) {
        JsonElement element = object == null ? null : object.get(memberName);
        return element != null && !element.isJsonNull() ? element.getAsInt() : -1;
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    static final class ProjectShareInfo {
        final String projectId;
        final String indexUrl;
        final String projectDesc;
        final String authorName;
        final String authorAvatarUrl;
        final String frontCover;
        final String cookieHeader;

        private ProjectShareInfo(String projectId,
                                 String indexUrl,
                                 String projectDesc,
                                 String authorName,
                                 String authorAvatarUrl,
                                 String frontCover) {
            this(projectId, indexUrl, projectDesc, authorName, authorAvatarUrl, frontCover, null);
        }

        private ProjectShareInfo(String projectId,
                                 String indexUrl,
                                 String projectDesc,
                                 String authorName,
                                 String authorAvatarUrl,
                                 String frontCover,
                                 String cookieHeader) {
            this.projectId = projectId;
            this.indexUrl = indexUrl;
            this.projectDesc = projectDesc;
            this.authorName = authorName;
            this.authorAvatarUrl = authorAvatarUrl;
            this.frontCover = frontCover;
            this.cookieHeader = cookieHeader;
        }

        private ProjectShareInfo withCookieHeader(String cookieHeader) {
            return new ProjectShareInfo(projectId, indexUrl, projectDesc, authorName, authorAvatarUrl, frontCover, cookieHeader);
        }
    }
}
