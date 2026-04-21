package com.shuqiang.captain.xhs.parser;

import android.util.Log;

import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseInput;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * 将输入归一化、短链解析、HTML 抓取和结果构建收敛在一个同步入口中。
 */
public class XhsParseRepository {
    private static final String TAG = "XhsParseRepo";
    private enum ParseRoute {
        XHS,
        GENERIC
    }

    public XhsParseResult parse(String rawText, String entrySource) throws XhsParserException {
        long startTime = System.currentTimeMillis();
        XhsParseInput parseInput = GenericWebInputParser.parse(rawText, entrySource);
        ParseRoute parseRoute = resolveRoute(parseInput.getExtractedUrl());
        String finalUrl;
        String linkType;
        PageFetchResult pageFetchResult;
        XhsParseResult parseResult;
        if (parseRoute == ParseRoute.XHS) {
            XhsParseInput xhsParseInput = XhsInputParser.parse(rawText, entrySource);
            linkType = xhsParseInput.isShortLink() ? "short_link" : "direct_link";
            finalUrl = xhsParseInput.isShortLink()
                    ? XhsLinkResolver.resolveFinalUrl(xhsParseInput.getExtractedUrl())
                    : xhsParseInput.getExtractedUrl();
            if (!XhsNetworkPolicy.isAllowedPageUrl(finalUrl)) {
                throw new XhsParserException(XhsParseError.INVALID_INPUT);
            }
            pageFetchResult = fetchPage(finalUrl);
            parseResult = XhsHtmlParser.parse(
                    pageFetchResult.html,
                    finalUrl,
                    xhsParseInput.getEntrySource(),
                    pageFetchResult.strategy
            );
        } else {
            finalUrl = parseInput.getExtractedUrl();
            linkType = "generic_url";
            pageFetchResult = null;
            parseResult = parseGenericPage(finalUrl, parseInput.getEntrySource());
        }
        Log.d(TAG, "parse finished, entry=" + entrySource
                + ", linkType=" + linkType
                + ", noteId=" + parseResult.getNoteId()
                + ", strategy=" + parseResult.getParseStrategy()
                + ", mediaCount=" + parseResult.getMediaCount()
                + ", durationMs=" + (System.currentTimeMillis() - startTime));
        return parseResult;
    }

    /**
     * 先按域名做平台路由，避免通过异常回退决定解析链路。
     */
    private ParseRoute resolveRoute(String url) {
        if (XhsInputParser.isDirectLink(url) || XhsInputParser.isShortLink(url)) {
            return ParseRoute.XHS;
        }
        return ParseRoute.GENERIC;
    }

    private XhsParseResult parseGenericPage(String pageUrl, String entrySource) throws XhsParserException {
        PageFetchResult desktopResult = executePageRequest(pageUrl, XhsHttpClient.DESKTOP_USER_AGENT, "desktop");
        XhsParseResult desktopParseResult = tryParseGenericResult(desktopResult, pageUrl, entrySource);
        if (hasDownloadableRichMedia(desktopParseResult)) {
            return desktopParseResult;
        }
        if (!shouldTryGenericMobileFallback(pageUrl, desktopParseResult)) {
            return desktopParseResult;
        }
        PageFetchResult mobileResult = executePageRequest(pageUrl, XhsHttpClient.MOBILE_USER_AGENT, "mobile");
        XhsParseResult mobileParseResult = tryParseGenericResult(mobileResult, pageUrl, entrySource);
        XhsParseResult preferredResult = selectPreferredGenericResult(desktopParseResult, mobileParseResult);
        if (preferredResult != null) {
            return preferredResult;
        }
        if (desktopResult.httpCode == 404 || mobileResult.httpCode == 404) {
            throw new XhsParserException(XhsParseError.NOTE_UNAVAILABLE);
        }
        throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
    }

    private XhsParseResult tryParseGenericResult(PageFetchResult pageFetchResult, String pageUrl, String entrySource)
            throws XhsParserException {
        if (pageFetchResult == null || !pageFetchResult.hasHtml()) {
            return null;
        }
        try {
            return GenericWebSniffParser.parse(
                    pageFetchResult.html,
                    pageUrl,
                    entrySource,
                    pageFetchResult.strategy
            );
        } catch (XhsParserException e) {
            if (e.getParseError() == XhsParseError.NO_MEDIA_FOUND
                    || e.getParseError() == XhsParseError.NOTE_UNAVAILABLE) {
                return null;
            }
            throw e;
        }
    }

    /**
     * generic 页优先选出“可下载信息更多”的结果；若 desktop 只剩封面、mobile 能给出视频，则允许切到 mobile。
     */
    static XhsParseResult selectPreferredGenericResult(XhsParseResult primaryResult, XhsParseResult fallbackResult) {
        if (primaryResult == null) {
            return fallbackResult;
        }
        if (fallbackResult == null) {
            return primaryResult;
        }
        int primaryRichMediaCount = countRichMedia(primaryResult);
        int fallbackRichMediaCount = countRichMedia(fallbackResult);
        if (fallbackRichMediaCount > primaryRichMediaCount) {
            return fallbackResult;
        }
        if (fallbackRichMediaCount > 0
                && fallbackRichMediaCount == primaryRichMediaCount
                && fallbackResult.getMediaCount() > primaryResult.getMediaCount()) {
            return fallbackResult;
        }
        return primaryResult;
    }

    private static boolean hasDownloadableRichMedia(XhsParseResult parseResult) {
        return countRichMedia(parseResult) > 0;
    }

    /**
     * 只对“desktop 解析失败”或“B 站这类已知 desktop 信息不完整”的场景补 mobile 回退，避免普通图片页白白多打一跳。
     */
    static boolean shouldTryGenericMobileFallback(String pageUrl, XhsParseResult desktopParseResult) {
        if (desktopParseResult == null) {
            return true;
        }
        return GenericWebSniffParser.isBilibiliHost(pageUrl)
                || GenericWebSniffParser.isBilibiliHost(desktopParseResult.getCanonicalUrl());
    }

    private static int countRichMedia(XhsParseResult parseResult) {
        if (parseResult == null || parseResult.getMediaItems() == null) {
            return 0;
        }
        int richMediaCount = 0;
        for (int i = 0; i < parseResult.getMediaItems().size(); i++) {
            if (parseResult.getMediaItems().get(i).getMediaType() != XhsMediaType.IMAGE) {
                richMediaCount++;
            }
        }
        return richMediaCount;
    }

    private PageFetchResult fetchPage(String pageUrl) throws XhsParserException {
        PageFetchResult desktopResult = executePageRequest(pageUrl, XhsHttpClient.DESKTOP_USER_AGENT, "desktop");
        if (desktopResult.hasUsefulContent()) {
            return desktopResult;
        }
        PageFetchResult mobileResult = executePageRequest(pageUrl, XhsHttpClient.MOBILE_USER_AGENT, "mobile");
        if (mobileResult.hasUsefulContent()) {
            return mobileResult;
        }
        if (desktopResult.httpCode == 404 || mobileResult.httpCode == 404) {
            throw new XhsParserException(XhsParseError.NOTE_UNAVAILABLE);
        }
        throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
    }

    private PageFetchResult executePageRequest(String pageUrl, String userAgent, String strategy)
            throws XhsParserException {
        Request request = XhsHttpClient.buildPageRequest(pageUrl, userAgent);
        try (Response response = XhsHttpClient.getClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    return new PageFetchResult(strategy, response.code(), "");
                }
                throw new XhsParserException(XhsParseError.NETWORK_ERROR);
            }
            String html = response.body() == null ? "" : response.body().string();
            return new PageFetchResult(strategy, response.code(), html);
        } catch (IOException e) {
            throw new XhsParserException(XhsParseError.NETWORK_ERROR, e);
        }
    }

    /**
     * 抓取结果只暴露最少字段，便于后续按策略切换。
     */
    private static class PageFetchResult {
        private final String strategy;
        private final int httpCode;
        private final String html;

        private PageFetchResult(String strategy, int httpCode, String html) {
            this.strategy = strategy;
            this.httpCode = httpCode;
            this.html = html;
        }

        private boolean hasUsefulContent() {
            return XhsHtmlParser.hasUsefulContent(html);
        }

        private boolean hasHtml() {
            return html != null && !html.trim().isEmpty();
        }
    }
}
