package com.shuqiang.captain.xhs.ui;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.shuqiang.captain.xhs.download.RuntimeMediaSessionStore;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.parser.WebViewResourceSniffer;

/** 浏览会话持续存在；清单只是覆盖层，只有明确退出网页时才释放 WebView 页面。 */
final class ManualWebExtractionController {
    interface Listener {
        void onExtractionReady(XhsParseResult result);
        void onResourcesUpdated(XhsParseResult result);
        boolean canUpdateResources();
        void onPageShown();
        void onClosed();
    }

    private final Context context;
    private final View container;
    private final TextView statusView;
    private final Button extractButton;
    private final WebView webView;
    private final WebViewResourceSniffer sniffer;
    private final RuntimeMediaSessionStore sessionStore = RuntimeMediaSessionStore.getInstance();
    private final Listener listener;
    private boolean active;
    private boolean listVisible;
    private XhsParseResult latestResult;

    ManualWebExtractionController(Context context, View container, TextView statusView,
                                  Button closeButton, Button refreshButton, Button extractButton,
                                  WebView webView, Listener listener) {
        this.context = context;
        this.container = container;
        this.statusView = statusView;
        this.extractButton = extractButton;
        this.webView = webView;
        this.sniffer = new WebViewResourceSniffer(webView);
        this.listener = listener;
        closeButton.setOnClickListener(view -> close());
        refreshButton.setOnClickListener(view -> {
            if (active) webView.reload();
        });
        extractButton.setOnClickListener(view -> {
            if (active) {
                if (!listener.canUpdateResources()) {
                    statusView.setText("正在保存所选资源，请完成后再更新清单。");
                    return;
                }
                // 即使候选为空也打开清单，后续资源仍能持续加入。
                listVisible = true;
                listener.onExtractionReady(register(latestResult));
                sniffer.captureCurrentPageMedia();
            }
        });
    }

    void open(String pageUrl, String entrySource) {
        if (active) sniffer.stop();
        hideKeyboard();
        active = true;
        listVisible = false;
        latestResult = null;
        container.setVisibility(View.VISIBLE);
        container.bringToFront();
        webView.onResume();
        webView.resumeTimers();
        extractButton.setEnabled(true);
        statusView.setText("正常浏览网页，新资源会持续加入清单。");
        sniffer.startInteractive(pageUrl, entrySource, new WebViewResourceSniffer.Callback() {
            @Override
            public void onStatusChanged(String message) {
                if (active) statusView.setText(message);
            }

            @Override
            public void onMediaFound(XhsParseResult result) {
                onResourcesChanged(result);
            }

            @Override
            public void onResourcesChanged(XhsParseResult result) {
                if (!active) return;
                latestResult = result;
                int count = result == null ? 0 : result.getMediaCount();
                extractButton.setText("发现资源 " + count + " 项 · 查看清单");
                if (listener.canUpdateResources() && (listVisible || result == null)) {
                    listener.onResourcesUpdated(register(result));
                }
            }

            @Override
            public void onSniffFailed(String reason) {
                if (active) {
                    extractButton.setEnabled(true);
                    statusView.setText(reason);
                }
            }

            @Override
            public void onCaptureStateChanged(boolean capturing) {
                if (active) extractButton.setEnabled(!capturing);
            }
        });
    }

    /** 登记只保存已观察到的请求上下文，不下载候选资源。 */
    private XhsParseResult register(XhsParseResult result) {
        if (result == null) return null;
        try {
            CookieManager cookies = CookieManager.getInstance();
            return sessionStore.capture(result, webView.getSettings().getUserAgentString(),
                    webView.getUrl(), cookies::getCookie);
        } catch (RuntimeException exception) {
            statusView.setText("资源会话登记失败，请刷新清单后重试。");
            return null;
        }
    }

    void refreshList() {
        if (active && listVisible && listener.canUpdateResources()) {
            listener.onResourcesUpdated(register(latestResult));
        }
    }

    /** 预览使用独立句柄，持续发现更新清单时不会释放正在预览的上下文。 */
    XhsParseResult registerPreview(XhsParseResult result) {
        return active ? register(result) : null;
    }

    void showPage() {
        if (!active) return;
        listVisible = false;
        container.bringToFront();
        listener.onPageShown();
    }

    boolean isActive() { return active; }

    boolean handleBackPressed() {
        if (!active) return false;
        if (listVisible) showPage();
        else if (webView.canGoBack()) webView.goBack();
        else close();
        return true;
    }

    void onResume() {
        if (active) {
            webView.onResume();
            webView.resumeTimers();
            sniffer.setObservationPaused(false);
        }
    }

    void onPause() {
        if (active) {
            sniffer.setObservationPaused(true);
            webView.onPause();
            webView.pauseTimers();
        }
    }

    void destroy() {
        active = false;
        sniffer.destroy();
    }

    void close() {
        if (!active) return;
        active = false;
        listVisible = false;
        latestResult = null;
        sniffer.stop();
        webView.onPause();
        container.setVisibility(View.GONE);
        extractButton.setEnabled(true);
        listener.onClosed();
    }

    private void hideKeyboard() {
        InputMethodManager input = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (input != null) input.hideSoftInputFromWindow(container.getWindowToken(), 0);
        container.requestFocus();
    }
}
