package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.junit.Assert;
import org.junit.Test;

public class XhsHtmlParserTest {

    @Test
    public void imageFileIdOverridesWatermarkedPreviewAndReachesDownloadRequest() throws Exception {
        String key = "spectrum/1040g34o3268u28vbk46g5n6t5v11c68hj48vjhg";
        String expected = "https://ci.xiaohongshu.com/" + key + "?imageView2/format/jpeg";
        String page = "https://www.xiaohongshu.com/discovery/item/example";
        String html = "<script>window.__INITIAL_STATE__={\"note\":{\"noteId\":\"example\","
                + "\"imageList\":[{\"fileId\":\"" + key + "\",\"width\":3001,\"height\":3001,"
                + "\"urlDefault\":\"https://sns-webpic-qc.xhscdn.com/preview.jpg\"}]}};</script>";
        XhsParseResult result = XhsHtmlParser.parse(html, page, "test", "desktop");
        com.shuqiang.captain.xhs.model.XhsMediaItem image = result.getMediaItems().get(0);
        Assert.assertEquals(expected, image.getMediaUrl());
        Assert.assertEquals(expected, image.getCoverUrl());
        Assert.assertEquals(3001, image.getWidth());
        Assert.assertEquals("jpg", image.getFileExtension());
        com.shuqiang.captain.xhs.download.XhsDownloadItem download =
                com.shuqiang.captain.xhs.download.XhsDownloadRequest.from(result).getItems().get(0);
        Assert.assertEquals(expected, download.getDirectUrl());
        Assert.assertEquals(page, download.getSourcePageUrl());
    }

    @Test
    public void metaOnlyImageAlsoUsesOriginalTemplateSource() throws Exception {
        String key = "spectrum/1040g34o3268u28vbk46g5n6t5v11c68hj48vjhg";
        String url = "https://sns-webpic-qc.xhscdn.com/202610132000/hash/" + key + "!h5_1080jpg";
        XhsParseResult result = XhsHtmlParser.parse("<meta property=\"og:image\" content=\"" + url + "\">",
                "https://www.xiaohongshu.com/discovery/item/example", "test", "desktop");
        Assert.assertEquals("https://ci.xiaohongshu.com/" + key + "?imageView2/format/jpeg",
                result.getMediaItems().get(0).getMediaUrl());
        Assert.assertEquals("jpg", result.getMediaItems().get(0).getFileExtension());
    }

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
