package com.shuqiang.captain.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.shuqiang.captain.xhs.download.RuntimeMediaSessionStore;
import com.shuqiang.captain.xhs.download.XhsDownloadContract;
import com.shuqiang.captain.xhs.download.XhsDownloadProgressStore;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsSaveSummary;
import com.shuqiang.captain.xhs.ui.XhsPreviewActivity;
import java.util.ArrayList;

/** Browser resources enter the existing preview and foreground-save owners. */
final class BrowserResourcePanel {
    private static final String TAG = "BrowserResourcePanel";
    private static final int REQUEST_STORAGE = 8451;
    private BrowserResourcePanel() { }

    static void show(Activity activity, WebView view, XhsParseResult result, boolean incognito) {
        String[] labels = new String[result.getMediaCount()];
        for (int i = 0; i < labels.length; i++) {
            XhsMediaItem item = result.getMediaItems().get(i);
            labels[i] = (i + 1) + ". " + item.getMediaType().getDisplayName() + " · "
                    + item.getTransport().getDisplayName() + " · " + item.getFileExtension();
        }
        new AlertDialog.Builder(activity)
                .setTitle((incognito ? "隐私标签 · " : "") + "网页资源（" + labels.length + "）")
                .setItems(labels, (dialog, position) -> choose(activity, view, result, position))
                .setNegativeButton("关闭", null).show();
    }

    private static void choose(Activity activity, WebView view, XhsParseResult source, int position) {
        new AlertDialog.Builder(activity).setTitle("播放与保存")
                .setMessage("可直接保存到手机，也可先预览、播放结束后再保存。")
                .setPositiveButton("保存", (dialog, which) -> launch(activity, view, source, position, false))
                .setNeutralButton("预览播放", (dialog, which) -> launch(activity, view, source, position, true))
                .setNegativeButton("取消", null).show();
    }

    private static void launch(Activity activity, WebView view, XhsParseResult source,
                               int position, boolean preview) {
        if (!preview && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && androidx.core.content.ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
            Toast.makeText(activity, "请允许存储权限后再次保存", Toast.LENGTH_SHORT).show();
            return;
        }
        XhsSaveSummary active = XhsDownloadProgressStore.getLatest();
        if (!preview && active != null && !active.isComplete()) {
            Toast.makeText(activity, "已有保存任务进行中，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<XhsMediaItem> items = new ArrayList<>();
        XhsMediaItem item = source.getMediaItems().get(position);
        item.setSelected(true);
        items.add(item);
        XhsParseResult single = new XhsParseResult(source.getNoteId(), source.getPageUrl(),
                source.getCanonicalUrl(), source.getAuthorName(), source.getTitle(),
                item.getCoverUrl(), source.getParseStrategy(), source.getEntrySource(), items);
        XhsParseResult registered = null;
        try {
            registered = RuntimeMediaSessionStore.getInstance().capture(single,
                    view.getSettings().getUserAgentString(), source.getPageUrl(),
                    url -> CookieManager.getInstance().getCookie(url));
            if (preview) activity.startActivity(XhsPreviewActivity.buildIntent(activity, registered, 0));
            else {
                XhsDownloadProgressStore.clear();
                Intent intent = XhsDownloadContract.buildStartIntent(activity, registered);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) activity.startForegroundService(intent);
                else activity.startService(intent);
                Toast.makeText(activity, "已开始保存，请查看通知栏进度", Toast.LENGTH_SHORT).show();
            }
            Log.i(TAG, "event=resource_handoff resource=" + item.getId()
                    + " mode=" + (preview ? "preview" : "save"));
        } catch (RuntimeException exception) {
            if (registered != null) RuntimeMediaSessionStore.getInstance().discard(
                    registered.getMediaItems().get(0).getRuntimeSessionId());
            Log.e(TAG, "event=handoff_failed error=" + exception.getClass().getSimpleName());
            Toast.makeText(activity, "无法启动资源播放或保存，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}
