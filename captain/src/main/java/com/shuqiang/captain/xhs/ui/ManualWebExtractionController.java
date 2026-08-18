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

/**
 * 手动网页提取 UI 的唯一状态 owner，负责覆盖层、浏览生命周期及运行态会话登记。
 */
final class ManualWebExtractionController {
    interface Listener {
        void onExtractionReady(XhsParseResult result);

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
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active) {
                    ManualWebExtractionController.this.webView.reload();
                }
            }
        });
        extractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (active && !sniffer.captureCurrentPageMedia()) {
                    statusView.setText("页面尚未准备好，请稍后再试。");
                }
            }
        });
    }

    void open(String pageUrl, String entrySource) {
        if (active) {
            sniffer.stop();
        }
        hideKeyboard();
        active = true;
        container.setVisibility(View.VISIBLE);
        webView.onResume();
        webView.resumeTimers();
        extractButton.setEnabled(true);
        statusView.setText("请在页面中完成确认、关闭弹窗或播放视频，然后点击提取。");
        sniffer.startInteractive(pageUrl, entrySource, new WebViewResourceSniffer.Callback() {
            @Override
            public void onStatusChanged(String message) {
                if (active) {
                    statusView.setText(message);
                }
            }

            @Override
            public void onMediaFound(XhsParseResult parseResult) {
                if (!active) {
                    return;
                }
                try {
                    final CookieManager cookies = CookieManager.getInstance();
                    XhsParseResult registered = sessionStore.capture(
                            parseResult,
                            webView.getSettings().getUserAgentString(),
                            webView.getUrl(),
                            new RuntimeMediaSessionStore.CookieProvider() {
                                @Override
                                public String getCookie(String url) {
                                    return cookies.getCookie(url);
                                }
                            }
                    );
                    closeInternal(false);
                    listener.onExtractionReady(registered);
                } catch (RuntimeException exception) {
                    extractButton.setEnabled(true);
                    statusView.setText("页面媒体登记失败，请重新操作后提取。");
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
                if (active) {
                    extractButton.setEnabled(!capturing);
                }
            }
        });
    }

    boolean isActive() {
        return active;
    }

    boolean handleBackPressed() {
        if (!active) {
            return false;
        }
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            close();
        }
        return true;
    }

    void onResume() {
        if (active) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    void onPause() {
        if (active) {
            webView.onPause();
            webView.pauseTimers();
        }
    }

    void destroy() {
        active = false;
        sniffer.destroy();
    }

    void close() {
        if (!active) {
            return;
        }
        closeInternal(true);
    }

    private void closeInternal(boolean notify) {
        active = false;
        sniffer.stop();
        webView.onPause();
        container.setVisibility(View.GONE);
        extractButton.setEnabled(true);
        if (notify) {
            listener.onClosed();
        }
    }

    private void hideKeyboard() {
        InputMethodManager input = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (input != null) {
            input.hideSoftInputFromWindow(container.getWindowToken(), 0);
        }
        container.requestFocus();
    }
}
