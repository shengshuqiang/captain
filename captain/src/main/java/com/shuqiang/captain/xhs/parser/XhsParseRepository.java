package com.shuqiang.captain.xhs.parser;

import android.content.Context;
import android.util.Log;

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
    private final Context context;

    private enum ParseRoute {
        XHS,
        WEIXIN,
        GENERIC
    }

    public XhsParseRepository(Context context) {
        this.context = context;
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
        } else if (parseRoute == ParseRoute.WEIXIN) {
            finalUrl = parseInput.getExtractedUrl();
            linkType = "weixin_sph";
            parseResult = WeixinSphRuntimeParser.parse(context, finalUrl, parseInput.getEntrySource());
        } else {
            finalUrl = parseInput.getExtractedUrl();
            linkType = "generic_url";
            pageFetchResult = fetchGenericPage(finalUrl);
            parseResult = GenericWebSniffParser.parse(
                    pageFetchResult.html,
                    finalUrl,
                    parseInput.getEntrySource(),
                    pageFetchResult.strategy
            );
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
        if (WeixinSphRuntimeParser.isSupportedUrl(url)) {
            return ParseRoute.WEIXIN;
        }
        return ParseRoute.GENERIC;
    }

    private PageFetchResult fetchGenericPage(String pageUrl) throws XhsParserException {
        PageFetchResult desktopResult = executePageRequest(pageUrl, XhsHttpClient.DESKTOP_USER_AGENT, "desktop");
        if (desktopResult.hasHtml()) {
            return desktopResult;
        }
        PageFetchResult mobileResult = executePageRequest(pageUrl, XhsHttpClient.MOBILE_USER_AGENT, "mobile");
        if (mobileResult.hasHtml()) {
            return mobileResult;
        }
        if (desktopResult.httpCode == 404 || mobileResult.httpCode == 404) {
            throw new XhsParserException(XhsParseError.NOTE_UNAVAILABLE);
        }
        throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
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
