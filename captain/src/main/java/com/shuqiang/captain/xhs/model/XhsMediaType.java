package com.shuqiang.captain.xhs.model;

/**
 * 统一资源类型，供网页嗅探列表、预览和保存链路复用。
 */
public enum XhsMediaType {
    IMAGE("图片"),
    VIDEO("视频"),
    PDF("PDF");

    private final String displayName;

    XhsMediaType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
