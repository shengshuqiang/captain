package com.shuqiang.captain.xhs.model;

import java.io.Serializable;

/**
 * 解析入口入参，保留原始文本和归一化后的链接，便于日志与调试。
 */
public class XhsParseInput implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String originalText;
    private final String extractedUrl;
    private final String entrySource;
    private final boolean shortLink;

    public XhsParseInput(String originalText, String extractedUrl, String entrySource, boolean shortLink) {
        this.originalText = originalText;
        this.extractedUrl = extractedUrl;
        this.entrySource = entrySource;
        this.shortLink = shortLink;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getExtractedUrl() {
        return extractedUrl;
    }

    public String getEntrySource() {
        return entrySource;
    }

    public boolean isShortLink() {
        return shortLink;
    }
}
