package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseInput;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用网页入口只负责从分享文案中提取首个网页链接，不绑定具体平台。
 */
public final class GenericWebInputParser {
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://[^\\s]+)");
    private static final String TRAILING_TRIM_CHARS = "。！？!?,，；;）)]}>\"'";

    private GenericWebInputParser() {
    }

    public static XhsParseInput parse(String rawText, String entrySource) throws XhsParserException {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new XhsParserException(XhsParseError.INVALID_INPUT);
        }
        Matcher matcher = URL_PATTERN.matcher(rawText);
        while (matcher.find()) {
            String extractedUrl = normalizeUrl(matcher.group(1));
            if (extractedUrl == null || extractedUrl.isEmpty()) {
                continue;
            }
            return new XhsParseInput(rawText, extractedUrl, entrySource, false);
        }
        throw new XhsParserException(XhsParseError.INVALID_INPUT);
    }

    private static String normalizeUrl(String url) {
        if (url == null) {
            return null;
        }
        String normalizedUrl = url.trim().replace("&amp;", "&");
        while (!normalizedUrl.isEmpty()
                && TRAILING_TRIM_CHARS.indexOf(normalizedUrl.charAt(normalizedUrl.length() - 1)) >= 0) {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
        }
        return normalizedUrl;
    }
}
