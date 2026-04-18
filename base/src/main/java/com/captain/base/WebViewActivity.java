package com.captain.base;

import android.Manifest;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.text.TextUtils;
import android.webkit.PermissionRequest;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class WebViewActivity extends BasePermissionActivity {
    public static final String URL_KEY = "url";
    public static final String TITLE_KEY = "title";
    private static final int PERMISSION_RECORD_AUDIO_REQUEST_CODE = 0x00000021;
    private static final String SUAN_SUAN_LE_ASSET_PATH = "/android_asset/suansuanle/index.html";
    private static final String SUAN_SUAN_LE_APP_ASSET_HOST = "appassets.androidplatform.net";
    private static final String SUAN_SUAN_LE_APP_ASSET_PATH = "/assets/suansuanle/index.html";
    private static final String SUAN_SUAN_LE_BRIDGE_NAME = "SuanSuanLeBridge";

    private WebView webView;
    private String currentUrl;
    private PermissionRequest pendingPermissionRequest;
    private SuansuanleVoiceBridge suansuanleVoiceBridge;

    @Override
    protected int getContentLayoutResource() {
        return R.layout.activity_web_view;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUrl = getIntent().getStringExtra(URL_KEY);
        webView = new WebView(this);
        initWebView(webView);
        configureSuansuanleBridgeIfNeeded(webView, currentUrl);

        FrameLayout webContainer = findViewById(R.id.web_container);
        webContainer.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        String title = getIntent().getStringExtra(TITLE_KEY);
        syncToolbarTitle(title, currentUrl);
        Log.d("MCPN", "TestWebViewActivity#loadUrl, url=" + currentUrl);
        webView.loadUrl(currentUrl);
    }

    private void syncToolbarTitle(@Nullable String title, @Nullable String url) {
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
            return;
        }
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

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                handlePermissionRequest(request);
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
//        打开webview debug模式
        webView.setWebContentsDebuggingEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
    }

    protected BaseWebViewClient buildWebViewClient() {
        return new BaseWebViewClient(this) {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                currentUrl = url;
                syncSuansuanleBridge(view, url);
            }
        };
    }

    private void configureSuansuanleBridgeIfNeeded(WebView webView, @Nullable String url) {
        syncSuansuanleBridge(webView, url);
    }

    private void syncSuansuanleBridge(WebView webView, @Nullable String url) {
        if (isSuansuanleUrl(url)) {
            ensureSuansuanleBridge(webView);
            return;
        }
        removeSuansuanleBridge(webView);
    }

    private void ensureSuansuanleBridge(WebView webView) {
        if (suansuanleVoiceBridge != null) {
            return;
        }
        suansuanleVoiceBridge = new SuansuanleVoiceBridge(
                this,
                webView,
                new SuansuanleVoiceBridge.PermissionDelegate() {
                    @Override
                    public boolean hasRecordAudioPermission() {
                        return WebViewActivity.this.hasRecordAudioPermission();
                    }

                    @Override
                    public void requestRecordAudioPermission() {
                        WebViewActivity.this.requestRecordAudioPermission();
                    }
                }
        );
        webView.addJavascriptInterface(suansuanleVoiceBridge, SUAN_SUAN_LE_BRIDGE_NAME);
    }

    private void removeSuansuanleBridge(WebView webView) {
        webView.removeJavascriptInterface(SUAN_SUAN_LE_BRIDGE_NAME);
        if (suansuanleVoiceBridge != null) {
            suansuanleVoiceBridge.destroy();
            suansuanleVoiceBridge = null;
        }
    }

    private boolean isSuansuanleUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getPath();
        if ("file".equalsIgnoreCase(scheme)) {
            return TextUtils.equals(path, SUAN_SUAN_LE_ASSET_PATH);
        }
        return "https".equalsIgnoreCase(scheme)
                && TextUtils.equals(host, SUAN_SUAN_LE_APP_ASSET_HOST)
                && TextUtils.equals(path, SUAN_SUAN_LE_APP_ASSET_PATH);
    }

    private boolean hasRecordAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_RECORD_AUDIO_REQUEST_CODE
        );
    }

    private void handlePermissionRequest(@Nullable PermissionRequest request) {
        if (request == null) {
            return;
        }
        if (!isSuansuanleUrl(currentUrl)) {
            request.deny();
            return;
        }
        String[] grantableResources = resolveGrantableResources(request);
        if (grantableResources.length == 0) {
            request.deny();
            return;
        }
        if (hasRecordAudioPermission()) {
            request.grant(grantableResources);
            return;
        }
        if (pendingPermissionRequest != null) {
            pendingPermissionRequest.deny();
        }
        pendingPermissionRequest = request;
        requestRecordAudioPermission();
    }

    private String[] resolveGrantableResources(PermissionRequest request) {
        List<String> resources = new ArrayList<>();
        for (String resource : request.getResources()) {
            if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) {
                resources.add(resource);
            }
        }
        return resources.toArray(new String[0]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_RECORD_AUDIO_REQUEST_CODE) {
            return;
        }
        boolean granted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (pendingPermissionRequest != null) {
            if (granted) {
                String[] grantableResources = resolveGrantableResources(pendingPermissionRequest);
                if (grantableResources.length > 0) {
                    pendingPermissionRequest.grant(grantableResources);
                } else {
                    pendingPermissionRequest.deny();
                }
            } else {
                pendingPermissionRequest.deny();
            }
            pendingPermissionRequest = null;
        }
        if (suansuanleVoiceBridge != null) {
            suansuanleVoiceBridge.onPermissionResult(granted);
        }
    }

    @Override
    protected void onDestroy() {
        if (pendingPermissionRequest != null) {
            pendingPermissionRequest.deny();
            pendingPermissionRequest = null;
        }
        if (suansuanleVoiceBridge != null) {
            suansuanleVoiceBridge.destroy();
            suansuanleVoiceBridge = null;
        }
        if (webView != null) {
            webView.removeJavascriptInterface(SUAN_SUAN_LE_BRIDGE_NAME);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
