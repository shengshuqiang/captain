package com.shuqiang.captain.xhs.ui;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/** 验证大量资源下分类、选择和预览位置的业务约束。 */
public class XhsMediaGroupsTest {
    @Test
    public void twoHundredImagesDoNotBuryVideosAndSizesDescendWithinCategory() {
        ArrayList<XhsMediaItem> input = new ArrayList<>();
        for (int i = 0; i < 200; i++) input.add(item("small" + i, XhsMediaType.IMAGE, 200, 200));
        XhsMediaItem video = item("video", XhsMediaType.VIDEO, 1920, 1080);
        XhsMediaItem large = item("large", XhsMediaType.IMAGE, 2000, 2000);
        XhsMediaItem larger = item("larger", XhsMediaType.IMAGE, 4000, 4000);
        input.addAll(Arrays.asList(large, video, larger));
        XhsMediaGroups groups = new XhsMediaGroups();
        groups.setItems(input);
        Assert.assertSame(video, groups.getItems().get(0));
        Assert.assertSame(larger, groups.getItems().get(1));
        Assert.assertSame(large, groups.getItems().get(2));
        Assert.assertEquals(3, groups.getRows().size()); // 默认只显示三个分类标题。
        Assert.assertEquals(200, groups.count(XhsMediaGroups.Category.SMALL_IMAGE, false));
        groups.toggleExpansion(XhsMediaGroups.Category.SMALL_IMAGE);
        Assert.assertEquals(203, groups.getRows().size());
        Assert.assertEquals("small0", groups.getItems().get(3).getId());
        Assert.assertEquals("small199", groups.getItems().get(202).getId());
    }

    @Test
    public void classifiesThresholdsAndUnknownSeparatelyWithoutOverflow() {
        Assert.assertEquals(XhsMediaGroups.Category.LARGE_IMAGE,
                XhsMediaGroups.categoryOf(item("large", XhsMediaType.IMAGE, 1000, 1000)));
        Assert.assertEquals(XhsMediaGroups.Category.MEDIUM_IMAGE,
                XhsMediaGroups.categoryOf(item("medium", XhsMediaType.IMAGE, 1000, 100)));
        Assert.assertEquals(XhsMediaGroups.Category.SMALL_IMAGE,
                XhsMediaGroups.categoryOf(item("small", XhsMediaType.IMAGE, 999, 100)));
        Assert.assertEquals(XhsMediaGroups.Category.UNKNOWN_IMAGE,
                XhsMediaGroups.categoryOf(item("unknown", XhsMediaType.IMAGE, 1000, 0)));
        Assert.assertEquals(XhsMediaGroups.Category.LARGE_IMAGE,
                XhsMediaGroups.categoryOf(item("huge", XhsMediaType.IMAGE, 100000, 100000)));
    }

    @Test
    public void categoryToggleIncludesHiddenItemsAndLeavesOtherCategoriesAlone() {
        XhsMediaItem one = item("one", XhsMediaType.IMAGE, 200, 200);
        XhsMediaItem two = item("two", XhsMediaType.IMAGE, 100, 100);
        XhsMediaItem video = item("video", XhsMediaType.VIDEO, 640, 360);
        one.setSelected(true);
        XhsMediaGroups groups = new XhsMediaGroups();
        groups.setItems(Arrays.asList(one, two, video));
        Assert.assertTrue(groups.isCollapsed(XhsMediaGroups.Category.SMALL_IMAGE));
        Assert.assertEquals(2, groups.toggleSelection(XhsMediaGroups.Category.SMALL_IMAGE).size());
        Assert.assertTrue(one.isSelected());
        Assert.assertTrue(two.isSelected());
        Assert.assertFalse(video.isSelected());
        groups.toggleSelection(XhsMediaGroups.Category.SMALL_IMAGE);
        Assert.assertFalse(one.isSelected());
        Assert.assertFalse(two.isSelected());
    }

    @Test
    public void headerAndCollapsedRowsKeepSortedPreviewMappingAfterLiveUpdate() {
        XhsMediaGroups groups = new XhsMediaGroups();
        XhsMediaItem small = item("small", XhsMediaType.IMAGE, 100, 100);
        XhsMediaItem pdf = item("pdf", XhsMediaType.PDF, 0, 0);
        XhsMediaItem video = item("video", XhsMediaType.VIDEO, 640, 360);
        groups.setItems(Arrays.asList(small, pdf, video));
        groups.toggleExpansion(XhsMediaGroups.Category.VIDEO);
        groups.toggleExpansion(XhsMediaGroups.Category.DOCUMENT);
        for (XhsMediaGroups.Row row : groups.getRows()) {
            if (!row.isHeader()) Assert.assertEquals(row.category,
                    XhsMediaGroups.categoryOf(groups.getItems().get(row.mediaPosition)));
        }
        groups.toggleExpansion(XhsMediaGroups.Category.SMALL_IMAGE);
        groups.setItems(Arrays.asList(small, pdf, video));
        Assert.assertFalse(groups.isCollapsed(XhsMediaGroups.Category.SMALL_IMAGE));
        Assert.assertEquals(6, groups.getRows().size());
        groups.resetExpansion();
        Assert.assertTrue(groups.isCollapsed(XhsMediaGroups.Category.SMALL_IMAGE));
    }

    @Test
    public void videosWithOnlyQualityMetadataStillSortHighestFirst() {
        XhsMediaGroups groups = new XhsMediaGroups();
        XhsMediaItem low = qualityVideo("low", 480);
        XhsMediaItem high = qualityVideo("high", 1080);
        groups.setItems(Arrays.asList(low, high));
        Assert.assertSame(high, groups.getItems().get(0));
        Assert.assertSame(low, groups.getItems().get(1));
        Assert.assertEquals(0, high.getWidth());
    }

    private static XhsMediaItem qualityVideo(String id, int quality) {
        return new XhsMediaItem(id, XhsMediaType.VIDEO, "https://fixture.example/" + id, null,
                0, 0, 0, "mp4", false,
                com.shuqiang.captain.xhs.model.XhsMediaTransport.HLS_STREAM, null,
                com.shuqiang.captain.xhs.model.XhsRequestMode.AUTO, null, quality);
    }

    private static XhsMediaItem item(String id, XhsMediaType type, int width, int height) {
        return new XhsMediaItem(id, type, "https://fixture.example/" + id, null,
                width, height, 0, type == XhsMediaType.VIDEO ? "mp4" : "jpg", false);
    }
}
