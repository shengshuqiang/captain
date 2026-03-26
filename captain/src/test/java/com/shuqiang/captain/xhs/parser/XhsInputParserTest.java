package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseInput;

import org.junit.Assert;
import org.junit.Test;

public class XhsInputParserTest {

    @Test
    public void parsePrefersDirectLinkWhenCopyTextContainsTwoUrls() throws Exception {
        String rawText = "sunshine☀️ http://xhslink.com/o/10hSPfUM2Th\n"
                + "https://www.xiaohongshu.com/explore/6832d99d0000000021003156?xsec_source=app_share";

        XhsParseInput parseInput = XhsInputParser.parse(rawText, "manual_input");

        Assert.assertFalse(parseInput.isShortLink());
        Assert.assertEquals(
                "https://www.xiaohongshu.com/explore/6832d99d0000000021003156?xsec_source=app_share",
                parseInput.getExtractedUrl()
        );
    }

    @Test
    public void parseThrowsWhenNoXhsUrlExists() {
        try {
            XhsInputParser.parse("这里只是一段普通文本", "manual_input");
            Assert.fail("should throw parser exception");
        } catch (XhsParserException exception) {
            Assert.assertEquals(XhsParseError.INVALID_INPUT, exception.getParseError());
        }
    }
}
