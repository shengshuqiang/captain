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

    @Test
    public void parseDoesNotTreatHlsPlaylistAsDownloadableVideo() throws Exception {
        String html = "<html><head>"
                + "<title>HLS 分享页</title>"
                + "<meta property=\"og:video\" content=\"https://cdn.example.com/video/demo.m3u8\"/>"
                + "<meta property=\"og:image\" content=\"https://cdn.example.com/video/demo-cover.jpg\"/>"
                + "</head><body></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/post/hls",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals(XhsMediaType.IMAGE, parseResult.getPrimaryMediaType());
        Assert.assertEquals("https://cdn.example.com/video/demo-cover.jpg",
                parseResult.getMediaItems().get(0).getMediaUrl());
    }

    @Test
    public void parseExtractsBilibiliMobileVideoAndCoverFromInitialState() throws Exception {
        String html = "<html><head>"
                + "<title>叫姐姐_哔哩哔哩_bilibili</title>"
                + "<link rel=\"canonical\" href=\"https://www.bilibili.com/video/BV1q4dfBNELT/\"/>"
                + "<meta property=\"og:image\" content=\"https://i1.hdslb.com/bfs/archive/cover.jpg\"/>"
                + "<meta property=\"og:site_name\" content=\"哔哩哔哩\"/>"
                + "</head><body><script>"
                + "window.__INITIAL_STATE__={\"video\":{\"viewInfo\":{\"bvid\":\"BV1q4dfBNELT\",\"pic\":\"http://i1.hdslb.com/bfs/archive/cover.jpg\"},"
                + "\"upInfo\":{\"name\":\"测试UP主\"},"
                + "\"playUrlInfo\":[{\"url\":\"https://upos-sz.bilivideo.com/upgcxcode/demo-16.mp4?foo=1\"}],"
                + "\"related\":{\"result\":[{\"pic\":\"http://i1.hdslb.com/bfs/archive/other.jpg\"}]}}};"
                + "(function(){})();"
                + "</script></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://m.bilibili.com/video/BV1q4dfBNELT",
                "manual_input",
                "mobile"
        );

        Assert.assertEquals(2, parseResult.getMediaCount());
        Assert.assertEquals(XhsMediaType.VIDEO, parseResult.getPrimaryMediaType());
        Assert.assertEquals("https://upos-sz.bilivideo.com/upgcxcode/demo-16.mp4?foo=1",
                parseResult.getMediaItems().get(0).getMediaUrl());
        Assert.assertEquals("https://i1.hdslb.com/bfs/archive/cover.jpg",
                parseResult.getMediaItems().get(1).getMediaUrl());
        Assert.assertEquals("测试UP主", parseResult.getAuthorName());
    }

    @Test
    public void parseExtractsExtensionlessVideoFromStructuredState() throws Exception {
        String html = "<html><head>"
                + "<title>结构化视频页</title>"
                + "<link rel=\"canonical\" href=\"https://www.example.com/video/123\"/>"
                + "</head><body>"
                + "<img src=\"https://static.example.com/logo.png\"/>"
                + "<script>window._ROUTER_DATA = {\"loaderData\":{\"video_(id)/page\":{\"videoInfoRes\":{\"item_list\":["
                + "{\"video\":{\"play_addr\":{\"uri\":\"video-id\",\"url_list\":["
                + "\"https:\\u002F\\u002Fmedia.example.com\\u002Fplay\\u002F?id=123\"]},"
                + "\"cover\":{\"url_list\":[\"https:\\u002F\\u002Fmedia.example.com\\u002Fcover.webp\"]}}}"
                + "]}}}};</script></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://short.example.com/abc",
                "manual_input",
                "mobile"
        );

        Assert.assertEquals(2, parseResult.getMediaCount());
        Assert.assertEquals(XhsMediaType.VIDEO, parseResult.getPrimaryMediaType());
        Assert.assertEquals("https://media.example.com/play/?id=123",
                parseResult.getMediaItems().get(0).getMediaUrl());
        Assert.assertEquals("mp4", parseResult.getMediaItems().get(0).getFileExtension());
        Assert.assertEquals("https://media.example.com/cover.webp",
                parseResult.getMediaItems().get(1).getMediaUrl());
        Assert.assertTrue(parseResult.getParseStrategy().contains("结构化视频状态"));
    }

    @Test
    public void parseExtractsJsonLdContentUrlWithoutFileExtension() throws Exception {
        String html = "<html><head><title>JSON-LD 视频页</title></head><body>"
                + "<script type=\"application/ld+json\">"
                + "{\"@type\":\"VideoObject\",\"contentUrl\":\"https://media.example.com/watch?id=9\","
                + "\"thumbnailUrl\":\"https://media.example.com/thumb.jpg\"}"
                + "</script></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/watch/9",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals(XhsMediaType.VIDEO, parseResult.getPrimaryMediaType());
        Assert.assertEquals("https://media.example.com/watch?id=9",
                parseResult.getMediaItems().get(0).getMediaUrl());
        Assert.assertEquals("https://media.example.com/thumb.jpg",
                parseResult.getMediaItems().get(1).getMediaUrl());
    }

    @Test
    public void parseDoesNotPromoteHlsOrImageStateToDownloadableVideo() throws Exception {
        String html = "<html><head>"
                + "<meta property=\"og:image\" content=\"https://media.example.com/cover.jpg\"/>"
                + "</head><body><script>window.__NEXT_DATA__={\"props\":{\"video\":{\"play_url\":{\"url_list\":["
                + "\"javascript:alert(1)\",\"https:\\u002F\\u002Fmedia.example.com\\u002Fstream.m3u8\","
                + "\"https:\\u002F\\u002Fmedia.example.com\\u002Fposter.webp\"]}}}};</script></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/video/1",
                "manual_input",
                "mobile"
        );

        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals(XhsMediaType.IMAGE, parseResult.getPrimaryMediaType());
        Assert.assertEquals("https://media.example.com/cover.jpg",
                parseResult.getMediaItems().get(0).getMediaUrl());
    }

    @Test
    public void parseDoesNotPromoteImageObjectContentUrlToVideo() throws Exception {
        String html = "<html><head>"
                + "<meta property=\"og:image\" content=\"https://media.example.com/cover.jpg\"/>"
                + "</head><body><script type=\"application/ld+json\">"
                + "{\"@type\":\"ImageObject\",\"contentUrl\":\"https://media.example.com/original?id=1\"}"
                + "</script></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/image/1",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals(XhsMediaType.IMAGE, parseResult.getPrimaryMediaType());
    }

    @Test
    public void parseDoesNotPromoteImageDownloadUrlToVideo() throws Exception {
        String html = "<html><head>"
                + "<meta property=\"og:image\" content=\"https://media.example.com/cover.jpg\"/>"
                + "</head><body><script type=\"application/json\">"
                + "{\"image\":{\"downloadUrl\":\"https://media.example.com/original?id=2\"}}"
                + "</script></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/image/2",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals(XhsMediaType.IMAGE, parseResult.getPrimaryMediaType());
    }

    @Test
    public void parseIgnoresStateMarkerInsideScriptString() throws Exception {
        String html = "<html><head>"
                + "<meta property=\"og:image\" content=\"https://media.example.com/cover.jpg\"/>"
                + "</head><body><script>"
                + "const example = 'window._ROUTER_DATA = {\"video\":{\"play_addr\":{\"url_list\":[\""
                + "https://media.example.com/not-real\"]}}}';"
                + "</script></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/article/1",
                "manual_input",
                "desktop"
        );

        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals(XhsMediaType.IMAGE, parseResult.getPrimaryMediaType());
    }

    @Test
    public void parseSkipsMusicPlayAddressBeforeMainVideo() throws Exception {
        String html = "<html><head><title>视频作品</title></head><body><script>"
                + "window._ROUTER_DATA={\"item\":{\"music\":{\"play_addr\":{\"url_list\":["
                + "\"https://media.example.com/audio?id=1\"]}},\"video\":{\"play_addr\":{\"url_list\":["
                + "\"https://media.example.com/video?id=1\"]},\"cover\":{\"url_list\":["
                + "\"https://media.example.com/cover.webp\"]}}}};"
                + "</script></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/video/1",
                "manual_input",
                "mobile"
        );

        Assert.assertEquals(XhsMediaType.VIDEO, parseResult.getPrimaryMediaType());
        Assert.assertEquals("https://media.example.com/video?id=1",
                parseResult.getMediaItems().get(0).getMediaUrl());
    }

    @Test
    public void parseDoesNotPromotePlayUrlInsideVideoCover() throws Exception {
        String html = "<html><head>"
                + "<meta property=\"og:image\" content=\"https://media.example.com/cover.jpg\"/>"
                + "</head><body><script>window._ROUTER_DATA={\"video\":{\"cover\":{\"playUrl\":"
                + "\"https://media.example.com/cover?id=1\"}}};</script></body></html>";

        XhsParseResult parseResult = GenericWebSniffParser.parse(
                html,
                "https://example.com/article/1",
                "manual_input",
                "mobile"
        );

        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals(XhsMediaType.IMAGE, parseResult.getPrimaryMediaType());
    }
}
