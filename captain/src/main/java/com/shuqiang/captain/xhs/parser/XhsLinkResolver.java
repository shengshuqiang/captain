package com.shuqiang.captain.xhs.parser;

import android.util.Log;

import com.shuqiang.captain.xhs.model.XhsParseError;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * 只在短链场景补一次真实地址解析，避免把短链依赖扩散到后续流程。
 */
public final class XhsLinkResolver {
    private static final String TAG = "XhsLinkResolver";

    private XhsLinkResolver() {
    }

    public static String resolveFinalUrl(String url) throws XhsParserException {
        String normalizedUrl = XhsNetworkPolicy.forceHttps(url);
        if (!XhsNetworkPolicy.isAllowedPageUrl(normalizedUrl)) {
            throw new XhsParserException(XhsParseError.INVALID_INPUT);
        }
        if (XhsInputParser.isDirectLink(normalizedUrl)) {
            return normalizedUrl;
        }
        Request request = XhsHttpClient.buildPageRequest(normalizedUrl, XhsHttpClient.DESKTOP_USER_AGENT);
        try (Response response = XhsHttpClient.getClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.w(TAG, "resolveFinalUrl failed, code=" + response.code());
                throw new XhsParserException(XhsParseError.SHORT_LINK_EXPIRED);
            }
            String finalUrl = response.request().url().toString();
            finalUrl = XhsNetworkPolicy.forceHttps(finalUrl);
            if (!XhsInputParser.isDirectLink(finalUrl) || !XhsNetworkPolicy.isAllowedPageUrl(finalUrl)) {
                throw new XhsParserException(XhsParseError.SHORT_LINK_EXPIRED);
            }
            return finalUrl;
        } catch (IOException e) {
            throw new XhsParserException(XhsParseError.NETWORK_ERROR, e);
        }
    }
}
