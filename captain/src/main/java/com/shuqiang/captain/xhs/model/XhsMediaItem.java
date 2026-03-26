package com.shuqiang.captain.xhs.model;

import java.io.Serializable;
import java.util.Locale;

/**
 * 页面中提取到的单个视频或图片资源。
 */
public class XhsMediaItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final XhsMediaType mediaType;
    private final String mediaUrl;
    private final String coverUrl;
    private final int width;
    private final int height;
    private final int durationSec;
    private final String fileExtension;
    private boolean selected;

    public XhsMediaItem(String id, XhsMediaType mediaType, String mediaUrl, String coverUrl,
                        int width, int height, int durationSec, String fileExtension, boolean selected) {
        this.id = id;
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.coverUrl = coverUrl;
        this.width = width;
        this.height = height;
        this.durationSec = durationSec;
        this.fileExtension = fileExtension;
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public XhsMediaType getMediaType() {
        return mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getDisplayDuration() {
        if (durationSec <= 0) {
            return "";
        }
        int minutes = durationSec / 60;
        int seconds = durationSec % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
