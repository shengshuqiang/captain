package com.shuqiang.captain.xhs.model;

/**
 * 小红书笔记资源类型。
 */
public enum XhsMediaType {
    IMAGE("图片"),
    VIDEO("视频");

    private final String displayName;

    XhsMediaType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
