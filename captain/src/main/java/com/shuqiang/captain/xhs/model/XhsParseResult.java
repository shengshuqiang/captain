package com.shuqiang.captain.xhs.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 统一承载页面解析结果，供页面展示与保存服务复用。
 */
public class XhsParseResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String noteId;
    private final String pageUrl;
    private final String canonicalUrl;
    private final String authorName;
    private final String title;
    private final String coverUrl;
    private final String parseStrategy;
    private final String entrySource;
    private final ArrayList<XhsMediaItem> mediaItems;

    public XhsParseResult(String noteId, String pageUrl, String canonicalUrl, String authorName,
                          String title, String coverUrl, String parseStrategy, String entrySource,
                          ArrayList<XhsMediaItem> mediaItems) {
        this.noteId = noteId;
        this.pageUrl = pageUrl;
        this.canonicalUrl = canonicalUrl;
        this.authorName = authorName;
        this.title = title;
        this.coverUrl = coverUrl;
        this.parseStrategy = parseStrategy;
        this.entrySource = entrySource;
        this.mediaItems = mediaItems;
    }

    public String getNoteId() {
        return noteId;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getTitle() {
        return title;
    }

    public String getDisplayTitle() {
        return title == null || title.trim().isEmpty() ? "网页资源" : title.trim();
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getParseStrategy() {
        return parseStrategy;
    }

    public String getEntrySource() {
        return entrySource;
    }

    public ArrayList<XhsMediaItem> getMediaItems() {
        return mediaItems;
    }

    public int getMediaCount() {
        return mediaItems == null ? 0 : mediaItems.size();
    }

    public int getSelectedCount() {
        if (mediaItems == null) {
            return 0;
        }
        int selectedCount = 0;
        for (XhsMediaItem item : mediaItems) {
            if (item.isSelected()) {
                selectedCount++;
            }
        }
        return selectedCount;
    }

    public boolean hasSelection() {
        return getSelectedCount() > 0;
    }

    public XhsMediaType getPrimaryMediaType() {
        if (mediaItems == null || mediaItems.isEmpty()) {
            return XhsMediaType.IMAGE;
        }
        return mediaItems.get(0).getMediaType();
    }

    public List<XhsMediaItem> getSelectedItems() {
        ArrayList<XhsMediaItem> selectedItems = new ArrayList<>();
        if (mediaItems == null) {
            return selectedItems;
        }
        for (XhsMediaItem item : mediaItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }
}
