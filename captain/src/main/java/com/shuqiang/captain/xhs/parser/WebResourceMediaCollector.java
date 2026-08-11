package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * 汇总 WebView 运行期间的资源请求和最终 DOM 媒体，并为加载过程资源给出保守的默认选择状态。
 */
final class WebResourceMediaCollector {
    private static final int MAX_CANDIDATE_COUNT = 200;
    private static final int MIN_CONTENT_EDGE_PX = 240;
    private static final long MIN_CONTENT_AREA_PX = 40_000L;
    private static final float MAX_CONTENT_ASPECT_RATIO = 4.0F;
    private static final String[] TRANSIENT_HINTS = {
            "avatar", "favicon", " icon", "icon-", "-icon", "logo", "sprite", "badge",
            "emoji", "placeholder", "loading", "spinner", "skeleton", "default-image",
            "error-image", "blur", "low-quality", "low_quality", "/board-thumb/",
            "banner", "advert", "tracking", "pixel", "mymyads", "/delivery/lg"
    };

    private final String pageUrl;
    private final String entrySource;
    private final LinkedHashMap<String, Candidate> candidates = new LinkedHashMap<>();
    private String pageTitle;

    WebResourceMediaCollector(String pageUrl, String entrySource) {
        this.pageUrl = pageUrl;
        this.entrySource = entrySource;
    }

    synchronized void setPageTitle(String pageTitle) {
        if (pageTitle != null && !pageTitle.trim().isEmpty()) {
            this.pageTitle = pageTitle.trim();
        }
    }

    synchronized void observeRequest(String rawUrl) {
        String normalizedUrl = normalizeUrl(rawUrl);
        XhsMediaType mediaType = inferRequestMediaType(normalizedUrl);
        if (mediaType != null) {
            mergeCandidate(normalizedUrl, mediaType, 0, 0, 0, 0,
                    false, false, "request");
        }
    }

    synchronized void observeDom(String rawUrl, String kind, int renderedWidth, int renderedHeight,
                                 int sourceWidth, int sourceHeight, boolean visible,
                                 boolean previewReady, boolean finalSnapshot, String hints) {
        String normalizedUrl = normalizeUrl(rawUrl);
        XhsMediaType mediaType = inferDomMediaType(kind, normalizedUrl);
        if (mediaType != null) {
            mergeCandidate(
                    normalizedUrl,
                    mediaType,
                    renderedWidth,
                    renderedHeight,
                    sourceWidth,
                    sourceHeight,
                    visible && finalSnapshot,
                    previewReady,
                    hints
            );
        }
    }

    synchronized int size() {
        return candidates.size();
    }

    synchronized XhsParseResult buildResult() {
        if (candidates.isEmpty()) {
            return null;
        }
        List<Candidate> orderedCandidates = new ArrayList<>(candidates.values());
        Collections.sort(orderedCandidates, new Comparator<Candidate>() {
            @Override
            public int compare(Candidate left, Candidate right) {
                int selectedOrder = Boolean.compare(isSelectedByDefault(right), isSelectedByDefault(left));
                if (selectedOrder != 0) {
                    return selectedOrder;
                }
                int domOrder = Boolean.compare(right.observedInFinalDom, left.observedInFinalDom);
                if (domOrder != 0) {
                    return domOrder;
                }
                return Integer.compare(left.order, right.order);
            }
        });

        String noteId = buildPageId(pageUrl);
        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>();
        String coverUrl = findDefaultCover(orderedCandidates);
        int index = 1;
        for (Candidate candidate : orderedCandidates) {
            boolean selected = isSelectedByDefault(candidate);
            mediaItems.add(new XhsMediaItem(
                    noteId + "_" + index,
                    candidate.mediaType,
                    candidate.url,
                    candidate.mediaType == XhsMediaType.IMAGE ? candidate.url : coverUrl,
                    candidate.getPreferredWidth(),
                    candidate.getPreferredHeight(),
                    0,
                    guessExtension(candidate.url, candidate.mediaType),
                    selected
            ));
            index++;
        }
        if (coverUrl == null) {
            coverUrl = mediaItems.get(0).getMediaUrl();
        }
        return new XhsParseResult(
                noteId,
                pageUrl,
                pageUrl,
                extractHost(pageUrl),
                pageTitle == null ? "网页资源" : pageTitle,
                coverUrl,
                "WebView 资源监测 · 可预览大图默认选中",
                entrySource,
                mediaItems
        );
    }

    private static String findDefaultCover(List<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            if (candidate.mediaType == XhsMediaType.IMAGE && isSelectedByDefault(candidate)) {
                return candidate.url;
            }
        }
        for (Candidate candidate : candidates) {
            if (candidate.mediaType == XhsMediaType.IMAGE) {
                return candidate.url;
            }
        }
        return null;
    }

    private void mergeCandidate(String normalizedUrl, XhsMediaType mediaType,
                                int renderedWidth, int renderedHeight,
                                int sourceWidth, int sourceHeight,
                                boolean observedInFinalDom, boolean previewReady, String hints) {
        if (normalizedUrl == null) {
            return;
        }
        Candidate existing = candidates.get(normalizedUrl);
        if (existing == null) {
            if (candidates.size() >= MAX_CANDIDATE_COUNT) {
                return;
            }
            candidates.put(normalizedUrl, new Candidate(
                    normalizedUrl,
                    mediaType,
                    Math.max(0, renderedWidth),
                    Math.max(0, renderedHeight),
                    Math.max(0, sourceWidth),
                    Math.max(0, sourceHeight),
                    observedInFinalDom,
                    previewReady,
                    hints,
                    candidates.size()
            ));
            return;
        }
        existing.mergeRenderedSize(renderedWidth, renderedHeight);
        existing.mergeSourceSize(sourceWidth, sourceHeight);
        existing.observedInFinalDom |= observedInFinalDom;
        existing.contentSizedFinalPlacement |= observedInFinalDom
                && isContentSize(renderedWidth, renderedHeight);
        existing.previewReady |= previewReady;
        if (hints != null && !hints.trim().isEmpty()) {
            existing.hints = (existing.hints + " " + hints).trim();
        }
    }

    private static boolean isSelectedByDefault(Candidate candidate) {
        if (candidate.mediaType != XhsMediaType.IMAGE) {
            return true;
        }
        if (!candidate.observedInFinalDom
                || !candidate.contentSizedFinalPlacement
                || !candidate.previewReady
                || hasTransientHint(candidate.url + " " + candidate.hints)) {
            return false;
        }
        return isContentSize(candidate.sourceWidth, candidate.sourceHeight);
    }

    private static boolean isContentSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return false;
        }
        int maxEdge = Math.max(width, height);
        int minEdge = Math.min(width, height);
        long area = (long) width * height;
        float aspectRatio = minEdge <= 0 ? Float.MAX_VALUE : (float) maxEdge / minEdge;
        return maxEdge >= MIN_CONTENT_EDGE_PX
                && area >= MIN_CONTENT_AREA_PX
                && aspectRatio <= MAX_CONTENT_ASPECT_RATIO;
    }

    private static boolean hasTransientHint(String value) {
        String lowerValue = value == null ? "" : value.toLowerCase(Locale.US);
        for (String hint : TRANSIENT_HINTS) {
            if (lowerValue.contains(hint)) {
                return true;
            }
        }
        return false;
    }

    private static XhsMediaType inferRequestMediaType(String url) {
        String lowerUrl = url == null ? "" : url.toLowerCase(Locale.US);
        if (containsAny(lowerUrl, ".m3u8", ".css", ".js", ".woff", ".woff2", ".ttf", ".json")) {
            return null;
        }
        if (containsAny(lowerUrl, ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp", ".avif", ".heic")) {
            return XhsMediaType.IMAGE;
        }
        if (containsAny(lowerUrl, ".mp4", ".webm", ".mov", ".m4v")) {
            return XhsMediaType.VIDEO;
        }
        if (lowerUrl.contains(".pdf")) {
            return XhsMediaType.PDF;
        }
        return null;
    }

    private static XhsMediaType inferDomMediaType(String kind, String url) {
        if (url == null) {
            return null;
        }
        if ("image".equalsIgnoreCase(kind)) {
            return XhsMediaType.IMAGE;
        }
        if ("video".equalsIgnoreCase(kind)) {
            return url.toLowerCase(Locale.US).contains(".m3u8") ? null : XhsMediaType.VIDEO;
        }
        if ("pdf".equalsIgnoreCase(kind)) {
            return XhsMediaType.PDF;
        }
        return inferRequestMediaType(url);
    }

    private static String normalizeUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return null;
        }
        String normalizedUrl = rawUrl.trim().replace("&amp;", "&").replace("\\u002F", "/").replace("\\/", "/");
        if (normalizedUrl.startsWith("//")) {
            normalizedUrl = "https:" + normalizedUrl;
        }
        int fragmentIndex = normalizedUrl.indexOf('#');
        if (fragmentIndex > 0) {
            normalizedUrl = normalizedUrl.substring(0, fragmentIndex);
        }
        return XhsNetworkPolicy.isAllowedMediaUrl(normalizedUrl) ? normalizedUrl : null;
    }

    private static boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String guessExtension(String url, XhsMediaType mediaType) {
        String path = null;
        try {
            path = new URI(url).getPath();
        } catch (Exception ignored) {
            // 无后缀动态资源按媒体类型使用默认扩展名。
        }
        if (path != null) {
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < path.length() - 1) {
                String extension = path.substring(dotIndex + 1).toLowerCase(Locale.US);
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

    private static String buildPageId(String url) {
        String host = extractHost(url);
        String hostPart = host == null || host.isEmpty() ? "web" : host.replace('.', '_');
        int hash = url == null ? 0 : Math.abs(url.hashCode());
        return hostPart + "_" + hash;
    }

    private static String extractHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static final class Candidate {
        private final String url;
        private final XhsMediaType mediaType;
        private final int order;
        private int renderedWidth;
        private int renderedHeight;
        private int sourceWidth;
        private int sourceHeight;
        private boolean observedInFinalDom;
        private boolean contentSizedFinalPlacement;
        private boolean previewReady;
        private String hints;

        private Candidate(String url, XhsMediaType mediaType,
                          int renderedWidth, int renderedHeight,
                          int sourceWidth, int sourceHeight,
                          boolean observedInFinalDom, boolean previewReady,
                          String hints, int order) {
            this.url = url;
            this.mediaType = mediaType;
            this.renderedWidth = renderedWidth;
            this.renderedHeight = renderedHeight;
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
            this.observedInFinalDom = observedInFinalDom;
            this.contentSizedFinalPlacement = observedInFinalDom
                    && isContentSize(renderedWidth, renderedHeight);
            this.previewReady = previewReady;
            this.hints = hints == null ? "" : hints;
            this.order = order;
        }

        private void mergeRenderedSize(int width, int height) {
            if (area(width, height) > area(renderedWidth, renderedHeight)) {
                renderedWidth = Math.max(0, width);
                renderedHeight = Math.max(0, height);
            }
        }

        private void mergeSourceSize(int width, int height) {
            if (area(width, height) > area(sourceWidth, sourceHeight)) {
                sourceWidth = Math.max(0, width);
                sourceHeight = Math.max(0, height);
            }
        }

        private int getPreferredWidth() {
            return sourceWidth > 0 ? sourceWidth : renderedWidth;
        }

        private int getPreferredHeight() {
            return sourceHeight > 0 ? sourceHeight : renderedHeight;
        }

        private static long area(int width, int height) {
            return (long) Math.max(0, width) * Math.max(0, height);
        }
    }
}
