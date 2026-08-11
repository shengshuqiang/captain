package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class XhsParseRepositoryTest {

    @Test
    public void selectPreferredGenericResultFallsBackToMobileWhenItAddsVideo() {
        XhsParseResult desktopResult = buildParseResult(
                new XhsMediaItem("desktop_cover", XhsMediaType.IMAGE,
                        "https://cdn.example.com/cover.jpg",
                        "https://cdn.example.com/cover.jpg", 0, 0, 0, "jpg", true)
        );
        XhsParseResult mobileResult = buildParseResult(
                new XhsMediaItem("mobile_video", XhsMediaType.VIDEO,
                        "https://cdn.example.com/video.mp4",
                        "https://cdn.example.com/cover.jpg", 0, 0, 0, "mp4", true),
                new XhsMediaItem("mobile_cover", XhsMediaType.IMAGE,
                        "https://cdn.example.com/cover.jpg",
                        "https://cdn.example.com/cover.jpg", 0, 0, 0, "jpg", true)
        );

        XhsParseResult preferredResult = XhsParseRepository.selectPreferredGenericResult(desktopResult, mobileResult);

        Assert.assertSame(mobileResult, preferredResult);
    }

    @Test
    public void selectPreferredGenericResultKeepsDesktopWhenFallbackDoesNotAddRichMedia() {
        XhsParseResult desktopResult = buildParseResult(
                new XhsMediaItem("desktop_cover", XhsMediaType.IMAGE,
                        "https://cdn.example.com/cover.jpg",
                        "https://cdn.example.com/cover.jpg", 0, 0, 0, "jpg", true)
        );
        XhsParseResult mobileResult = buildParseResult(
                new XhsMediaItem("mobile_cover", XhsMediaType.IMAGE,
                        "https://cdn.example.com/cover.jpg",
                        "https://cdn.example.com/cover.jpg", 0, 0, 0, "jpg", true),
                new XhsMediaItem("mobile_extra", XhsMediaType.IMAGE,
                        "https://cdn.example.com/other.jpg",
                        "https://cdn.example.com/other.jpg", 0, 0, 0, "jpg", true)
        );

        XhsParseResult preferredResult = XhsParseRepository.selectPreferredGenericResult(desktopResult, mobileResult);

        Assert.assertSame(desktopResult, preferredResult);
    }

    @Test
    public void shouldTryGenericMobileFallbackSkipsOrdinaryImagePages() {
        XhsParseResult desktopResult = buildParseResult(
                "https://example.com/post/1",
                new XhsMediaItem("desktop_cover", XhsMediaType.IMAGE,
                        "https://cdn.example.com/cover.jpg",
                        "https://cdn.example.com/cover.jpg", 0, 0, 0, "jpg", true)
        );

        boolean shouldFallback = XhsParseRepository.shouldTryGenericMobileFallback(
                "https://example.com/post/1",
                desktopResult
        );

        Assert.assertFalse(shouldFallback);
    }

    @Test
    public void shouldTryGenericMobileFallbackKeepsBilibiliPagesEnabled() {
        XhsParseResult desktopResult = buildParseResult(
                "https://www.bilibili.com/video/BV1q4dfBNELT/",
                new XhsMediaItem("desktop_cover", XhsMediaType.IMAGE,
                        "https://i1.hdslb.com/bfs/archive/cover.jpg",
                        "https://i1.hdslb.com/bfs/archive/cover.jpg", 0, 0, 0, "jpg", true)
        );

        boolean shouldFallback = XhsParseRepository.shouldTryGenericMobileFallback(
                "https://m.bilibili.com/video/BV1q4dfBNELT",
                desktopResult
        );

        Assert.assertTrue(shouldFallback);
    }

    @Test
    public void shouldTryGenericMobileFallbackForDouyinShortLinkWhenDesktopOnlyHasCover() {
        XhsParseResult desktopResult = buildParseResult(
                "https://www.douyin.com/video/7657490923185873481",
                new XhsMediaItem("desktop_cover", XhsMediaType.IMAGE,
                        "https://p3-pc-sign.douyinpic.com/cover.jpeg",
                        "https://p3-pc-sign.douyinpic.com/cover.jpeg", 0, 0, 0, "jpg", true)
        );

        boolean shouldFallback = XhsParseRepository.shouldTryGenericMobileFallback(
                "https://v.douyin.com/edv2J8qaQEM/",
                desktopResult
        );

        Assert.assertTrue(shouldFallback);
    }

    @Test
    public void shouldTryGenericMobileFallbackForDouyinCanonicalUrl() {
        XhsParseResult desktopResult = buildParseResult(
                "https://www.douyin.com/video/7657490923185873481",
                new XhsMediaItem("desktop_cover", XhsMediaType.IMAGE,
                        "https://p3-pc-sign.douyinpic.com/cover.jpeg",
                        "https://p3-pc-sign.douyinpic.com/cover.jpeg", 0, 0, 0, "jpg", true)
        );

        boolean shouldFallback = XhsParseRepository.shouldTryGenericMobileFallback(
                "https://example.com/redirect",
                desktopResult
        );

        Assert.assertTrue(shouldFallback);
    }

    @Test
    public void requiresRuntimeMediaInspectionForImageOnlyStaticResult() {
        XhsParseResult imageOnlyResult = buildParseResult(
                new XhsMediaItem("site_icon", XhsMediaType.IMAGE,
                        "https://example.com/apple-touch-icon.png",
                        "https://example.com/apple-touch-icon.png", 0, 0, 0, "png", true)
        );

        Assert.assertTrue(XhsParseRepository.requiresRuntimeMediaInspection(imageOnlyResult));
    }

    @Test
    public void doesNotRequireRuntimeMediaInspectionWhenStaticResultHasVideo() {
        XhsParseResult videoResult = buildParseResult(
                new XhsMediaItem("video", XhsMediaType.VIDEO,
                        "https://cdn.example.com/video.mp4",
                        "https://cdn.example.com/cover.jpg", 0, 0, 0, "mp4", true)
        );

        Assert.assertFalse(XhsParseRepository.requiresRuntimeMediaInspection(videoResult));
    }

    private static XhsParseResult buildParseResult(XhsMediaItem... mediaItems) {
        return buildParseResult("https://example.com/post/1", mediaItems);
    }

    private static XhsParseResult buildParseResult(String canonicalUrl, XhsMediaItem... mediaItems) {
        ArrayList<XhsMediaItem> items = new ArrayList<>();
        for (XhsMediaItem mediaItem : mediaItems) {
            items.add(mediaItem);
        }
        return new XhsParseResult(
                "note",
                canonicalUrl,
                canonicalUrl,
                "example",
                "example",
                items.get(0).getCoverUrl(),
                "test",
                "manual_input",
                items
        );
    }
}
