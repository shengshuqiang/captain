package com.shuqiang.captain.xhs.model;

import java.io.Serializable;

/**
 * 区分最终媒体类型与实际传输协议，避免把 HLS 清单误当成 MP4 文件保存。
 */
public enum XhsMediaTransport implements Serializable {
    DIRECT_FILE("直链"),
    HLS_STREAM("HLS 流");

    private final String displayName;

    XhsMediaTransport(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
