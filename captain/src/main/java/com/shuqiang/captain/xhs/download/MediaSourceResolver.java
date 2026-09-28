package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsRequestMode;
import com.shuqiang.captain.xhs.parser.HlsMediaCandidateParser;
import com.shuqiang.captain.xhs.parser.PornhubMediaParser;
import com.shuqiang.captain.xhs.parser.XhsHttpClient;
import com.shuqiang.captain.xhs.parser.XhsNetworkPolicy;

import java.io.IOException;

/**
 * 下载开始时按稳定页面来源重新解析短效 URL，并复用同一 Cookie 会话下载后续资源。
 */
public final class MediaSourceResolver {
    private final DownloadHttpSession httpSession;
    private final RuntimeMediaSession runtimeSession;

    public MediaSourceResolver(DownloadHttpSession httpSession) {
        this(httpSession, null);
    }

    public MediaSourceResolver(DownloadHttpSession httpSession, RuntimeMediaSession runtimeSession) {
        this.httpSession = httpSession;
        this.runtimeSession = runtimeSession;
    }

    public ResolvedMediaSource resolve(XhsDownloadItem item) throws IOException {
        if (item.requiresRuntimeSession()) {
            return resolveRuntimeCandidate(item);
        }
        String userAgent = resolveUserAgent(item.getRequestMode());
        if (item.getSourceKey() == null || item.getSourceKey().trim().isEmpty()) {
            if (!XhsNetworkPolicy.isAllowedMediaUrl(item.getDirectUrl())) {
                throw new IOException("媒体地址无效");
            }
            return new ResolvedMediaSource(item.getDirectUrl(), item.getTransport(), userAgent,
                    item.getSourcePageUrl(), item.getQualityHeight(), null, 0);
        }
        if (!XhsNetworkPolicy.isAllowedMediaUrl(item.getSourcePageUrl())) {
            throw new IOException("来源页面地址无效");
        }
        String html = httpSession.readText(item.getSourcePageUrl(), userAgent, item.getSourcePageUrl(),
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        XhsMediaItem resolvedItem = resolveFromPage(item, html);
        if (resolvedItem == null || resolvedItem.getTransport() != item.getTransport()) {
            throw new IOException("来源已变化，未找到所选画质");
        }
        XhsMediaItem fallback = null;
        if (item.getTransport() == XhsMediaTransport.HLS_STREAM
                && PornhubMediaParser.SOURCE_HLS.equals(item.getSourceKey())) {
            fallback = PornhubMediaParser.findResolvedItem(html, item.getSourcePageUrl(),
                    PornhubMediaParser.SOURCE_DIRECT, 0);
        } else if (item.getTransport() == XhsMediaTransport.HLS_STREAM
                && HlsMediaCandidateParser.SOURCE_KEY.equals(item.getSourceKey())) {
            fallback = HlsMediaCandidateParser.findResolvedItem(html, item.getSourcePageUrl(),
                    HlsMediaCandidateParser.SOURCE_DIRECT_KEY, 0);
        }
        return new ResolvedMediaSource(resolvedItem.getMediaUrl(), resolvedItem.getTransport(), userAgent,
                item.getSourcePageUrl(), resolvedItem.getQualityHeight(),
                fallback == null ? null : fallback.getMediaUrl(), fallback == null ? 0 : fallback.getQualityHeight());
    }

    private ResolvedMediaSource resolveRuntimeCandidate(XhsDownloadItem item) throws IOException {
        if (runtimeSession == null) {
            throw new IOException("浏览会话已失效，请重新验证并提取");
        }
        RuntimeMediaSession.Candidate candidate = runtimeSession.resolve(item.getRuntimeCandidateId());
        if (candidate == null || candidate.getTransport() != item.getTransport()
                || !XhsNetworkPolicy.isAllowedMediaUrl(candidate.getUrl())) {
            throw new IOException("浏览会话已失效，请重新验证并提取");
        }
        return new ResolvedMediaSource(candidate.getUrl(), candidate.getTransport(),
                runtimeSession.getUserAgent(), candidate.getReferer(), candidate.getQualityHeight(), null, 0);
    }

    private XhsMediaItem resolveFromPage(XhsDownloadItem item, String html) {
        if (PornhubMediaParser.SOURCE_HLS.equals(item.getSourceKey())
                || PornhubMediaParser.SOURCE_DIRECT.equals(item.getSourceKey())) {
            return PornhubMediaParser.findResolvedItem(html, item.getSourcePageUrl(),
                    item.getSourceKey(), item.getQualityHeight());
        }
        if (HlsMediaCandidateParser.SOURCE_KEY.equals(item.getSourceKey())) {
            return HlsMediaCandidateParser.findResolvedItem(html, item.getSourcePageUrl(),
                    item.getSourceKey(), item.getQualityHeight());
        }
        if (HlsMediaCandidateParser.SOURCE_DIRECT_KEY.equals(item.getSourceKey())) {
            return HlsMediaCandidateParser.findResolvedItem(html, item.getSourcePageUrl(),
                    item.getSourceKey(), item.getQualityHeight());
        }
        return null;
    }

    private String resolveUserAgent(XhsRequestMode requestMode) {
        return requestMode == XhsRequestMode.MOBILE_RETRY || requestMode == XhsRequestMode.WEBVIEW
                ? XhsHttpClient.MOBILE_USER_AGENT : XhsHttpClient.DESKTOP_USER_AGENT;
    }
}
