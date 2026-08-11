package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.junit.Assert;
import org.junit.Test;

public class WebResourceMediaCollectorTest {

    @Test
    public void buildResultKeepsProcessImagesButOnlySelectsFinalContentImage() {
        WebResourceMediaCollector collector = new WebResourceMediaCollector(
                "https://example.com/dynamic/list",
                "manual_input"
        );
        collector.observeRequest("https://static.example.com/app-icon.png");
        collector.observeRequest("https://cdn.example.com/photo-blur.jpg");
        collector.observeDom(
                "https://cdn.example.com/intermediate.jpg",
                "image",
                1080,
                1440,
                1080,
                1440,
                true,
                true,
                false,
                "loading cover"
        );
        collector.observeDom(
                "https://cdn.example.com/photo.jpg",
                "image",
                1080,
                1440,
                1080,
                1440,
                true,
                true,
                true,
                "post-cover"
        );

        XhsParseResult result = collector.buildResult();

        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.getMediaCount());
        Assert.assertEquals(1, result.getSelectedCount());
        Assert.assertEquals("https://cdn.example.com/photo.jpg", result.getSelectedItems().get(0).getMediaUrl());
    }

    @Test
    public void buildResultDoesNotSelectVisibleAvatarOrSmallIcon() {
        WebResourceMediaCollector collector = new WebResourceMediaCollector(
                "https://example.com/post/1",
                "manual_input"
        );
        collector.observeDom(
                "https://cdn.example.com/user/avatar.jpg",
                "image",
                640,
                640,
                640,
                640,
                true,
                true,
                true,
                "profile avatar"
        );
        collector.observeDom(
                "https://cdn.example.com/action.png",
                "image",
                96,
                96,
                512,
                512,
                true,
                true,
                true,
                "toolbar"
        );

        XhsParseResult result = collector.buildResult();

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.getMediaCount());
        Assert.assertEquals(0, result.getSelectedCount());
    }

    @Test
    public void buildResultDoesNotSelectFinalImageBeforePreviewIsReady() {
        WebResourceMediaCollector collector = new WebResourceMediaCollector(
                "https://jkforum.net/p/forum-234-4.html?orderby=dateline",
                "manual_input"
        );
        collector.observeDom(
                "https://cdn1.mymyatt.net/content/forum-cover",
                "image",
                332,
                499,
                516,
                688,
                true,
                false,
                true,
                "article cover"
        );

        XhsParseResult result = collector.buildResult();

        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getSelectedCount());
        Assert.assertEquals("jpg", result.getMediaItems().get(0).getFileExtension());
    }

    @Test
    public void buildResultSelectsPreviewableLargeCoverButNotWideBanner() {
        WebResourceMediaCollector collector = new WebResourceMediaCollector(
                "https://jkforum.net/p/forum-234-4.html?orderby=dateline",
                "manual_input"
        );
        collector.observeDom(
                "https://jkforum.net/attachment/313734700125183?t=516f&c=3:4",
                "image",
                168,
                253,
                516,
                688,
                true,
                true,
                true,
                "article cover 封面"
        );
        collector.observeDom(
                "https://jkforum.net/attachment/8700001700000149",
                "image",
                1020,
                186,
                2040,
                372,
                true,
                true,
                true,
                "header"
        );

        XhsParseResult result = collector.buildResult();

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.getMediaCount());
        Assert.assertEquals(1, result.getSelectedCount());
        Assert.assertEquals(
                "https://jkforum.net/attachment/313734700125183?t=516f&c=3:4",
                result.getSelectedItems().get(0).getMediaUrl()
        );
    }

    @Test
    public void buildResultDoesNotSelectLargePlacementBackedByTinyDecodedImage() {
        WebResourceMediaCollector collector = new WebResourceMediaCollector(
                "https://example.com/gallery",
                "manual_input"
        );
        collector.observeDom(
                "https://cdn.example.com/stretched-placeholder.png",
                "image",
                320,
                320,
                32,
                32,
                true,
                true,
                true,
                "gallery image"
        );

        XhsParseResult result = collector.buildResult();

        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getSelectedCount());
    }

    @Test
    public void buildResultSelectsVideoAndRejectsHlsPlaylist() {
        WebResourceMediaCollector collector = new WebResourceMediaCollector(
                "https://example.com/video/1",
                "manual_input"
        );
        collector.observeDom(
                "https://media.example.com/play?id=1",
                "video",
                1080,
                1920,
                1080,
                1920,
                true,
                true,
                true,
                "player"
        );
        collector.observeRequest("https://media.example.com/master.m3u8");

        XhsParseResult result = collector.buildResult();

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getMediaCount());
        XhsMediaItem item = result.getMediaItems().get(0);
        Assert.assertEquals(XhsMediaType.VIDEO, item.getMediaType());
        Assert.assertTrue(item.isSelected());
    }

    @Test
    public void collectorRejectsDataAndNonMediaRequests() {
        WebResourceMediaCollector collector = new WebResourceMediaCollector(
                "https://example.com/post/1",
                "manual_input"
        );
        collector.observeRequest("data:image/gif;base64,AAAA");
        collector.observeRequest("https://example.com/app.js");
        collector.observeRequest("https://example.com/styles.css");

        Assert.assertEquals(0, collector.size());
        Assert.assertNull(collector.buildResult());
    }
}
