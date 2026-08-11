package com.shuqiang.captain.xhs.parser;

import org.junit.Assert;
import org.junit.Test;

public class WebViewResourceSnifferTest {

    @Test
    public void canSniffRejectsMissingAndUnsupportedUrls() {
        Assert.assertFalse(WebViewResourceSniffer.canSniff(null));
        Assert.assertFalse(WebViewResourceSniffer.canSniff(""));
        Assert.assertFalse(WebViewResourceSniffer.canSniff("file:///sdcard/local.html"));
    }

    @Test
    public void canSniffAcceptsHttpPageUrls() {
        Assert.assertTrue(WebViewResourceSniffer.canSniff(
                "https://jkforum.net/p/forum-234-4.html?orderby=dateline"
        ));
    }
}
