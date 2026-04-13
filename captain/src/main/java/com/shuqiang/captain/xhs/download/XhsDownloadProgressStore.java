package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsSaveSummary;

/**
 * 仅缓存当前保存任务的最近一次进度，避免页面在前后台切换时错过终态广播。
 */
public final class XhsDownloadProgressStore {
    private static XhsSaveSummary latestSummary;

    private XhsDownloadProgressStore() {
    }

    public static synchronized void update(XhsSaveSummary saveSummary) {
        latestSummary = saveSummary;
    }

    public static synchronized XhsSaveSummary getLatest() {
        return latestSummary;
    }

    public static synchronized void clear() {
        latestSummary = null;
    }
}
