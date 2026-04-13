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
    private static final String[] HEAD_IMAGE_KEYS = {
            "og:image",
            "og:image:url",
            "og:image:secure_url",
            "twitter:image",
            "twitter:image:src",
            "image",
            "thumbnail",
            "thumbnailurl"
    };
    private static final String[] HEAD_VIDEO_KEYS = {
            "og:video",
            "og:video:url",
            "og:video:secure_url",
            "twitter:player:stream",
            "video",
            "contenturl"
    };
    private static final String[] HEAD_PDF_KEYS = {
            "pdf"
    };

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
        LinkedHashSet<String> headImageCandidates = extractHeadImageCandidates(document, pageUrl);
        String defaultCoverUrl = firstCandidate(headImageCandidates);

        collectMetaCandidates(document, pageUrl, mediaMap, imageCandidates, headImageCandidates, defaultCoverUrl);
        collectDomCandidates(document, pageUrl, mediaMap, imageCandidates, defaultCoverUrl);
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
                                              LinkedHashSet<String> imageCandidates,
                                              LinkedHashSet<String> headImageCandidates,
                                              String defaultCoverUrl) {
        Elements headElements = document.select("head meta[content],head link[href]");
        for (Element headElement : headElements) {
            XhsMediaType mediaType = resolveHeadMediaType(headElement);
            if (mediaType != XhsMediaType.VIDEO && mediaType != XhsMediaType.PDF) {
                continue;
            }
            String content = resolveHeadUrl(headElement, pageUrl);
            if (content == null) {
                continue;
            }
            addTypedCandidate(mediaMap, imageCandidates, content, mediaType, defaultCoverUrl);
        }
        for (String imageUrl : headImageCandidates) {
            addTypedCandidate(mediaMap, imageCandidates, imageUrl, XhsMediaType.IMAGE, imageUrl);
        }
    }

    private static void collectDomCandidates(Document document, String pageUrl,
                                             Map<String, XhsMediaItem> mediaMap,
                                             LinkedHashSet<String> imageCandidates,
                                             String defaultCoverUrl) {
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
            String coverUrl = firstNonEmpty(
                    normalizeUrl(element.attr("abs:poster")),
                    normalizeUrl(pageUrl, element.attr("poster")),
                    normalizeUrl(pageUrl, element.attr("data-poster")),
                    defaultCoverUrl
            );
            XhsMediaType mediaType = inferMediaType(mediaUrl, element.attr("type"));
            addTypedCandidate(mediaMap, imageCandidates, mediaUrl, mediaType, coverUrl);
        }
        for (Element element : document.select("a[href],link[href],embed[src],object[data],iframe[src]")) {
            if (isInHead(element)) {
                continue;
            }
            String candidateUrl = firstNonEmpty(
                    normalizeUrl(element.attr("abs:href")),
                    normalizeUrl(element.attr("abs:src")),
                    normalizeUrl(element.attr("abs:data"))
            );
            String hint = element.attr("type") + " " + element.className();
            XhsMediaType mediaType = inferMediaType(candidateUrl, hint);
            addTypedCandidate(mediaMap, imageCandidates, candidateUrl, mediaType,
                    mediaType == XhsMediaType.VIDEO ? defaultCoverUrl : null);
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

    /**
     * head 里的分享图常常只有相对路径或无后缀 URL，需要单独按标签语义识别并解析成绝对地址。
     */
    private static LinkedHashSet<String> extractHeadImageCandidates(Document document, String pageUrl) {
        LinkedHashSet<String> headImageCandidates = new LinkedHashSet<>();
        Elements headElements = document.select("head meta[content],head link[href]");
        for (Element headElement : headElements) {
            if (resolveHeadMediaType(headElement) != XhsMediaType.IMAGE) {
                continue;
            }
            String candidateUrl = resolveHeadUrl(headElement, pageUrl);
            if (candidateUrl == null) {
                continue;
            }
            headImageCandidates.add(candidateUrl);
        }
        return headImageCandidates;
    }

    private static String resolveHeadUrl(Element headElement, String pageUrl) {
        return firstNonEmpty(
                normalizeUrl(pageUrl, headElement.attr("content")),
                normalizeUrl(headElement.attr("abs:href")),
                normalizeUrl(pageUrl, headElement.attr("href"))
        );
    }

    private static XhsMediaType resolveHeadMediaType(Element headElement) {
        if (headElement == null) {
            return null;
        }
        String property = headElement.attr("property");
        String name = headElement.attr("name");
        String itemprop = headElement.attr("itemprop");
        String rel = headElement.attr("rel");
        String type = headElement.attr("type");
        String as = headElement.attr("as");
        if (matchesAny(property, HEAD_IMAGE_KEYS) || matchesAny(name, HEAD_IMAGE_KEYS) || matchesAny(itemprop, HEAD_IMAGE_KEYS)
                || ("image_src".equalsIgnoreCase(rel))
                || ("preload".equalsIgnoreCase(rel) && "image".equalsIgnoreCase(as))) {
            return XhsMediaType.IMAGE;
        }
        if (matchesAny(property, HEAD_VIDEO_KEYS) || matchesAny(name, HEAD_VIDEO_KEYS) || matchesAny(itemprop, HEAD_VIDEO_KEYS)
                || ("video".equalsIgnoreCase(type) || (type != null && type.toLowerCase(Locale.US).startsWith("video/")))
                || ("preload".equalsIgnoreCase(rel) && "video".equalsIgnoreCase(as))) {
            return XhsMediaType.VIDEO;
        }
        if (matchesAny(property, HEAD_PDF_KEYS) || matchesAny(name, HEAD_PDF_KEYS) || matchesAny(itemprop, HEAD_PDF_KEYS)
                || "application/pdf".equalsIgnoreCase(type)) {
            return XhsMediaType.PDF;
        }
        return null;
    }

    private static boolean matchesAny(String value, String... candidates) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        for (String candidate : candidates) {
            if (candidate.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInHead(Element element) {
        return element != null && element.parents().select("head").first() != null;
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
        if (containsAny(lowerHint, "image/", "og:image", "twitter:image", "itemprop=\"image\"", "itemprop=image",
                "thumbnail", "image_src", "poster", "video:image", "preload image", "as=image")
                || containsAny(lowerUrl, ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp", ".avif", ".heic")) {
            return XhsMediaType.IMAGE;
        }
        if (containsAny(lowerUrl, ".mp4", ".m3u8", ".webm", ".mov", ".m4v")
                || containsAny(lowerHint, "video/", "og:video", "twitter:player", "itemprop=\"video\"", "itemprop=video", "videoobject")) {
            return XhsMediaType.VIDEO;
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
        return normalizeUrl(null, url);
    }

    private static String normalizeUrl(String pageUrl, String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String normalizedUrl = url.trim().replace("&amp;", "&");
        if (normalizedUrl.startsWith("//")) {
            normalizedUrl = "https:" + normalizedUrl;
        } else if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
            if (pageUrl == null || pageUrl.trim().isEmpty()) {
                return null;
            }
            try {
                normalizedUrl = URI.create(pageUrl).resolve(normalizedUrl).toString();
            } catch (Exception ignored) {
                return null;
            }
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

    private static String firstCandidate(LinkedHashSet<String> candidates) {
        return candidates == null || candidates.isEmpty() ? null : candidates.iterator().next();
    }
}
