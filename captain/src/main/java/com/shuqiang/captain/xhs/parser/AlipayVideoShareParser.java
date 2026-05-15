package com.shuqiang.captain.xhs.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 支付宝/淘宝视频分享页是 JS 壳页面，真实视频信息藏在 scheme 与详情接口里。
 */
final class AlipayVideoShareParser {
    static final String DETAIL_API_URL = "https://webgw-internet.alipay.com"
            + "/contentservice/com.alipay.sofa.function.SOFAFunction/apply/"
            + "myjf.yuyan.contentservice.default.ContentWebGWController.list";
    static final String APP_ID = "180020010001266490";
    private static final String VIDEO_URL_PREFIX = "https://gw.alipayobjects.com/v/open_content/afts/video/";
    private static final String DEFAULT_CLARITY = "720P_h265";

    private AlipayVideoShareParser() {
    }

    static boolean isSupportedPage(String pageUrl) {
        try {
            URI uri = new URI(pageUrl);
            String host = uri.getHost();
            String path = uri.getPath();
            if (host == null || path == null) {
                return false;
            }
            String lowerHost = host.toLowerCase(Locale.US);
            return "render.alipay.com".equals(lowerHost)
                    && path.endsWith("/video-share.html");
        } catch (Exception ignored) {
            return false;
        }
    }

    static ShareInfo extractShareInfo(String pageUrl) {
        Map<String, String> pageParams = parseUrlQuery(pageUrl);
        String scheme = pageParams.get("scheme");
        Map<String, String> schemeParams = parseUrlQuery(scheme);
        Map<String, String> nestedParams = parseUrlQuery(schemeParams.get("url"));

        VideoInfo videoInfo = firstNonNull(
                parseVideoInfo(pageParams.get("videoInfo")),
                parseVideoInfo(schemeParams.get("videoInfo")),
                parseVideoInfo(nestedParams.get("videoInfo"))
        );
        String contentId = firstNonEmpty(
                pageParams.get("contentId"),
                schemeParams.get("contentId"),
                nestedParams.get("contentId")
        );
        if (contentId == null && videoInfo == null) {
            return null;
        }
        return new ShareInfo(contentId, videoInfo);
    }

    static XhsParseResult parseDetailResponse(String responseJson,
                                              String pageUrl,
                                              String entrySource,
                                              String requestStrategy,
                                              ShareInfo shareInfo) {
        JsonObject data = extractFirstDataObject(responseJson);
        if (data == null) {
            return null;
        }
        JsonObject video = XhsStateJsonParser.getObject(data, "video");
        String apiVideoUrl = normalizeUrl(XhsStateJsonParser.getString(video, "vid"));
        String shareVideoUrl = buildVideoUrl(shareInfo == null ? null : shareInfo.videoInfo);
        String mediaUrl = firstNonEmpty(shareVideoUrl, apiVideoUrl);
        if (mediaUrl == null || !XhsNetworkPolicy.isAllowedMediaUrl(mediaUrl)) {
            return null;
        }

        String contentId = firstNonEmpty(
                shareInfo == null ? null : shareInfo.contentId,
                getNestedString(data, "ext", "spmExt", "_item_id"),
                XhsStateJsonParser.getString(data, "contentId")
        );
        String coverUrl = normalizeUrl(firstNonEmpty(
                getNestedString(video, "firstFramePic", "url"),
                getNestedString(data, "coverPic", "url")
        ));
        String noteId = firstNonEmpty(contentId, XhsStateJsonParser.getString(video, "djangoId"), buildPageId(pageUrl));
        String title = firstNonEmpty(
                cleanTitle(XhsStateJsonParser.getString(data, "title")),
                cleanTitle(XhsStateJsonParser.getString(data, "content")),
                "支付宝视频"
        );
        String authorName = firstNonEmpty(getNestedString(data, "author", "nickName"), "支付宝");
        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>();
        mediaItems.add(new XhsMediaItem(
                noteId + "_1",
                XhsMediaType.VIDEO,
                mediaUrl,
                coverUrl,
                getInt(video, "widthRatio"),
                getInt(video, "heightRatio"),
                getDurationSec(video),
                "mp4",
                true
        ));
        return new XhsParseResult(
                noteId,
                pageUrl,
                pageUrl,
                authorName,
                title,
                firstNonEmpty(coverUrl, mediaUrl),
                requestStrategy + " · 支付宝视频分享",
                entrySource,
                mediaItems
        );
    }

    static XhsParseResult parseShareInfoOnly(String pageUrl,
                                             String entrySource,
                                             String requestStrategy,
                                             ShareInfo shareInfo) {
        if (shareInfo == null) {
            return null;
        }
        String mediaUrl = buildVideoUrl(shareInfo.videoInfo);
        if (mediaUrl == null || !XhsNetworkPolicy.isAllowedMediaUrl(mediaUrl)) {
            return null;
        }
        String noteId = firstNonEmpty(shareInfo.contentId,
                shareInfo.videoInfo == null ? null : shareInfo.videoInfo.vid,
                buildPageId(pageUrl));
        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>();
        mediaItems.add(new XhsMediaItem(
                noteId + "_1",
                XhsMediaType.VIDEO,
                mediaUrl,
                null,
                0,
                0,
                0,
                "mp4",
                true
        ));
        return new XhsParseResult(
                noteId,
                pageUrl,
                pageUrl,
                "支付宝",
                "支付宝视频",
                mediaUrl,
                requestStrategy + " · 支付宝视频分享",
                entrySource,
                mediaItems
        );
    }

    private static JsonObject extractFirstDataObject(String responseJson) {
        if (responseJson == null || responseJson.trim().isEmpty()) {
            return null;
        }
        try {
            JsonObject root = new JsonParser().parse(responseJson).getAsJsonObject();
            JsonObject resultObj = XhsStateJsonParser.getObject(root, "resultObj");
            JsonArray dataArray = XhsStateJsonParser.getArray(resultObj, "data");
            if (dataArray == null || dataArray.size() == 0) {
                return null;
            }
            return XhsStateJsonParser.getObject(dataArray.get(0));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static VideoInfo parseVideoInfo(String rawVideoInfo) {
        if (rawVideoInfo == null || rawVideoInfo.trim().isEmpty()) {
            return null;
        }
        try {
            JsonObject jsonObject = new JsonParser().parse(rawVideoInfo).getAsJsonObject();
            String vid = XhsStateJsonParser.getString(jsonObject, "vid");
            String clarity = firstNonEmpty(XhsStateJsonParser.getString(jsonObject, "clarity"), DEFAULT_CLARITY);
            if (!isSafePathSegment(vid) || !isSafePathSegment(clarity)) {
                return null;
            }
            return new VideoInfo(vid, clarity);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String buildVideoUrl(VideoInfo videoInfo) {
        if (videoInfo == null || !isSafePathSegment(videoInfo.vid)) {
            return null;
        }
        String clarity = isSafePathSegment(videoInfo.clarity) ? videoInfo.clarity : DEFAULT_CLARITY;
        return VIDEO_URL_PREFIX + videoInfo.vid + "/" + clarity;
    }

    private static Map<String, String> parseUrlQuery(String url) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        if (url == null || url.trim().isEmpty()) {
            return params;
        }
        String query = extractRawQuery(url);
        if (query == null || query.trim().isEmpty()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int equalsIndex = pair.indexOf('=');
            String rawKey = equalsIndex >= 0 ? pair.substring(0, equalsIndex) : pair;
            String rawValue = equalsIndex >= 0 ? pair.substring(equalsIndex + 1) : "";
            String key = urlDecode(rawKey);
            if (key == null || key.isEmpty()) {
                continue;
            }
            params.put(key, urlDecode(rawValue));
        }
        return params;
    }

    private static String extractRawQuery(String url) {
        try {
            URI uri = new URI(url);
            String rawQuery = uri.getRawQuery();
            if (rawQuery != null) {
                return rawQuery;
            }
        } catch (Exception ignored) {
            // 回退到字符串裁剪，兼容 alipays:// 与相对路径。
        }
        int queryIndex = url.indexOf('?');
        if (queryIndex < 0 || queryIndex == url.length() - 1) {
            return null;
        }
        int anchorIndex = url.indexOf('#', queryIndex);
        return anchorIndex >= 0 ? url.substring(queryIndex + 1, anchorIndex) : url.substring(queryIndex + 1);
    }

    private static String urlDecode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            return value;
        } catch (IllegalArgumentException ignored) {
            return value;
        }
    }

    private static String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String normalizedUrl = XhsNetworkPolicy.forceHttps(url.trim().replace("&amp;", "&"));
        int anchorIndex = normalizedUrl.indexOf('#');
        if (anchorIndex > 0) {
            normalizedUrl = normalizedUrl.substring(0, anchorIndex);
        }
        return normalizedUrl;
    }

    private static boolean isSafePathSegment(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]+");
    }

    private static String getNestedString(JsonObject parent, String firstKey, String secondKey) {
        return XhsStateJsonParser.getString(XhsStateJsonParser.getObject(parent, firstKey), secondKey);
    }

    private static String getNestedString(JsonObject parent, String firstKey, String secondKey, String thirdKey) {
        return XhsStateJsonParser.getString(
                XhsStateJsonParser.getObject(XhsStateJsonParser.getObject(parent, firstKey), secondKey),
                thirdKey
        );
    }

    private static int getInt(JsonObject parent, String memberName) {
        JsonElement element = parent == null ? null : parent.get(memberName);
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return 0;
        }
        try {
            return element.getAsInt();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static int getDurationSec(JsonObject video) {
        JsonElement element = video == null ? null : video.get("duration");
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return 0;
        }
        try {
            return (int) Math.round(element.getAsDouble());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String cleanTitle(String title) {
        if (title == null) {
            return null;
        }
        String cleaned = title.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static String buildPageId(String url) {
        int hash = url == null ? 0 : Math.abs(url.hashCode());
        return "alipay_video_" + hash;
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
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

    static final class ShareInfo {
        final String contentId;
        final VideoInfo videoInfo;

        ShareInfo(String contentId, VideoInfo videoInfo) {
            this.contentId = contentId;
            this.videoInfo = videoInfo;
        }
    }

    private static final class VideoInfo {
        final String vid;
        final String clarity;

        VideoInfo(String vid, String clarity) {
            this.vid = vid;
            this.clarity = clarity;
        }
    }
}
