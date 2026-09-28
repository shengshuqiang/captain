package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 下载任务只保存稳定页面定位信息，下载开始时再解析短效媒体地址。
 */
public final class XhsDownloadRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String noteId;
    private final ArrayList<XhsDownloadItem> items;
    private final String runtimeSessionId;

    private XhsDownloadRequest(String noteId, ArrayList<XhsDownloadItem> items, String runtimeSessionId) {
        this.noteId = noteId;
        this.items = items;
        this.runtimeSessionId = runtimeSessionId;
    }

    public static XhsDownloadRequest from(XhsParseResult parseResult) {
        ArrayList<XhsDownloadItem> items = new ArrayList<>();
        String runtimeSessionId = null;
        if (parseResult != null) {
            for (XhsMediaItem mediaItem : parseResult.getSelectedItems()) {
                if (mediaItem.requiresRuntimeSession()) {
                    if (runtimeSessionId == null) {
                        runtimeSessionId = mediaItem.getRuntimeSessionId();
                    } else if (!runtimeSessionId.equals(mediaItem.getRuntimeSessionId())) {
                        throw new IllegalArgumentException("选中资源来自不同的浏览会话");
                    }
                }
                items.add(XhsDownloadItem.from(mediaItem));
            }
        }
        return new XhsDownloadRequest(parseResult == null ? "web" : parseResult.getNoteId(), items,
                runtimeSessionId);
    }

    public String getNoteId() {
        return noteId;
    }

    public List<XhsDownloadItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getRuntimeSessionId() {
        return runtimeSessionId;
    }
}
