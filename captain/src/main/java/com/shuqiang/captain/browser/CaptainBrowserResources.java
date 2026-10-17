package com.shuqiang.captain.browser;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.Toast;
import com.shuqiang.captain.xhs.parser.BrowserResourceSession;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import fulguris.integration.CaptainBrowserBridge;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/** Passive browser observer; no replacement of the upstream WebViewClient or networking. */
public final class CaptainBrowserResources implements CaptainBrowserBridge.Listener {
    private static final String TAG = "CaptainBrowserResources";
    private static final long SCAN_INTERVAL_MS = 1500;
    private static final long SCAN_TIMEOUT_MS = 1800;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Map<WebView, Page> pages = new WeakHashMap<>();

    @Override public void onPageStarted(WebView view, String url) {
        onDestroyed(view);
        if (!isHttp(url)) return;
        Page page = new Page(url);
        synchronized (pages) { pages.put(view, page); }
        WeakReference<WebView> weakView = new WeakReference<>(view);
        page.poll = new Runnable() {
            @Override public void run() {
                WebView current = weakView.get();
                if (current == null || pageFor(current) != page) return;
                // Skip background tabs; retain their discovered candidates until closed/navigation.
                if (current.isShown()) scan(current, page, null);
                main.postDelayed(this, SCAN_INTERVAL_MS);
            }
        };
        main.postDelayed(page.poll, SCAN_INTERVAL_MS);
        Log.i(TAG, "event=page_started id=" + page.id);
    }

    @Override public void onRequest(WebView view, WebResourceRequest request) {
        Page page = pageFor(view);
        if (page != null && request != null) page.resources.observeRequest(request.getUrl().toString());
    }

    @Override public void onPageFinished(WebView view) {
        Page page = pageFor(view);
        if (page != null) scan(view, page, null);
    }

    @Override public void onDestroyed(WebView view) {
        Page removed;
        synchronized (pages) { removed = pages.remove(view); }
        if (removed != null && removed.poll != null) main.removeCallbacks(removed.poll);
    }

    @Override public void openResources(Activity activity, WebView view, boolean incognito) {
        // The upstream private browser runs in another process; never cross that credential boundary.
        if (incognito) {
            message(activity, "隐私标签暂不支持资源导出，请在普通标签中打开");
            return;
        }
        if (view == null) { message(activity, "请先打开一个网页"); return; }
        Page page = pageFor(view);
        if (page == null && isHttp(view.getUrl())) {
            onPageStarted(view, view.getUrl());
            page = pageFor(view);
        }
        if (page == null) { message(activity, "当前页面没有可保存的网页资源"); return; }
        Page selected = page;
        scan(view, selected, () -> {
            if (activity.isFinishing() || pageFor(view) != selected) return;
            XhsParseResult result = selected.resources.snapshot();
            if (result == null || result.getMediaCount() == 0) {
                message(activity, "尚未发现资源，请播放视频后再查看");
                return;
            }
            BrowserResourcePanel.show(activity, view, result, incognito);
        });
    }

    private void scan(WebView view, Page page, Runnable after) {
        if (page.scanPending) {
            if (after != null) after.run();
            return;
        }
        page.scanPending = true;
        java.util.concurrent.atomic.AtomicBoolean finished = new java.util.concurrent.atomic.AtomicBoolean();
        Runnable complete = () -> {
            if (!finished.compareAndSet(false, true)) return;
            page.scanPending = false;
            if (pageFor(view) == page && after != null) after.run();
        };
        // Busy page scripts must not leave the resource action waiting indefinitely.
        main.postDelayed(complete, SCAN_TIMEOUT_MS);
        try {
            view.evaluateJavascript(page.resources.snapshotScript(), value -> {
                if (pageFor(view) != page || finished.get()) return;
                try { page.resources.observeSnapshot(value); }
                catch (Exception exception) {
                    Log.w(TAG, "event=snapshot_failed id=" + page.id
                            + " error=" + exception.getClass().getSimpleName());
                }
                main.removeCallbacks(complete);
                complete.run();
            });
        } catch (RuntimeException exception) {
            Log.w(TAG, "event=scan_failed id=" + page.id
                    + " error=" + exception.getClass().getSimpleName());
            main.removeCallbacks(complete);
            complete.run();
        }
    }

    private Page pageFor(WebView view) { synchronized (pages) { return pages.get(view); } }
    private static boolean isHttp(String url) {
        return url != null && (url.startsWith("https://") || url.startsWith("http://"));
    }
    private static void message(Activity activity, String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
    private static final class Page {
        final String id = java.util.UUID.randomUUID().toString();
        final BrowserResourceSession resources;
        Runnable poll;
        boolean scanPending;
        Page(String url) { resources = new BrowserResourceSession(url); }
    }
}
