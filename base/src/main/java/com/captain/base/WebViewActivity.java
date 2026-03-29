package com.captain.base;

import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class WebViewActivity extends BasePermissionActivity {
    public static final String URL_KEY = "url";
    // 离线化 url 映射关系
    private static Map<Integer, String> OFFLINE_URL_MAP = new HashMap<>();
    static {
        // 隐私政策 url
        OFFLINE_URL_MAP.put(R.string.captain_privacy_policy_url, "captain_privacy_policy.html");
        OFFLINE_URL_MAP.put(R.string.captain_privacy, "captain_privacy.png");
        OFFLINE_URL_MAP.put(R.string.shengshuqiang_weixin, "shengshuqiang-weixin.jpg");
        OFFLINE_URL_MAP.put(R.string.captain_v, "captain.v.1.1.100.png");
    }

    @Override
    protected int getContentLayoutResource() {
        return R.layout.activity_web_view;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        initWebView(webView);

        FrameLayout webContainer = findViewById(R.id.web_container);
        webContainer.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        String url= getIntent().getStringExtra(URL_KEY);
        syncToolbarTitle(url);
        Log.d("MCPN", "TestWebViewActivity#loadUrl, url=" + url);
        webView.loadUrl(url);
    }

    private void syncToolbarTitle(String url) {
        if (TextUtils.equals(url, getString(R.string.captain_privacy_policy_url))) {
            setTitle(R.string.privacy_policy_title);
        }
    }

    protected void initWebView(WebView webView) {
        webView.setWebViewClient(buildWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("MCPN", "TestWebViewActivity#onConsoleMessage " + consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
//        打开webview debug模式
        webView.setWebContentsDebuggingEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    }

    protected BaseWebViewClient buildWebViewClient() {
        return new BaseWebViewClient(this);
    }
}
