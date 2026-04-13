package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.junit.Assert;
import org.junit.Test;

public class GenericWebSniffParserTest {

    @Test
    public void parseResolvesAllHeadShareImages() throws Exception {
        String html = "<html><head>"
                + "<title>分享页</title>"
                + "<meta property=\"og:image\" content=\"/assets/share-cover\"/>"
                + "<meta property=\"og:image:secure_url\" content=\"https://cdn.example.com/images/share-b.webp?x=1\"/>"
                + "<meta name=\"twitter:image\" content=\"https://cdn.example.com/images/share-c\"/>"
                + "</head><body></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/post/123",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals(3, parseResult.getMediaCount());
        Assert.assertEquals("https://example.com/assets/share-cover", parseResult.getCoverUrl());
        Assert.assertEquals(XhsMediaType.IMAGE, parseResult.getMediaItems().get(0).getMediaType());
        Assert.assertEquals("https://example.com/assets/share-cover", parseResult.getMediaItems().get(0).getMediaUrl());
    }

    @Test
    public void parseKeepsVideoPrimaryAndAddsHeadCoverImage() throws Exception {
        String html = "<html><head>"
                + "<title>视频页</title>"
                + "<meta property=\"og:video\" content=\"/media/demo-video\"/>"
                + "<meta itemprop=\"image\" content=\"/media/demo-cover\"/>"
                + "</head><body></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/post/video",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals(2, parseResult.getMediaCount());
        Assert.assertEquals(XhsMediaType.VIDEO, parseResult.getPrimaryMediaType());
        Assert.assertEquals("https://example.com/media/demo-cover", parseResult.getMediaItems().get(0).getCoverUrl());
        Assert.assertEquals(XhsMediaType.IMAGE, parseResult.getMediaItems().get(1).getMediaType());
    }

    @Test
    public void parseIgnoresNonUrlOgImageMetadata() throws Exception {
        String html = "<html><head>"
                + "<title>分享页</title>"
                + "<meta property=\"og:image\" content=\"https://cdn.example.com/images/share-cover.jpg\"/>"
                + "<meta property=\"og:image:width\" content=\"1200\"/>"
                + "<meta property=\"og:image:height\" content=\"630\"/>"
                + "<meta name=\"twitter:image:alt\" content=\"share cover\"/>"
                + "<link rel=\"icon\" type=\"image/png\" href=\"/favicon.png\"/>"
                + "</head><body></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/post/123",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals("https://cdn.example.com/images/share-cover.jpg",
                parseResult.getMediaItems().get(0).getMediaUrl());
    }
}
