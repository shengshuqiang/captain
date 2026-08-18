package com.shuqiang.captain.xhs.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.shuqiang.captain.MainActivity;
import com.shuqiang.captain.xhs.model.XhsSaveItemResult;
import com.shuqiang.captain.xhs.model.XhsSaveStatus;
import com.shuqiang.captain.xhs.model.XhsSaveSummary;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import captain.R;

/**
 * 串行执行保存任务，确保页面退出后任务不中断。
 */
public class XhsDownloadService extends Service {
    private static final String TAG = "XhsDownloadService";
    private static final String CHANNEL_ID = "captain_xhs_download";
    private static final int NOTIFICATION_ID = 2001;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final XhsDownloadCoordinator downloadCoordinator = new XhsDownloadCoordinator();
    private volatile boolean running;
    private volatile boolean cancelRequested;
    private volatile Future<?> runningTask;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && XhsDownloadContract.ACTION_CANCEL_DOWNLOAD.equals(intent.getAction())) {
            requestCancel();
            return START_NOT_STICKY;
        }
        if (intent == null || !XhsDownloadContract.ACTION_START_DOWNLOAD.equals(intent.getAction())) {
            return START_NOT_STICKY;
        }
        final XhsDownloadRequest downloadRequest = (XhsDownloadRequest) intent.getSerializableExtra(
                XhsDownloadContract.EXTRA_DOWNLOAD_REQUEST);
        if (downloadRequest == null) {
            return START_NOT_STICKY;
        }
        if (running) {
            RuntimeMediaSessionStore.getInstance().discard(downloadRequest.getRuntimeSessionId());
            return START_NOT_STICKY;
        }
        running = true;
        cancelRequested = false;
        downloadCoordinator.resetCancellation();
        XhsDownloadProgressStore.clear();
        startForeground(NOTIFICATION_ID, buildNotification(
                getString(R.string.xhs_download_notification_title),
                "准备开始保存",
                0,
                0,
                true
        ));
        runningTask = executorService.submit(new Runnable() {
            @Override
            public void run() {
                handleDownload(downloadRequest);
            }
        });
        return START_NOT_STICKY;
    }

    private void handleDownload(final XhsDownloadRequest downloadRequest) {
        RuntimeMediaSession runtimeSession = null;
        DownloadHttpSession httpSession = null;
        try {
            if (downloadRequest.getRuntimeSessionId() != null) {
                runtimeSession = RuntimeMediaSessionStore.getInstance().claim(
                        downloadRequest.getRuntimeSessionId());
                if (runtimeSession == null) {
                    int failedCount = downloadRequest.getItems().size();
                    finishWithSummary(new XhsSaveSummary(failedCount, failedCount, 0, failedCount, 0,
                            true, "浏览会话已失效，请重新验证并提取", null));
                    return;
                }
            }
            httpSession = new DownloadHttpSession(runtimeSession);
            handleDownloadItems(downloadRequest, httpSession, runtimeSession);
        } finally {
            if (httpSession != null) {
                httpSession.close();
            }
            if (runtimeSession != null) {
                runtimeSession.close();
            }
        }
    }

    private void handleDownloadItems(final XhsDownloadRequest downloadRequest,
                                     final DownloadHttpSession httpSession,
                                     final RuntimeMediaSession runtimeSession) {
        List<XhsDownloadItem> selectedItems = downloadRequest.getItems();
        int totalCount = selectedItems.size();
        int processedCount = 0;
        int successCount = 0;
        int failedCount = 0;
        int duplicateCount = 0;
        String lastSavedUri = null;

        if (totalCount == 0) {
            finishWithSummary(new XhsSaveSummary(0, 0, 0, 0, 0, true, "没有可保存的资源", null));
            return;
        }

        for (int i = 0; i < totalCount; i++) {
            if (cancelRequested || Thread.currentThread().isInterrupted()) {
                finishCancelled(totalCount, processedCount, successCount, failedCount, duplicateCount, lastSavedUri);
                return;
            }
            final XhsDownloadItem mediaItem = selectedItems.get(i);
            String progressMessage = "正在保存 " + (i + 1) + "/" + totalCount + " · " + mediaItem.getMediaType().getDisplayName();
            dispatchProgress(new XhsSaveSummary(totalCount, processedCount, successCount, failedCount, duplicateCount,
                    false, progressMessage, lastSavedUri));
            updateNotification(progressMessage, processedCount, totalCount, true);

            try {
                final int completedBefore = processedCount;
                final int successBefore = successCount;
                final int failedBefore = failedCount;
                final int duplicateBefore = duplicateCount;
                final String savedUriBefore = lastSavedUri;
                XhsSaveItemResult saveItemResult = downloadCoordinator.download(
                        this,
                        downloadRequest.getNoteId(),
                        mediaItem,
                        i + 1,
                        new MediaDownloadContext.ProgressListener() {
                            @Override
                            public void onProgress(DownloadStage stage, int current, int total) {
                                String stageMessage = stage.getMessage();
                                if (total > 0) {
                                    stageMessage += " " + Math.min(current + 1, total) + "/" + total;
                                }
                                dispatchProgress(new XhsSaveSummary(totalCount, completedBefore,
                                        successBefore, failedBefore, duplicateBefore, false,
                                        stageMessage, savedUriBefore));
                                updateNotification(stageMessage, completedBefore, totalCount, true);
                            }
                        },
                        httpSession,
                        runtimeSession);
                processedCount++;
                if (saveItemResult.getSaveStatus() == XhsSaveStatus.SUCCESS) {
                    successCount++;
                    lastSavedUri = saveItemResult.getSavedUri();
                } else if (saveItemResult.getSaveStatus() == XhsSaveStatus.SKIPPED_DUPLICATE) {
                    duplicateCount++;
                    lastSavedUri = saveItemResult.getSavedUri();
                } else {
                    failedCount++;
                }
                dispatchProgress(new XhsSaveSummary(totalCount, processedCount, successCount, failedCount, duplicateCount,
                        false, saveItemResult.getMessage(), lastSavedUri));
                updateNotification(saveItemResult.getMessage(), processedCount, totalCount, true);
            } catch (Exception e) {
                if (cancelRequested || Thread.currentThread().isInterrupted()
                        || e instanceof DownloadCancelledException) {
                    finishCancelled(totalCount, processedCount, successCount, failedCount,
                            duplicateCount, lastSavedUri);
                    return;
                }
                processedCount++;
                failedCount++;
                String message = safeFailureMessage(e, mediaItem, i + 1);
                dispatchProgress(new XhsSaveSummary(totalCount, processedCount, successCount, failedCount, duplicateCount,
                        false, message, lastSavedUri));
                updateNotification(message, processedCount, totalCount, true);
                Log.e(TAG, "item save failed, itemId=" + mediaItem.getId()
                        + ", transport=" + mediaItem.getTransport()
                        + ", error=" + e.getClass().getSimpleName());
            }
        }

        String finishMessage;
        if (failedCount == 0) {
            finishMessage = getString(R.string.xhs_download_notification_done);
        } else if (successCount > 0 || duplicateCount > 0) {
            finishMessage = "部分资源已保存";
        } else {
            finishMessage = getString(R.string.xhs_download_notification_failed);
        }
        finishWithSummary(new XhsSaveSummary(totalCount, processedCount, successCount, failedCount, duplicateCount,
                true, finishMessage, lastSavedUri));
    }

    private String safeFailureMessage(Exception exception, XhsDownloadItem item, int index) {
        String detail = exception == null ? null : exception.getMessage();
        if (detail != null && detail.startsWith("浏览会话已失效")) {
            return detail;
        }
        return "保存失败：" + item.getMediaType().getDisplayName() + " " + index;
    }

    private void finishWithSummary(XhsSaveSummary saveSummary) {
        dispatchProgress(saveSummary);
        updateNotification(saveSummary.getCurrentMessage(), saveSummary.getProcessedCount(), saveSummary.getTotalCount(), false);
        running = false;
        runningTask = null;
        stopForeground(false);
        stopSelf();
    }

    private void finishCancelled(int totalCount, int processedCount, int successCount,
                                 int failedCount, int duplicateCount, String lastSavedUri) {
        finishWithSummary(new XhsSaveSummary(totalCount, processedCount, successCount, failedCount,
                duplicateCount, true, "下载已取消，临时文件已清理", lastSavedUri));
    }

    private void requestCancel() {
        if (!running) {
            return;
        }
        cancelRequested = true;
        boolean activeContext = downloadCoordinator.cancelCurrent();
        Future<?> activeTask = runningTask;
        if (activeTask != null && activeContext) {
            activeTask.cancel(true);
        }
    }

    private void dispatchProgress(XhsSaveSummary saveSummary) {
        XhsDownloadProgressStore.update(saveSummary);
        sendBroadcast(XhsDownloadContract.buildProgressIntent(this, saveSummary));
    }

    private void updateNotification(String contentText, int progress, int total, boolean ongoing) {
        Notification notification = buildNotification(
                getString(R.string.xhs_download_notification_title),
                contentText,
                progress,
                total,
                ongoing
        );
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Notification buildNotification(String title, String contentText, int progress, int total, boolean ongoing) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_file_save)
                .setContentTitle(title)
                .setContentText(contentText)
                .setOnlyAlertOnce(true)
                .setOngoing(ongoing)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        if (ongoing) {
            PendingIntent cancelIntent = PendingIntent.getService(this, 1,
                    XhsDownloadContract.buildCancelIntent(this), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_close, "取消", cancelIntent);
        }
        if (total > 0) {
            builder.setProgress(total, progress, false);
        }
        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.xhs_download_notification_channel),
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("网页资源保存进度");
        notificationManager.createNotificationChannel(channel);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        requestCancel();
        executorService.shutdownNow();
        super.onDestroy();
    }
}
