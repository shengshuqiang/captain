package com.shuqiang.captain.xhs.parser;

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

    public XhsParseResult parse(String rawText, String entrySource) throws XhsParserException {
        long startTime = System.currentTimeMillis();
        XhsParseInput parseInput = XhsInputParser.parse(rawText, entrySource);
        String linkType = parseInput.isShortLink() ? "short_link" : "direct_link";
        String finalUrl = parseInput.isShortLink()
                ? XhsLinkResolver.resolveFinalUrl(parseInput.getExtractedUrl())
                : parseInput.getExtractedUrl();
        if (!XhsNetworkPolicy.isAllowedPageUrl(finalUrl)) {
            throw new XhsParserException(XhsParseError.INVALID_INPUT);
        }
        PageFetchResult pageFetchResult = fetchPage(finalUrl);
        XhsParseResult parseResult = XhsHtmlParser.parse(
                pageFetchResult.html,
                finalUrl,
                parseInput.getEntrySource(),
                pageFetchResult.strategy
        );
        Log.d(TAG, "parse finished, entry=" + entrySource
                + ", linkType=" + linkType
                + ", noteId=" + parseResult.getNoteId()
                + ", strategy=" + parseResult.getParseStrategy()
                + ", mediaCount=" + parseResult.getMediaCount()
                + ", durationMs=" + (System.currentTimeMillis() - startTime));
        return parseResult;
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
    }
}
