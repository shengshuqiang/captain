package com.shuqiang.captain.xhs.model;

import java.io.Serializable;

/**
 * 单个资源保存结果，用于服务内汇总和页面回显。
 */
public class XhsSaveItemResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String itemId;
    private final XhsSaveStatus saveStatus;
    private final String savedUri;
    private final String message;
    private final String displayName;

    public XhsSaveItemResult(String itemId, XhsSaveStatus saveStatus, String savedUri,
                             String message, String displayName) {
        this.itemId = itemId;
        this.saveStatus = saveStatus;
        this.savedUri = savedUri;
        this.message = message;
        this.displayName = displayName;
    }

    public String getItemId() {
        return itemId;
    }

    public XhsSaveStatus getSaveStatus() {
        return saveStatus;
    }

    public String getSavedUri() {
        return savedUri;
    }

    public String getMessage() {
        return message;
    }

    public String getDisplayName() {
        return displayName;
    }
}
