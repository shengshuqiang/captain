package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用网页资源嗅探，尽量从 meta、DOM 和内联脚本中提取图片、视频和 PDF。
 */
public final class GenericWebSniffParser {
    private static final Pattern RAW_URL_PATTERN = Pattern.compile(
            "(https?:\\\\?/\\\\?/[^\\\"'<>\\s]+|https?://[^\\\"'<>\\s]+)"
    );

    private GenericWebSniffParser() {
    }

    public static XhsParseResult parse(String html, String pageUrl, String entrySource, String requestStrategy)
            throws XhsParserException {
        if (html == null || html.trim().isEmpty()) {
            throw new XhsParserException(XhsParseError.NOTE_UNAVAILABLE);
        }
        Document document = Jsoup.parse(html, pageUrl);
        String canonicalUrl = firstNonEmpty(
                normalizeUrl(document.select("link[rel=canonical]").attr("abs:href")),
                normalizeUrl(extractMeta(document, "og:url")),
                pageUrl
        );
        String pageTitle = firstNonEmpty(
                cleanTitle(extractMeta(document, "og:title")),
                cleanTitle(document.title()),
                "网页资源"
        );
        String authorOrSite = firstNonEmpty(
                cleanTitle(extractMeta(document, "author")),
                cleanTitle(extractMeta(document, "og:site_name")),
                extractHost(canonicalUrl)
        );

        LinkedHashMap<String, XhsMediaItem> mediaMap = new LinkedHashMap<>();
        LinkedHashSet<String> imageCandidates = new LinkedHashSet<>();

        collectMetaCandidates(document, pageUrl, mediaMap, imageCandidates);
        collectDomCandidates(document, pageUrl, mediaMap, imageCandidates);
        collectRawHtmlCandidates(html, pageUrl, mediaMap, imageCandidates);

        if (mediaMap.isEmpty()) {
            throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
        }

        String noteId = buildPageId(canonicalUrl);
        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>();
        int index = 1;
        for (XhsMediaItem item : mediaMap.values()) {
            mediaItems.add(new XhsMediaItem(
                    buildMediaId(noteId, index),
                    item.getMediaType(),
                    item.getMediaUrl(),
                    item.getCoverUrl(),
                    item.getWidth(),
                    item.getHeight(),
                    item.getDurationSec(),
                    item.getFileExtension(),
                    true
            ));
            index++;
        }
        String coverUrl = firstNonEmpty(
                findFirstImageCover(mediaItems),
                imageCandidates.isEmpty() ? null : imageCandidates.iterator().next(),
                mediaItems.get(0).getCoverUrl(),
                mediaItems.get(0).getMediaUrl()
        );
        return new XhsParseResult(
                noteId,
                pageUrl,
                canonicalUrl,
                authorOrSite,
                pageTitle,
                coverUrl,
                requestStrategy + " · 通用网页嗅探",
                entrySource,
                mediaItems
        );
    }

    private static void collectMetaCandidates(Document document, String pageUrl,
                                              Map<String, XhsMediaItem> mediaMap,
                                              LinkedHashSet<String> imageCandidates) {
        addTypedCandidate(mediaMap, imageCandidates, normalizeUrl(extractMeta(document, "og:video")), XhsMediaType.VIDEO, null);
        addTypedCandidate(mediaMap, imageCandidates, normalizeUrl(extractMeta(document, "twitter:player:stream")), XhsMediaType.VIDEO, null);
        addTypedCandidate(mediaMap, imageCandidates, normalizeUrl(extractMeta(document, "og:image")), XhsMediaType.IMAGE, null);
        addTypedCandidate(mediaMap, imageCandidates, normalizeUrl(extractMeta(document, "twitter:image")), XhsMediaType.IMAGE, null);
        addTypedCandidate(mediaMap, imageCandidates, normalizeUrl(extractMeta(document, "twitter:image:src")), XhsMediaType.IMAGE, null);

        Elements metaElements = document.select("meta[content]");
        for (Element metaElement : metaElements) {
            String content = normalizeUrl(metaElement.attr("content"));
            if (content == null) {
                continue;
            }
            XhsMediaType mediaType = inferMediaType(content, metaElement.attr("property") + " " + metaElement.attr("name"));
            addTypedCandidate(mediaMap, imageCandidates, content, mediaType, null);
        }
    }

    private static void collectDomCandidates(Document document, String pageUrl,
                                             Map<String, XhsMediaItem> mediaMap,
                                             LinkedHashSet<String> imageCandidates) {
        for (Element element : document.select("img[src],img[data-src],img[data-original]")) {
            String imageUrl = firstNonEmpty(
                    normalizeUrl(element.attr("abs:src")),
                    normalizeUrl(element.attr("abs:data-src")),
                    normalizeUrl(element.attr("abs:data-original"))
            );
            addTypedCandidate(mediaMap, imageCandidates, imageUrl, XhsMediaType.IMAGE, imageUrl);
        }
        for (Element element : document.select("video[src],source[src]")) {
            String mediaUrl = normalizeUrl(element.attr("abs:src"));
            String coverUrl = normalizeUrl(element.attr("abs:poster"));
            XhsMediaType mediaType = inferMediaType(mediaUrl, element.attr("type"));
            addTypedCandidate(mediaMap, imageCandidates, mediaUrl, mediaType, coverUrl);
        }
        for (Element element : document.select("a[href],link[href],embed[src],object[data],iframe[src]")) {
            String candidateUrl = firstNonEmpty(
                    normalizeUrl(element.attr("abs:href")),
                    normalizeUrl(element.attr("abs:src")),
                    normalizeUrl(element.attr("abs:data"))
            );
            String hint = element.attr("type") + " " + element.className();
            addTypedCandidate(mediaMap, imageCandidates, candidateUrl, inferMediaType(candidateUrl, hint), null);
        }
    }

    private static void collectRawHtmlCandidates(String html, String pageUrl,
                                                 Map<String, XhsMediaItem> mediaMap,
                                                 LinkedHashSet<String> imageCandidates) {
        Matcher matcher = RAW_URL_PATTERN.matcher(html);
        while (matcher.find()) {
            String candidateUrl = matcher.group(1).replace("\\/", "/");
            addTypedCandidate(mediaMap, imageCandidates, normalizeUrl(candidateUrl), inferMediaType(candidateUrl, ""), null);
        }
    }

    private static void addTypedCandidate(Map<String, XhsMediaItem> mediaMap,
                                          LinkedHashSet<String> imageCandidates,
                                          String candidateUrl,
                                          XhsMediaType mediaType,
                                          String coverUrl) {
        if (candidateUrl == null || mediaType == null || !XhsNetworkPolicy.isAllowedMediaUrl(candidateUrl)) {
            return;
        }
        String normalizedUrl = normalizeUrl(candidateUrl);
        if (normalizedUrl == null || mediaMap.containsKey(normalizedUrl)) {
            return;
        }
        String normalizedCover = normalizeUrl(coverUrl);
        if (mediaType == XhsMediaType.IMAGE) {
            imageCandidates.add(normalizedUrl);
        } else if (normalizedCover != null) {
            imageCandidates.add(normalizedCover);
        }
        mediaMap.put(normalizedUrl, new XhsMediaItem(
                normalizedUrl,
                mediaType,
                normalizedUrl,
                normalizedCover,
                0,
                0,
                0,
                guessExtension(normalizedUrl, mediaType),
                true
        ));
    }

    private static String extractMeta(Document document, String key) {
        Element element = document.selectFirst("meta[property=" + key + "],meta[name=" + key + "]");
        if (element == null) {
            return null;
        }
        String content = element.attr("content");
        return content == null || content.trim().isEmpty() ? null : content.trim();
    }

    private static XhsMediaType inferMediaType(String url, String hint) {
        String lowerUrl = url == null ? "" : url.toLowerCase(Locale.US);
        String lowerHint = hint == null ? "" : hint.toLowerCase(Locale.US);
        if (lowerUrl.contains(".pdf") || lowerHint.contains("application/pdf") || lowerHint.contains("pdf")) {
            return XhsMediaType.PDF;
        }
        if (containsAny(lowerUrl, ".mp4", ".m3u8", ".webm", ".mov", ".m4v")
                || containsAny(lowerHint, "video/", "og:video", "twitter:player")) {
            return XhsMediaType.VIDEO;
        }
        if (containsAny(lowerUrl, ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp", ".avif", ".heic")
                || containsAny(lowerHint, "image/", "og:image", "twitter:image")) {
            return XhsMediaType.IMAGE;
        }
        return null;
    }

    private static boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String guessExtension(String mediaUrl, XhsMediaType mediaType) {
        if (mediaUrl != null) {
            String lowerUrl = mediaUrl.toLowerCase(Locale.US);
            int questionIndex = lowerUrl.indexOf('?');
            if (questionIndex >= 0) {
                lowerUrl = lowerUrl.substring(0, questionIndex);
            }
            int dotIndex = lowerUrl.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < lowerUrl.length() - 1) {
                String extension = lowerUrl.substring(dotIndex + 1);
                if (extension.length() <= 5) {
                    return extension;
                }
            }
        }
        if (mediaType == XhsMediaType.VIDEO) {
            return "mp4";
        }
        if (mediaType == XhsMediaType.PDF) {
            return "pdf";
        }
        return "jpg";
    }

    /**
     * 通用嗅探不能只用域名做文件前缀，否则同域不同页面会互相覆盖。
     */
    private static String buildPageId(String url) {
        String host = extractHost(url);
        String hostPart = (host == null || host.isEmpty()) ? "web" : host.replace('.', '_');
        int hash = url == null ? 0 : Math.abs(url.hashCode());
        return hostPart + "_" + hash;
    }

    private static String buildMediaId(String noteId, int index) {
        return noteId + "_" + index;
    }

    private static String extractHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String normalizedUrl = url.trim().replace("&amp;", "&");
        if (normalizedUrl.startsWith("//")) {
            normalizedUrl = "https:" + normalizedUrl;
        }
        int anchorIndex = normalizedUrl.indexOf('#');
        if (anchorIndex > 0) {
            normalizedUrl = normalizedUrl.substring(0, anchorIndex);
        }
        return normalizedUrl;
    }

    private static String cleanTitle(String title) {
        if (title == null) {
            return null;
        }
        String cleaned = title.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String findFirstImageCover(ArrayList<XhsMediaItem> mediaItems) {
        for (XhsMediaItem item : mediaItems) {
            if (item.getMediaType() == XhsMediaType.IMAGE) {
                return item.getMediaUrl();
            }
            if (item.getCoverUrl() != null && !item.getCoverUrl().isEmpty()) {
                return item.getCoverUrl();
            }
        }
        return null;
    }
}
