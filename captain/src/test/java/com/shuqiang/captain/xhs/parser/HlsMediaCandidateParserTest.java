package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.junit.Assert;
import org.junit.Test;

public class HlsMediaCandidateParserTest {
    @Test
    public void parseAcceptsOnlyExplicitVideoSourcesAndSelectsHighestLabel() {
        String html = "<html><head><title>HLS</title>"
                + "<meta property=\"og:image\" content=\"https://cdn.example.com/cover.jpg\"/>"
                + "</head><body><video>"
                + "<source src=\"https://cdn.example.com/480/index.m3u8\" type=\"application/x-mpegURL\" label=\"480p\"/>"
                + "<source src=\"https://cdn.example.com/1080/index.m3u8\" type=\"application/vnd.apple.mpegurl\" label=\"1080p\"/>"
                + "<source src=\"https://cdn.example.com/fallback-360.mp4\" type=\"video/mp4\" label=\"360p\"/>"
                + "</video><script>{\"ad\":\"https://ads.example.com/noise.m3u8\"}</script></body></html>";

        XhsParseResult result = HlsMediaCandidateParser.parse(
                html, "https://example.com/watch/1", "manual", "desktop");

        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.getMediaCount());
        Assert.assertEquals(1080, result.getMediaItems().get(0).getQualityHeight());
        Assert.assertTrue(result.getMediaItems().get(0).isSelected());
        Assert.assertFalse(result.getMediaItems().get(1).isSelected());
        Assert.assertEquals(XhsMediaTransport.HLS_STREAM, result.getMediaItems().get(0).getTransport());
        Assert.assertEquals(XhsMediaTransport.DIRECT_FILE, result.getMediaItems().get(2).getTransport());
        Assert.assertEquals(360, result.getMediaItems().get(2).getQualityHeight());
    }

    @Test
    public void parseIgnoresM3u8FoundOnlyInArbitraryJson() {
        String html = "<html><body><script type=\"application/json\">"
                + "{\"preload\":\"https://cdn.example.com/noise.m3u8\"}</script></body></html>";

        Assert.assertNull(HlsMediaCandidateParser.parse(
                html, "https://example.com/watch/1", "manual", "desktop"));
    }

    @Test
    public void parseRejectsM3u8MentionFoundOnlyInVideoQuery() {
        String html = "<html><body><video>"
                + "<source src=\"https://cdn.example.com/api?next=master.m3u8\" type=\"video/mp4\"/>"
                + "</video></body></html>";

        Assert.assertNull(HlsMediaCandidateParser.parse(
                html, "https://example.com/watch/1", "manual", "desktop"));
    }
}
