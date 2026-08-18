package com.shuqiang.captain.xhs.download;

import android.content.Context;
import android.util.Log;

import com.shuqiang.captain.xhs.model.XhsSaveItemResult;

import java.io.IOException;

/**
 * 单项任务状态与资源的唯一协调者，成功、失败或取消后都清除应用私有临时明文。
 */
public final class XhsDownloadCoordinator {
    private static final String TAG = "XhsDownloadCoord";
    private final MediaDownloadDispatcher dispatcher = new MediaDownloadDispatcher();
    private volatile MediaDownloadContext currentContext;
    private volatile boolean cancellationRequested;

    public XhsSaveItemResult download(Context context, String noteId, XhsDownloadItem item,
                                      int selectedIndex, MediaDownloadContext.ProgressListener listener,
                                      DownloadHttpSession httpSession, RuntimeMediaSession runtimeSession)
            throws IOException {
        MediaDownloadContext downloadContext = null;
        try {
            Log.i(TAG, "download started, itemId=" + item.getId() + ", transport=" + item.getTransport()
                    + ", quality=" + item.getQualityHeight());
            downloadContext = new MediaDownloadContext(context, noteId, item, selectedIndex, listener,
                    httpSession, runtimeSession);
            currentContext = downloadContext;
            if (cancellationRequested) {
                downloadContext.cancel();
            }
            XhsSaveItemResult result = dispatcher.download(downloadContext, item);
            downloadContext.transition(DownloadStage.COMPLETED, 1, 1);
            Log.i(TAG, "download completed, itemId=" + item.getId()
                    + ", status=" + result.getSaveStatus());
            return result;
        } catch (DownloadCancelledException exception) {
            Log.i(TAG, "download cancelled, itemId=" + item.getId()
                    + ", stage=" + (downloadContext == null ? "initializing" : downloadContext.getStage()));
            throw exception;
        } catch (IOException exception) {
            Log.e(TAG, "download failed, itemId=" + item.getId()
                    + ", stage=" + (downloadContext == null ? "initializing" : downloadContext.getStage())
                    + ", error=" + exception.getClass().getSimpleName());
            throw exception;
        } finally {
            if (downloadContext != null) {
                downloadContext.cleanUp();
            }
            currentContext = null;
        }
    }

    public boolean cancelCurrent() {
        cancellationRequested = true;
        MediaDownloadContext activeContext = currentContext;
        if (activeContext != null) {
            activeContext.cancel();
        }
        return activeContext != null;
    }

    public void resetCancellation() {
        cancellationRequested = false;
    }
}
