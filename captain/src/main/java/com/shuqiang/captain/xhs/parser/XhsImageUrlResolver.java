package com.shuqiang.captain.xhs.parser;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 从小红书 CDN 展示图还原原始图片标识，避免下载网页水印/缩略图模板。 */
public final class XhsImageUrlResolver {
    private static final String ORIGINAL_IMAGE_ORIGIN = "https://ci.xiaohongshu.com/";
    private static final String JPEG_PROCESSING = "?imageView2/format/jpeg";
    private static final Pattern WEB_IMAGE_PATH = Pattern.compile(
            "^/\\d{12,14}/[a-zA-Z0-9]+/([a-zA-Z0-9_/-]+)(?:!.*)?$");
    private static final Pattern IMAGE_KEY = Pattern.compile(
            "(?:[a-zA-Z0-9_-]+/)*[a-zA-Z0-9_-]{20,}");

    private XhsImageUrlResolver() {}

    /** 解析器共用原图输出格式，避免各自猜测无扩展名 CDN 路径。 */
    static boolean isOriginalJpegUrl(String url) {
        return url != null && url.startsWith(ORIGINAL_IMAGE_ORIGIN) && url.endsWith(JPEG_PROCESSING);
    }

    /** 页面明确提供 fileId 时优先使用，避免依赖会过期的展示模板 URL。 */
    static String fromFileId(String fileId) {
        return fileId != null && IMAGE_KEY.matcher(fileId).matches()
                ? ORIGINAL_IMAGE_ORIGIN + fileId + JPEG_PROCESSING : null;
    }

    /** 只处理明确的图片 CDN 和已知原图标识；普通图片/视频地址保持原样。 */
    public static String resolve(String url) {
        if (url == null) return null;
        try {
            URI uri = new URI(XhsNetworkPolicy.forceHttps(url));
            String host = uri.getHost();
            String path = uri.getRawPath();
            if (host == null || path == null) return url;
            host = host.toLowerCase(Locale.US);
            String key = null;
            if (host.matches("sns-webpic-[a-z0-9-]+\\.xhscdn\\.(com|net)")) {
                Matcher match = WEB_IMAGE_PATH.matcher(path);
                if (match.matches()) key = match.group(1);
            } else if (host.equals("ci.xiaohongshu.com")
                    || host.matches("sns-img-[a-z0-9-]+\\.xhscdn\\.(com|net)")) {
                key = path.substring(1).split("!", 2)[0];
            }
            String original = fromFileId(key);
            if (original == null) return url;
            // 固定输出 JPEG，使文件扩展名和原图 CDN 的返回格式一致。
            return original;
        } catch (Exception ignored) {
            return url;
        }
    }
}
