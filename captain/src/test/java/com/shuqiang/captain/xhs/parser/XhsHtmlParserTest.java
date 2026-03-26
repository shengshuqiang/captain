package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.junit.Assert;
import org.junit.Test;

public class XhsHtmlParserTest {

    @Test
    public void parseVideoPageUsesOgVideo() throws Exception {
        String html = "<html><head>"
                + "<meta name=\"og:url\" content=\"https://www.xiaohongshu.com/explore/6832d99d0000000021003156\"/>"
                + "<meta name=\"og:title\" content=\"sunshine☀️ - 小红书\"/>"
                + "<meta name=\"og:image\" content=\"https://sns-webpic-qc.xhscdn.com/cover.jpg\"/>"
                + "<meta name=\"og:videotime\" content=\"00:06\"/>"
                + "<meta name=\"og:video\" content=\"https://sns-video-bd.xhscdn.com/stream/video.mp4\"/>"
                + "<script>window.__INITIAL_STATE__={\"note\":{\"noteId\":\"6832d99d0000000021003156\",\"title\":\"一直很喜欢的skims look\",\"user\":{\"nickname\":\"sunshine☀️\"}}};</script>"
                + "</head><body></body></html>";

        XhsParseResult parseResult = XhsHtmlParser.parse(
                html,
                "https://www.xiaohongshu.com/explore/6832d99d0000000021003156",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals("6832d99d0000000021003156", parseResult.getNoteId());
        Assert.assertEquals(XhsMediaType.VIDEO, parseResult.getPrimaryMediaType());
        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals(6, parseResult.getMediaItems().get(0).getDurationSec());
    }

    @Test
    public void parseVideoPagePrefersVideoOverImageList() throws Exception {
        String html = "<html><head>"
                + "<meta name=\"og:url\" content=\"https://www.xiaohongshu.com/explore/6832d99d0000000021003156\"/>"
                + "<meta name=\"og:title\" content=\"sunshine☀️ - 小红书\"/>"
                + "<meta name=\"og:image\" content=\"https://sns-webpic-qc.xhscdn.com/cover.jpg\"/>"
                + "<meta name=\"og:videotime\" content=\"00:06\"/>"
                + "<meta name=\"og:video\" content=\"https://sns-video-bd.xhscdn.com/stream/video.mp4\"/>"
                + "<script>window.__INITIAL_STATE__={\"note\":{\"noteId\":\"6832d99d0000000021003156\",\"title\":\"一直很喜欢的skims look\",\"user\":{\"nickname\":\"sunshine☀️\"},\"imageList\":[{\"infoList\":[{\"url\":\"https://sns-webpic-qc.xhscdn.com/video-cover.jpg\"}]}],\"video\":{\"consumer\":{\"originVideoKey\":\"https://sns-video-bd.xhscdn.com/stream/video.mp4\"},\"duration\":6}}};</script>"
                + "</head><body></body></html>";

        XhsParseResult parseResult = XhsHtmlParser.parse(
                html,
                "https://www.xiaohongshu.com/explore/6832d99d0000000021003156",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals(XhsMediaType.VIDEO, parseResult.getPrimaryMediaType());
        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals("https://sns-video-bd.xhscdn.com/stream/video.mp4",
                parseResult.getMediaItems().get(0).getMediaUrl());
    }

    @Test
    public void parseImagePageUsesInitialStateImageList() throws Exception {
        String html = "<html><head>"
                + "<meta name=\"og:url\" content=\"https://www.xiaohongshu.com/explore/66aabbccdd00112233445566\"/>"
                + "<meta name=\"og:title\" content=\"旅行博主 - 小红书\"/>"
                + "<meta name=\"og:image\" content=\"https://sns-webpic-qc.xhscdn.com/cover2.jpg\"/>"
                + "<script>window.__INITIAL_STATE__={\"noteCard\":{\"noteId\":\"66aabbccdd00112233445566\",\"title\":\"海边散步的一天\",\"user\":{\"nickname\":\"旅行博主\"},\"imageList\":[{\"width\":1080,\"height\":1440,\"infoList\":[{\"url\":\"https://sns-webpic-qc.xhscdn.com/photo1.jpg\"}]},{\"width\":1080,\"height\":1440,\"infoList\":[{\"url\":\"https://sns-webpic-qc.xhscdn.com/photo2.jpg\"}]}]}};</script>"
                + "</head><body></body></html>";

        XhsParseResult parseResult = XhsHtmlParser.parse(
                html,
                "https://www.xiaohongshu.com/explore/66aabbccdd00112233445566",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals("66aabbccdd00112233445566", parseResult.getNoteId());
        Assert.assertEquals(XhsMediaType.IMAGE, parseResult.getPrimaryMediaType());
        Assert.assertEquals(2, parseResult.getMediaCount());
        Assert.assertEquals("海边散步的一天", parseResult.getTitle());
    }
}
