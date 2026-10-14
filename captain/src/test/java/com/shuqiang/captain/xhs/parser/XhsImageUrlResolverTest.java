package com.shuqiang.captain.xhs.parser;

import org.junit.Assert;
import org.junit.Test;

/** 原图取源只允许已识别的小红书图片地址，未知格式不得猜测截断。 */
public class XhsImageUrlResolverTest {
    private static final String KEY = "notes_pre_post/1040g3k031e80n8h60e2g5ndidcf09ahrjdnd900";
    private static final String ORIGINAL = "https://ci.xiaohongshu.com/" + KEY + "?imageView2/format/jpeg";

    @Test
    public void resolvesWebTemplateToOriginalAndIsIdempotent() {
        String web = "https://sns-webpic-qc.xhscdn.com/202610132000/0123456789abcdef/"
                + KEY + "!nd_dft_wlteh_webp_3";
        Assert.assertEquals(ORIGINAL, XhsImageUrlResolver.resolve(web));
        Assert.assertEquals(ORIGINAL, XhsImageUrlResolver.resolve(ORIGINAL));
        Assert.assertEquals(ORIGINAL, XhsImageUrlResolver.resolve(web.replace("https:", "")));
    }

    @Test
    public void stripsOriginalCdnWatermarkProcessingWhileKeepingFullImageKey() {
        Assert.assertEquals(ORIGINAL, XhsImageUrlResolver.resolve("https://sns-img-bd.xhscdn.com/"
                + KEY + "?imageView2/2/w/1080/watermark/2/text/test"));
    }

    @Test
    public void preservesUnrecognizedUrlsVideoAndSpoofedHosts() {
        String[] urls = {"https://sns-webpic-qc.xhscdn.com/photo.jpg?token=keep",
                "https://sns-video-bd.xhscdn.com/stream/video.mp4",
                "https://sns-webpic-qc.xhscdn.com.attacker.example/202610132000/hash/" + KEY + "!template",
                "https://fixture.example/image.jpg?watermark=keep", "not a url"};
        for (String url : urls) Assert.assertEquals(url, XhsImageUrlResolver.resolve(url));
        Assert.assertNull(XhsImageUrlResolver.resolve(null));
    }
}
