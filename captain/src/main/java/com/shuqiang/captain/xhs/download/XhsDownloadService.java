package com.shuqiang.captain.xhs.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.shuqiang.captain.MainActivity;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsSaveItemResult;
import com.shuqiang.captain.xhs.model.XhsSaveStatus;
import com.shuqiang.captain.xhs.model.XhsSaveSummary;
import com.shuqiang.captain.xhs.storage.XhsMediaSaver;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import captain.R;

/**
 * 串行执行保存任务，确保页面退出后任务不中断。
 */
public class XhsDownloadService extends Service {
    private static final String CHANNEL_ID = "captain_xhs_download";
    private static final int NOTIFICATION_ID = 2001;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final XhsMediaSaver mediaSaver = new XhsMediaSaver();
    private volatile boolean running;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || !XhsDownloadContract.ACTION_START_DOWNLOAD.equals(intent.getAction())) {
            return START_NOT_STICKY;
        }
        final XhsParseResult parseResult = (XhsParseResult) intent.getSerializableExtra(XhsDownloadContract.EXTRA_PARSE_RESULT);
        if (parseResult == null || running) {
            return START_NOT_STICKY;
        }
        running = true;
        XhsDownloadProgressStore.clear();
        startForeground(NOTIFICATION_ID, buildNotification(
                getString(R.string.xhs_download_notification_title),
                "准备开始保存",
                0,
                0,
                true
        ));
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                handleDownload(parseResult);
            }
        });
        return START_NOT_STICKY;
    }

    private void handleDownload(XhsParseResult parseResult) {
        List<XhsMediaItem> selectedItems = parseResult.getSelectedItems();
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
            XhsMediaItem mediaItem = selectedItems.get(i);
            String progressMessage = "正在保存 " + (i + 1) + "/" + totalCount + " · " + mediaItem.getMediaType().getDisplayName();
            dispatchProgress(new XhsSaveSummary(totalCount, processedCount, successCount, failedCount, duplicateCount,
                    false, progressMessage, lastSavedUri));
            updateNotification(progressMessage, processedCount, totalCount, true);

            try {
                XhsSaveItemResult saveItemResult = mediaSaver.save(this, parseResult, mediaItem, i + 1);
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
                processedCount++;
                failedCount++;
                String message = "保存失败：" + mediaItem.getMediaType().getDisplayName() + " " + (i + 1);
                dispatchProgress(new XhsSaveSummary(totalCount, processedCount, successCount, failedCount, duplicateCount,
                        false, message, lastSavedUri));
                updateNotification(message, processedCount, totalCount, true);
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

    private void finishWithSummary(XhsSaveSummary saveSummary) {
        dispatchProgress(saveSummary);
        updateNotification(saveSummary.getCurrentMessage(), saveSummary.getProcessedCount(), saveSummary.getTotalCount(), false);
        running = false;
        stopForeground(false);
        stopSelf();
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
        executorService.shutdownNow();
        super.onDestroy();
    }
}
