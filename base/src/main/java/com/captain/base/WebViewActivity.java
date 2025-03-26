package com.captain.base;

import android.Manifest;
import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Base64;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        initWebView(webView);

        this.setContentView(webView);
        String url= getIntent().getStringExtra(URL_KEY);
        Log.d("MCPN", "TestWebViewActivity#loadUrl, url=" + url);
        webView.loadUrl(url);
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
