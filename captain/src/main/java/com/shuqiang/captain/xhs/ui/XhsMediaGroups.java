package com.shuqiang.captain.xhs.ui;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/** 分类和显示行共享同一份资源引用，折叠不会影响选择、预览或保存。 */
final class XhsMediaGroups {
    static final long LARGE_IMAGE_PIXELS = 1_000_000L;
    static final long MEDIUM_IMAGE_PIXELS = 100_000L;

    enum Category {
        VIDEO("视频", "按分辨率从大到小"),
        LARGE_IMAGE("大图", "≥100 万像素"),
        MEDIUM_IMAGE("中图", "10 万–100 万像素"),
        SMALL_IMAGE("小图", "<10 万像素"),
        UNKNOWN_IMAGE("尺寸未知图片", "网页未提供尺寸"),
        DOCUMENT("文档", "PDF");

        final String title;
        final String hint;

        Category(String title, String hint) {
            this.title = title;
            this.hint = hint;
        }
    }

    /** 媒体位置指向排序后的完整列表，不能用含分类标题的行位置打开预览。 */
    static final class Row {
        final Category category;
        final int mediaPosition;

        Row(Category category, int mediaPosition) {
            this.category = category;
            this.mediaPosition = mediaPosition;
        }

        boolean isHeader() { return mediaPosition < 0; }
    }

    private final ArrayList<XhsMediaItem> items = new ArrayList<>();
    private final ArrayList<Row> rows = new ArrayList<>();
    private final EnumSet<Category> collapsed = EnumSet.noneOf(Category.class);

    XhsMediaGroups() { resetExpansion(); }

    /** 默认只展示分类摘要，避免 200 张大图仍然铺满列表。 */
    void resetExpansion() {
        collapsed.clear();
        collapsed.addAll(EnumSet.allOf(Category.class));
        rebuildRows();
    }

    void setItems(List<XhsMediaItem> mediaItems) {
        items.clear();
        if (mediaItems != null) items.addAll(mediaItems);
        // 兼容 Android 5.0，稳定排序保留同尺寸资源的发现顺序，不依据勾选状态改变位置。
        Collections.sort(items, (left, right) -> {
            int categoryOrder = categoryOf(left).compareTo(categoryOf(right));
            return categoryOrder != 0 ? categoryOrder
                    : Long.compare(resolutionSortKey(right), resolutionSortKey(left));
        });
        rebuildRows();
    }

    ArrayList<XhsMediaItem> getItems() { return items; }
    List<Row> getRows() { return rows; }
    boolean isCollapsed(Category category) { return collapsed.contains(category); }

    void toggleExpansion(Category category) {
        if (!collapsed.remove(category)) collapsed.add(category);
        rebuildRows();
    }

    int count(Category category, boolean selectedOnly) {
        int count = 0;
        for (XhsMediaItem item : items) {
            if (categoryOf(item) == category && (!selectedOnly || item.isSelected())) count++;
        }
        return count;
    }

    /** 部分选择时补齐全选，全部选择时取消；仅返回本类资源以保存用户覆盖值。 */
    List<XhsMediaItem> toggleSelection(Category category) {
        boolean select = count(category, true) < count(category, false);
        ArrayList<XhsMediaItem> changed = new ArrayList<>();
        for (XhsMediaItem item : items) {
            if (categoryOf(item) == category) {
                item.setSelected(select);
                changed.add(item);
            }
        }
        return changed;
    }

    private void rebuildRows() {
        rows.clear();
        Category last = null;
        for (int i = 0; i < items.size(); i++) {
            Category category = categoryOf(items.get(i));
            if (category != last) {
                rows.add(new Row(category, -1));
                last = category;
            }
            if (!isCollapsed(category)) rows.add(new Row(category, i));
        }
    }

    static Category categoryOf(XhsMediaItem item) {
        if (item.getMediaType() == XhsMediaType.VIDEO) return Category.VIDEO;
        if (item.getMediaType() == XhsMediaType.PDF) return Category.DOCUMENT;
        long pixels = pixelArea(item);
        if (pixels <= 0) return Category.UNKNOWN_IMAGE;
        if (pixels >= LARGE_IMAGE_PIXELS) return Category.LARGE_IMAGE;
        if (pixels >= MEDIUM_IMAGE_PIXELS) return Category.MEDIUM_IMAGE;
        return Category.SMALL_IMAGE;
    }

    /** 按已有宽高计算像素面积，不把分辨率或时长标记为文件体积。 */
    static long pixelArea(XhsMediaItem item) {
        return (long) Math.max(0, item.getWidth()) * Math.max(0, item.getHeight());
    }

    /** 视频未提供宽高时按已有画质档位排序，不向展示或保存数据补造尺寸。 */
    private static long resolutionSortKey(XhsMediaItem item) {
        long area = pixelArea(item);
        if (area > 0 || item.getMediaType() != XhsMediaType.VIDEO) return area;
        long qualityHeight = Math.max(0, item.getQualityHeight());
        return qualityHeight * qualityHeight;
    }
}
