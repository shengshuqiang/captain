package com.shuqiang.captain.xhs.model;

import java.io.Serializable;

/**
 * 下载服务实时广播的聚合结果。
 */
public class XhsSaveSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int totalCount;
    private final int processedCount;
    private final int successCount;
    private final int failedCount;
    private final int duplicateCount;
    private final boolean complete;
    private final String currentMessage;
    private final String lastSavedUri;

    public XhsSaveSummary(int totalCount, int processedCount, int successCount, int failedCount,
                          int duplicateCount, boolean complete, String currentMessage, String lastSavedUri) {
        this.totalCount = totalCount;
        this.processedCount = processedCount;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.duplicateCount = duplicateCount;
        this.complete = complete;
        this.currentMessage = currentMessage;
        this.lastSavedUri = lastSavedUri;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public boolean isComplete() {
        return complete;
    }

    public String getCurrentMessage() {
        return currentMessage;
    }

    public String getLastSavedUri() {
        return lastSavedUri;
    }

    public boolean hasAnySuccess() {
        return successCount > 0 || duplicateCount > 0;
    }

    public boolean isPartialSuccess() {
        return hasAnySuccess() && failedCount > 0;
    }
}
