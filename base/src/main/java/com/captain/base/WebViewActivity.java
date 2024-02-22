package com.captain.base;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class WebViewActivity extends AppCompatActivity {
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
        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                long interceptRequestTime = System.currentTimeMillis();
                final String url = request.getUrl().toString();
//                Log.d("MCPN", "TestWebViewActivity#shouldInterceptRequest " + Thread.currentThread().getName() + ", interceptRequestTime=" + interceptRequestTime + ", url=" + url);
                WebResourceResponse htmlWebResourceResponse = shouldInterceptHtmlRequest(url);
                if (htmlWebResourceResponse != null) {
                    return htmlWebResourceResponse;
                }

                WebResourceResponse netWebResourceResponse = shouldInterceptNetRequest(url);
                if (netWebResourceResponse != null) {
                    return netWebResourceResponse;
                }

                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                Toast.makeText(WebViewActivity.this, "onPageFinished", Toast.LENGTH_SHORT).show();
            }
        });
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

        this.setContentView(webView);
        String url= getIntent().getStringExtra(URL_KEY);
        Log.d("MCPN", "TestWebViewActivity#loadUrl, url=" + url);
        webView.loadUrl(url);
    }

    /**
     * 是否使用离线化，断网使用
     * @return
     */
    private boolean isUseOffline () {
        return !Utils.isNetworkConnected(this);
    }

    /**
     * URL 拦截
     * @param url
     * @return
     */
    private WebResourceResponse shouldInterceptHtmlRequest(String url) {
        if (isUseOffline()) {
            for (Map.Entry<Integer, String> entry : OFFLINE_URL_MAP.entrySet()) {
                Integer resID = entry.getKey();
                // 待匹配线上 url
                String matchOnLineUrl = getString(resID);
                // 离线资源名称
                String offlineAssetsFileName = entry.getValue();
                System.out.println("Key: " + resID + ", Value: " + matchOnLineUrl);
                if (url.startsWith(matchOnLineUrl)) {
                    Context appContext = getApplicationContext();
                    Map<String, String> headers = new HashMap(8);
                    headers.put("X-Intercepted-By", "maicai/debugging");
                    headers.put("Connection", "keep-alive");
                    if (offlineAssetsFileName.endsWith(".html")) {
                        headers.put("Content-Type", "text/html; charset=utf-8");
                    } else if (offlineAssetsFileName.endsWith(".png") || offlineAssetsFileName.endsWith(".jpg") || offlineAssetsFileName.endsWith(".webp")) {
                        headers.put("Content-Type", "image/png");
                    }
                    headers.put("Access-Control-Allow-Origin", "*");
                    WebResourceResponse var6;
                    try {
                        InputStream is = appContext.getAssets().open(offlineAssetsFileName);
                        var6 = new WebResourceResponse("text/html", "utf-8", 200, "OK", headers, is);
                        return var6;
                    } catch (IOException var10) {
                        Log.i("TitansOffline", "Assets 中未找到 " + offlineAssetsFileName);
                        var6 = null;
                        return var6;
                    }
                }
            }
        }

        // 返回空表示不拦截
        return null;
    }

    private WebResourceResponse shouldInterceptNetRequest(String url) {
        if (url.contains("/api/c/mallcoin/checkIn/getCheckInMainView")) {
            if (!url.contains("isPreNet")) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Transfer-Encoding", "chunked");
                headers.put("X-Android-Selected-Protocol", "http/1.1");
                headers.put("M-TraceId", "-6035504633014653523");
                headers.put("Server", "openresty");
                headers.put("Connection", "keep-alive");
                headers.put("X-Android-Response-Source", "NETWORK 200");
                headers.put("Vary", "Accept-Encoding");
                headers.put("X-Android-Sent-Millis", "1664267038559");
                headers.put("X-Android-Received-Millis", "1664267038698");
                headers.put("Date", "Tue, 27 Sep 2022 08:23:58 GMT");
                headers.put("Content-Type", "application/json");
                headers.put("charset", "utf-8");
//                        支持跨域
                headers.put("Access-Control-Allow-Origin", "*");
                headers.put("Access-Control-Allow-Headers", "Content-Type");
                String dataStr = "{\"code\":0,\"data\":{\"downgradeCheckIn\":false,\"downgradeAccountInfo\":false,\"checkInButtonStatus\":\"UNCHECKIN\",\"userInfo\":{\"balance\":\"10010\",\"balanceStatus\":1,\"hasCouponCode\":false,\"exchangeThreshold\":false},\"expireDay\":30,\"pushStatus\":true,\"canShare\":true,\"showPop\":false,\"canPopGuidePic\":false,\"isTodayInitUser\":false,\"isPopRewarded\":false,\"taskWithoutRewardNum\":0,\"checkInCount\":2,\"initialUser\":false}}";
                return new WebResourceResponse("application/json", "utf-8", 200, "OK", headers, new BufferedInputStream(new ByteArrayInputStream(dataStr.getBytes())) {
                    @Override
                    public void close() throws IOException {
                        long closeTime = System.currentTimeMillis();
//                        Log.d("MCPN", "TestWebViewActivity#shouldInterceptRequest " + Thread.currentThread().getName() + ", closeTime=" + closeTime+ ", durationTime=" + (closeTime - interceptRequestTime) + ", url=" + url);
                        super.close();
                    }
                });
            } else {
                return null;
            }
        }

        return null;
    }
}
