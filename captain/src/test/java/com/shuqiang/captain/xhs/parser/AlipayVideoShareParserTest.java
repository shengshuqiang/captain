package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.junit.Assert;
import org.junit.Test;

import java.net.URLEncoder;

public class AlipayVideoShareParserTest {
    private static final String CONTENT_ID = "20250212OB020010034874259494";
    private static final String VIDEO_ID = "E7oCTY4GD90AAAAAAAAAAAAAfa7_AQBr";
    private static final String CLARITY = "1080P_h265_EH";
    private static final String EXPECTED_VIDEO_URL = "https://gw.alipayobjects.com/v/open_content/afts/video/"
            + VIDEO_ID + "/" + CLARITY;

    @Test
    public void parseShareInfoOnlyBuildsDirectVideoUrlFromNestedScheme() throws Exception {
        String pageUrl = buildShareUrl(CONTENT_ID, VIDEO_ID, CLARITY);

        AlipayVideoShareParser.ShareInfo shareInfo = AlipayVideoShareParser.extractShareInfo(pageUrl);
        XhsParseResult parseResult = AlipayVideoShareParser.parseShareInfoOnly(
                pageUrl,
                "manual_input",
                "alipay_scheme",
                shareInfo
        );

        Assert.assertNotNull(shareInfo);
        Assert.assertNotNull(parseResult);
        Assert.assertEquals(CONTENT_ID, parseResult.getNoteId());
        Assert.assertEquals(XhsMediaType.VIDEO, parseResult.getPrimaryMediaType());
        Assert.assertEquals(1, parseResult.getMediaCount());
        Assert.assertEquals(EXPECTED_VIDEO_URL, parseResult.getMediaItems().get(0).getMediaUrl());
    }

    @Test
    public void parseDetailResponseUsesApiMetadataAndKeepsSharedClarityVideoUrl() throws Exception {
        String pageUrl = buildShareUrl(CONTENT_ID, VIDEO_ID, CLARITY);
        AlipayVideoShareParser.ShareInfo shareInfo = AlipayVideoShareParser.extractShareInfo(pageUrl);
        String responseJson = "{"
                + "\"success\":true,"
                + "\"resultObj\":{\"data\":[{"
                + "\"title\":\"骑行视频\","
                + "\"content\":\"\","
                + "\"author\":{\"nickName\":\"End筒子\"},"
                + "\"ext\":{\"spmExt\":{\"_item_id\":\"" + CONTENT_ID + "\"}},"
                + "\"video\":{"
                + "\"vid\":\"http://gw.alipayobjects.com/v/open_content/afts/video/" + VIDEO_ID + "/720P_h265\","
                + "\"djangoId\":\"" + VIDEO_ID + "\","
                + "\"duration\":10.233332633972168,"
                + "\"widthRatio\":2160,"
                + "\"heightRatio\":3840,"
                + "\"firstFramePic\":{\"url\":\"https://mdn.alipayobjects.com/open_content/afts/img/cover/original\"}"
                + "}"
                + "}]}"
                + "}";

        XhsParseResult parseResult = AlipayVideoShareParser.parseDetailResponse(
                responseJson,
                pageUrl,
                "manual_input",
                "alipay_webgw",
                shareInfo
        );

        Assert.assertNotNull(parseResult);
        Assert.assertEquals(CONTENT_ID, parseResult.getNoteId());
        Assert.assertEquals("骑行视频", parseResult.getTitle());
        Assert.assertEquals("End筒子", parseResult.getAuthorName());
        Assert.assertEquals("https://mdn.alipayobjects.com/open_content/afts/img/cover/original",
                parseResult.getCoverUrl());
        Assert.assertEquals(EXPECTED_VIDEO_URL, parseResult.getMediaItems().get(0).getMediaUrl());
        Assert.assertEquals(10, parseResult.getMediaItems().get(0).getDurationSec());
        Assert.assertEquals(2160, parseResult.getMediaItems().get(0).getWidth());
        Assert.assertEquals(3840, parseResult.getMediaItems().get(0).getHeight());
    }

    private static String buildShareUrl(String contentId, String vid, String clarity) throws Exception {
        String videoInfo = "{\"clarity\":\"" + clarity + "\",\"vid\":\"" + vid + "\"}";
        String nestedUrl = "/www/detail.html?contentId=" + contentId + "&refer=share";
        String scheme = "alipays://platformapi/startapp?appId=68687748"
                + "&url=" + encode(nestedUrl)
                + "&contentId=" + contentId
                + "&videoInfo=" + encode(videoInfo);
        return "https://render.alipay.com/p/yuyan/180020010001266490/video-share.html?scheme="
                + encode(scheme);
    }

    private static String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }
}
