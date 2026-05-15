package com.shuqiang.captain.xhs.parser;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 淘宝动态详情页需要浏览器上下文执行脚本，WebView 仅作为静态解析失败后的可视化兜底。
 */
public final class TaobaoWebViewSniffer {
    private static final String TAG = "TaobaoWebViewSniffer";
    private static final int SNIFF_TIMEOUT_MS = 18000;
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile("(?:https?:)?//[^\\s\"'<>\\\\]+");
    private static final String COLLECT_MEDIA_JS = "(function(){"
            + "var urls=[];var seen={};"
            + "function add(v){"
            + "if(!v||typeof v!=='string'){return;}"
            + "v=v.replace(/\\\\u002F/g,'/').replace(/\\\\\\//g,'/').replace(/&amp;/g,'&');"
            + "var re=/(https?:)?\\/\\/[^\\s\\\"'<>\\\\]+/g;var m;"
            + "while((m=re.exec(v))){var u=m[0];if(u.indexOf('//')===0){u='https:'+u;}"
            + "if(!seen[u]){seen[u]=1;urls.push(u);}}"
            + "}"
            + "function walk(o,d){"
            + "if(!o||d>5){return;}"
            + "if(typeof o==='string'){add(o);return;}"
            + "if(typeof o!=='object'){return;}"
            + "if(Array.isArray(o)){for(var i=0;i<o.length&&i<80;i++){walk(o[i],d+1);}return;}"
            + "for(var k in o){"
            + "if(!Object.prototype.hasOwnProperty.call(o,k)){continue;}"
            + "if(/video|play|url|src|data|item|api|mtop/i.test(k)){try{walk(o[k],d+1);}catch(e){}}"
            + "}"
            + "}"
            + "try{add(document.documentElement?document.documentElement.innerHTML:'');}catch(e){}"
            + "for(var key in window){"
            + "if(/data|video|item|api|mtop|taobao/i.test(key)){try{walk(window[key],0);}catch(e){}}"
            + "}"
            + "return urls.slice(0,120);"
            + "})()";

    private final WebView webView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (completed) {
                return;
            }
            completed = true;
            Log.w(TAG, "taobao webview sniff timeout, page=" + TaobaoShareParser.describeUrlForLog(pageUrl));
            Callback activeCallback = callback;
            if (activeCallback != null) {
                activeCallback.onSniffFailed("页面已打开，但未嗅探到可保存的视频资源。");
            }
        }
    };

    private Callback callback;
    private String pageUrl;
    private String entrySource;
    private boolean completed = true;

    public interface Callback {
        void onStatusChanged(String message);

        void onMediaFound(XhsParseResult parseResult);

        void onSniffFailed(String reason);
    }

    public TaobaoWebViewSniffer(WebView webView) {
        this.webView = webView;
        configureWebView();
    }

    public static boolean canSniff(String pageUrl) {
        return TaobaoShareParser.isSupportedPage(pageUrl);
    }

    public void start(String pageUrl, String entrySource, Callback callback) {
        stop();
        this.pageUrl = XhsNetworkPolicy.forceHttps(pageUrl);
        this.entrySource = entrySource;
        this.callback = callback;
        this.completed = false;
        if (!canSniff(this.pageUrl)) {
            completed = true;
            if (callback != null) {
                callback.onSniffFailed("当前链接不是淘宝详情页。");
            }
            return;
        }
        Log.d(TAG, "taobao webview sniff start, page=" + TaobaoShareParser.describeUrlForLog(this.pageUrl));
        if (callback != null) {
            callback.onStatusChanged("正在打开淘宝页面并监听视频资源。");
        }
        mainHandler.postDelayed(timeoutRunnable, SNIFF_TIMEOUT_MS);
        webView.loadUrl(this.pageUrl);
    }

    public void stop() {
        mainHandler.removeCallbacks(timeoutRunnable);
        callback = null;
        pageUrl = null;
        entrySource = null;
        completed = true;
        try {
            webView.stopLoading();
            webView.loadUrl("about:blank");
        } catch (Exception ignored) {
            // WebView 已销毁时无需继续处理。
        }
    }

    public void destroy() {
        stop();
        try {
            webView.destroy();
        } catch (Exception ignored) {
            // ignore
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString(XhsHttpClient.MOBILE_USER_AGENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
        webView.setWebViewClient(new SniffingWebViewClient());
        webView.setWebChromeClient(new SniffingWebChromeClient());
    }

    private void inspectCandidateText(String rawText, String source) {
        if (completed || TextUtils.isEmpty(rawText)) {
            return;
        }
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        addCandidate(candidates, rawText);
        addCandidate(candidates, Uri.decode(rawText));
        Matcher matcher = HTTP_URL_PATTERN.matcher(Uri.decode(rawText));
        while (matcher.find()) {
            addCandidate(candidates, matcher.group());
        }
        for (String candidate : candidates) {
            String normalizedUrl = TaobaoShareParser.normalizeSniffedUrl(pageUrl, candidate);
            if (TaobaoShareParser.isLikelyDownloadableVideoUrl(normalizedUrl)) {
                completeWithMediaUrl(normalizedUrl, source);
                return;
            }
        }
    }

    private void addCandidate(LinkedHashSet<String> candidates, String rawCandidate) {
        if (TextUtils.isEmpty(rawCandidate)) {
            return;
        }
        String candidate = rawCandidate.trim()
                .replace("&amp;", "&")
                .replace("\\u002F", "/")
                .replace("\\/", "/");
        if (candidate.startsWith("//")) {
            candidate = "https:" + candidate;
        }
        if (candidate.startsWith("http://") || candidate.startsWith("https://")) {
            candidates.add(candidate);
        }
    }

    private void completeWithMediaUrl(final String mediaUrl, final String source) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (completed) {
                    return;
                }
                XhsParseResult parseResult = TaobaoShareParser.buildSniffedVideoResult(
                        mediaUrl,
                        pageUrl,
                        entrySource,
                        source
                );
                if (parseResult == null) {
                    return;
                }
                completed = true;
                mainHandler.removeCallbacks(timeoutRunnable);
                Log.d(TAG, "taobao webview sniff found media, source=" + source
                        + ", page=" + TaobaoShareParser.describeUrlForLog(pageUrl)
                        + ", mediaHost=" + describeHost(mediaUrl));
                Callback activeCallback = callback;
                if (activeCallback != null) {
                    activeCallback.onMediaFound(parseResult);
                }
            }
        });
    }

    private void evaluatePageCandidates(final String source) {
        if (completed) {
            return;
        }
        webView.evaluateJavascript(COLLECT_MEDIA_JS, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                inspectJavascriptResult(value, source);
            }
        });
    }

    private void inspectJavascriptResult(String value, String source) {
        if (completed || TextUtils.isEmpty(value) || "null".equals(value)) {
            return;
        }
        try {
            Object parsedValue = new JSONTokener(value).nextValue();
            if (parsedValue instanceof JSONArray) {
                inspectJsonArray((JSONArray) parsedValue, source);
            } else if (parsedValue instanceof String) {
                inspectJsonArray(new JSONArray((String) parsedValue), source);
            }
        } catch (Exception exception) {
            Log.d(TAG, "ignore taobao js sniff result, source=" + source
                    + ", error=" + exception.getClass().getSimpleName());
        }
    }

    private void inspectJsonArray(JSONArray jsonArray, String source) {
        for (int i = 0; i < jsonArray.length(); i++) {
            inspectCandidateText(jsonArray.optString(i), source);
            if (completed) {
                return;
            }
        }
    }

    private String describeHost(String url) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            return host == null ? "unknown" : host.toLowerCase(Locale.US);
        } catch (Exception ignored) {
            return "invalid";
        }
    }

    private final class SniffingWebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (request != null && request.getUrl() != null) {
                inspectCandidateText(request.getUrl().toString(), "webview_request");
            }
            return null;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            inspectCandidateText(url, "webview_request_legacy");
            return null;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request != null && request.getUrl() != null) {
                inspectCandidateText(request.getUrl().toString(), "webview_navigation");
            }
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            inspectCandidateText(url, "webview_navigation_legacy");
            return false;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            inspectCandidateText(url, "webview_resource");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!TaobaoShareParser.isSupportedPage(url)) {
                return;
            }
            if (callback != null && !completed) {
                callback.onStatusChanged("页面已打开，正在扫描页面数据。");
            }
            evaluatePageCandidates("webview_page_finished");
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    evaluatePageCandidates("webview_page_delayed");
                }
            }, 1200);
        }
    }

    private final class SniffingWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress >= 80 && TaobaoShareParser.isSupportedPage(view.getUrl())) {
                evaluatePageCandidates("webview_progress_" + newProgress);
            }
        }
    }
}
