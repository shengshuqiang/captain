package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.junit.Assert;
import org.junit.Test;

public class TaobaoShareParserTest {
    private static final String PAGE_URL = "https://item.taobao.com/item.htm?id=940585894119";

    @Test
    public void extractJsRedirectUrlReadsTaobaoShortLinkTrampoline() {
        String html = "<script>var url = 'https://item.taobao.com/item.htm?id=940585894119"
                + "&amp;short_name=h.RYVkqoGjYnFDBco';</script>";

        String redirectUrl = TaobaoShareParser.extractJsRedirectUrl(html);

        Assert.assertEquals(
                "https://item.taobao.com/item.htm?id=940585894119&short_name=h.RYVkqoGjYnFDBco",
                redirectUrl
        );
    }

    @Test
    public void parseDetailResponseExtractsGalleryVideoFromApiStackValue() {
        String apiStackValue = "{"
                + "\"global\":{\"data\":{"
                + "\"item\":{\"itemId\":\"940585894119\",\"title\":\"7天无理由退货\"},"
                + "\"seller\":{\"shopName\":\"淘宝店铺\"},"
                + "\"gallery\":{\"videos\":[{"
                + "\"url\":\"https://cloud.video.taobao.com/play/u/1/p/1/e/6/t/1/940585894119.mp4\","
                + "\"videoThumbnailURL\":\"//img.alicdn.com/imgextra/i1/video-cover.jpg\""
                + "}]}"
                + "}}"
                + "}";
        String responseJson = "{"
                + "\"data\":{"
                + "\"apiStack\":[{\"value\":" + quote(apiStackValue) + "}],"
                + "\"item\":{\"title\":\"fallback\"}"
                + "}"
                + "}";

        XhsParseResult parseResult = TaobaoShareParser.parseDetailResponse(
                responseJson,
                PAGE_URL,
                "manual_input",
                "taobao_mtop_detail"
        );

        Assert.assertNotNull(parseResult);
        Assert.assertEquals("940585894119", parseResult.getNoteId());
        Assert.assertEquals("7天无理由退货", parseResult.getTitle());
        Assert.assertEquals("淘宝店铺", parseResult.getAuthorName());
        Assert.assertEquals(XhsMediaType.VIDEO, parseResult.getPrimaryMediaType());
        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals("https://cloud.video.taobao.com/play/u/1/p/1/e/6/t/1/940585894119.mp4",
                parseResult.getMediaItems().get(0).getMediaUrl());
        Assert.assertEquals("https://img.alicdn.com/imgextra/i1/video-cover.jpg",
                parseResult.getMediaItems().get(0).getCoverUrl());
    }

    @Test
    public void parseDetailResponseExtractsNestedDescVideoUrl() {
        String responseJson = "{"
                + "\"data\":{"
                + "\"item\":{\"itemId\":\"940585894119\",\"title\":\"淘宝商品\"},"
                + "\"seller\":{\"sellerNick\":\"卖家\"},"
                + "\"desc\":{\"model\":{\"videoUrl\":\"https://video.alicdn.com/video/940585894119.mov\","
                + "\"thumbnail\":\"https://img.alicdn.com/cover.jpg\"}}"
                + "}"
                + "}";

        XhsParseResult parseResult = TaobaoShareParser.parseDetailResponse(
                responseJson,
                PAGE_URL,
                "manual_input",
                "taobao_mtop_detail"
        );

        Assert.assertNotNull(parseResult);
        Assert.assertEquals("https://video.alicdn.com/video/940585894119.mov",
                parseResult.getMediaItems().get(0).getMediaUrl());
        Assert.assertEquals("mov", parseResult.getMediaItems().get(0).getFileExtension());
    }

    @Test
    public void parseDetailResponseIgnoresHlsOnlyPayload() {
        String responseJson = "{"
                + "\"data\":{"
                + "\"item\":{\"itemId\":\"940585894119\",\"title\":\"淘宝商品\"},"
                + "\"gallery\":{\"videos\":[{\"videoUrl\":\"https://video.alicdn.com/live/index.m3u8\"}]}"
                + "}"
                + "}";

        XhsParseResult parseResult = TaobaoShareParser.parseDetailResponse(
                responseJson,
                PAGE_URL,
                "manual_input",
                "taobao_mtop_detail"
        );

        Assert.assertNull(parseResult);
    }

    @Test
    public void sniffedVideoResultAcceptsTaobaoVideoPlayUrlWithoutExtension() {
        String mediaUrl = "https://cloud.video.taobao.com/play/u/1/p/1/e/6/t/1/940585894119";

        XhsParseResult parseResult = TaobaoShareParser.buildSniffedVideoResult(
                mediaUrl,
                PAGE_URL,
                "manual_input",
                "webview_request"
        );

        Assert.assertNotNull(parseResult);
        Assert.assertEquals("940585894119", parseResult.getNoteId());
        Assert.assertEquals(XhsMediaType.VIDEO, parseResult.getPrimaryMediaType());
        Assert.assertEquals(mediaUrl, parseResult.getMediaItems().get(0).getMediaUrl());
        Assert.assertEquals("mp4", parseResult.getMediaItems().get(0).getFileExtension());
    }

    @Test
    public void sniffedVideoResultRejectsHlsAndImageFallback() {
        Assert.assertNull(TaobaoShareParser.buildSniffedVideoResult(
                "https://cloud.video.taobao.com/live/index.m3u8",
                PAGE_URL,
                "manual_input",
                "webview_request"
        ));
        Assert.assertNull(TaobaoShareParser.buildSniffedVideoResult(
                "https://img.alicdn.com/imgextra/i1/cover.jpg",
                PAGE_URL,
                "manual_input",
                "webview_request"
        ));
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
