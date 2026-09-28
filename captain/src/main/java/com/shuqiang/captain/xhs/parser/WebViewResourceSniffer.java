package com.shuqiang.captain.xhs.parser;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 静态解析失败后在 WebView 中执行页面脚本，淘宝继续走精确视频嗅探，其他网页汇总运行态媒体资源。
 */
public final class WebViewResourceSniffer {
    private static final String TAG = "WebViewResourceSniffer";
    private static final int SNIFF_TIMEOUT_MS = 18000;
    private static final int GENERIC_PRIME_SCAN_MS = 3500;
    private static final int GENERIC_FINAL_SCAN_MS = 7000;
    private static final int INTERACTIVE_SNAPSHOT_TIMEOUT_MS = 1500;
    private static final long ANY_PAGE_GENERATION = -1L;
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
    private static final String COLLECT_DOM_MEDIA_JS = WebResourceDomSnapshot.SCRIPT;

    private final WebView webView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService alipayParseExecutor = Executors.newSingleThreadExecutor();
    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (completed) {
                return;
            }
            if (!taobaoMode && completeGenericResult("webview_timeout")) {
                return;
            }
            completed = true;
            Log.w(TAG, "webview sniff timeout, sniffId=" + sniffId
                    + ", page=" + TaobaoShareParser.describeUrlForLog(pageUrl)
                    + ", durationMs=" + elapsedMs());
            Callback activeCallback = callback;
            if (activeCallback != null) {
                activeCallback.onSniffFailed("页面已打开，但未监测到可保存的图片、视频或 PDF。");
            }
        }
    };
    private final Runnable genericPrimeRunnable = new Runnable() {
        @Override
        public void run() {
            evaluatePageCandidates("webview_page_priming");
        }
    };
    private final Runnable genericFinalScanRunnable = new Runnable() {
        @Override
        public void run() {
            evaluatePageCandidates("webview_page_final");
        }
    };
    private final Runnable pageDelayedRunnable = new Runnable() {
        @Override
        public void run() {
            evaluatePageCandidates("webview_page_delayed");
        }
    };
    /** 页面脚本繁忙时仍可返回已观察到的地址，避免按钮无限等待 JS 回调。 */
    private final Runnable interactiveSnapshotTimeout = new Runnable() {
        @Override
        public void run() {
            if (!completed && interactiveSnapshotPending) {
                completeInteractiveResult("webview_manual_timeout", sessionGeneration, pageGeneration);
            }
        }
    };

    private Callback callback;
    private String pageUrl;
    private String entrySource;
    private volatile boolean completed = true;
    private boolean taobaoMode;
    private boolean interactiveMode;
    private boolean interactiveSnapshotPending;
    private WebResourceMediaCollector genericCollector;
    private String sniffId;
    private long startedAtMs;
    private volatile long sessionGeneration;
    private long pageGeneration;
    private int pageErrorLogCount;
    private String lastDiscoverySignature;
    /** 仅轮询少量播放器元数据，同时节流网络候选的 UI 通知；不加载任何媒体。 */
    private final Runnable interactiveDiscoveryPoll = new Runnable() {
        @Override
        public void run() {
            if (completed || !interactiveMode) return;
            publishInteractiveResources();
            evaluateDomMedia("webview_player_poll", sessionGeneration, pageGeneration);
            mainHandler.postDelayed(this, 1500);
        }
    };

    public interface Callback {
        void onStatusChanged(String message);

        void onMediaFound(XhsParseResult parseResult);

        void onSniffFailed(String reason);

        default void onCaptureStateChanged(boolean capturing) {
            // 自动嗅探调用方无需处理手动快照状态。
        }

        default void onResourcesChanged(XhsParseResult result) {
            // 自动解析不消费持续发现；null 表示新页面尚未发现候选。
        }
    }

    public WebViewResourceSniffer(WebView webView) {
        this.webView = webView;
        configureWebView();
    }

    public static boolean canSniff(String pageUrl) {
        return pageUrl != null
                && !pageUrl.trim().isEmpty()
                && XhsNetworkPolicy.isAllowedMediaUrl(pageUrl);
    }

    public void start(String pageUrl, String entrySource, Callback callback) {
        startInternal(pageUrl, entrySource, callback, false);
    }

    /** 手动浏览模式持续监听页面资源，只有用户点击提取时才结束并返回结果。 */
    public void startInteractive(String pageUrl, String entrySource, Callback callback) {
        startInternal(pageUrl, entrySource, callback, true);
    }

    private void startInternal(String pageUrl, String entrySource, Callback callback,
                               boolean interactiveMode) {
        stop();
        this.pageUrl = XhsNetworkPolicy.forceHttps(pageUrl);
        this.entrySource = entrySource;
        this.callback = callback;
        this.startedAtMs = System.currentTimeMillis();
        this.sniffId = Integer.toHexString((this.pageUrl + ":" + startedAtMs).hashCode());
        this.interactiveMode = interactiveMode;
        this.taobaoMode = !interactiveMode && TaobaoShareParser.isSupportedPage(this.pageUrl);
        this.genericCollector = taobaoMode ? null : new WebResourceMediaCollector(this.pageUrl, entrySource);
        this.completed = false;
        this.lastDiscoverySignature = null;
        if (!canSniff(this.pageUrl)) {
            completed = true;
            if (callback != null) {
                callback.onSniffFailed("当前链接不是可监测的网页地址。");
            }
            return;
        }
        Log.d(TAG, "webview sniff start, sniffId=" + sniffId + ", taobaoMode=" + taobaoMode
                + ", page=" + TaobaoShareParser.describeUrlForLog(this.pageUrl));
        if (callback != null) {
            callback.onStatusChanged(taobaoMode
                    ? "正在打开淘宝页面并监听视频资源。"
                    : "正在打开动态页面并监测图片、视频和 PDF 资源。");
        }
        if (!interactiveMode) {
            mainHandler.postDelayed(timeoutRunnable, SNIFF_TIMEOUT_MS);
        }
        webView.loadUrl(this.pageUrl);
    }

    /** 用户主动查看资源时读取一次元数据；不为验证候选加载额外媒体。 */
    public boolean captureCurrentPageMedia() {
        if (completed || !interactiveMode || interactiveSnapshotPending || genericCollector == null) {
            return false;
        }
        String currentUrl = webView.getUrl();
        if (!canSniff(currentUrl)) {
            if (callback != null) {
                callback.onStatusChanged("当前页面不是可提取的网页地址。");
            }
            return false;
        }
        interactiveSnapshotPending = true;
        genericCollector.setPageUrl(currentUrl);
        if (callback != null) {
            callback.onStatusChanged("正在整理资源来源，不下载媒体文件。");
            callback.onCaptureStateChanged(true);
        }
        if (AlipayVideoShareParser.isSupportedPage(currentUrl)) {
            resolveAlipayShare(currentUrl, sessionGeneration, pageGeneration);
            return true;
        }
        mainHandler.postDelayed(interactiveSnapshotTimeout, INTERACTIVE_SNAPSHOT_TIMEOUT_MS);
        evaluatePageCandidates("webview_manual_final");
        return true;
    }

    /** 支付宝分享页只有拉起 App 的壳；手动提取复用已有的详情接口解析。 */
    private void resolveAlipayShare(final String shareUrl, final long expectedSession,
                                    final long expectedPage) {
        final String source = entrySource;
        final String requestSniffId = sniffId;
        alipayParseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                XhsParseResult result = null;
                try {
                    result = new XhsParseRepository().parse(shareUrl, source);
                } catch (Exception exception) {
                    Log.w(TAG, "alipay manual parse failed, sniffId=" + requestSniffId
                            + ", error=" + exception.getClass().getSimpleName());
                }
                final XhsParseResult resolved = result;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isActiveScan(expectedSession, expectedPage) || !interactiveSnapshotPending) {
                            return;
                        }
                        interactiveSnapshotPending = false;
                        Callback activeCallback = callback;
                        if (activeCallback == null) {
                            return;
                        }
                        activeCallback.onCaptureStateChanged(false);
                        if (resolved == null) {
                            activeCallback.onSniffFailed("支付宝视频解析失败，请刷新页面后重试。");
                        } else {
                            activeCallback.onMediaFound(resolved);
                        }
                    }
                });
            }
        });
    }

    public void stop() {
        sessionGeneration++;
        pageGeneration = 0L;
        completed = true;
        mainHandler.removeCallbacks(timeoutRunnable);
        mainHandler.removeCallbacks(interactiveDiscoveryPoll);
        removePageScanCallbacks();
        callback = null;
        pageUrl = null;
        entrySource = null;
        genericCollector = null;
        taobaoMode = false;
        interactiveMode = false;
        interactiveSnapshotPending = false;
        sniffId = null;
        startedAtMs = 0L;
        try {
            webView.stopLoading();
            webView.loadUrl("about:blank");
        } catch (Exception ignored) {
            // WebView 已销毁时无需继续处理。
        }
    }

    public void destroy() {
        stop();
        alipayParseExecutor.shutdownNow();
        try {
            webView.destroy();
        } catch (Exception ignored) {
            // ignore
        }
    }

    /** Activity 进入后台时停止轮询；清单覆盖层切换不调用此方法。 */
    public void setObservationPaused(boolean paused) {
        mainHandler.removeCallbacks(interactiveDiscoveryPoll);
        if (!paused && interactiveMode && !completed) {
            mainHandler.postDelayed(interactiveDiscoveryPoll, 1500);
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
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString(XhsHttpClient.MOBILE_USER_AGENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
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
        if (completed || !taobaoMode || TextUtils.isEmpty(rawText)) {
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
        XhsParseResult parseResult = TaobaoShareParser.buildSniffedVideoResult(
                mediaUrl,
                pageUrl,
                entrySource,
                source
        );
        completeWithParseResult(parseResult, source);
    }

    private void completeWithParseResult(final XhsParseResult parseResult, final String source) {
        completeWithParseResult(
                parseResult,
                source,
                sessionGeneration,
                ANY_PAGE_GENERATION
        );
    }

    private void completeWithParseResult(final XhsParseResult parseResult,
                                         final String source,
                                         final long expectedSession,
                                         final long expectedPage) {
        if (parseResult == null) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isActiveCompletion(expectedSession, expectedPage)) {
                    return;
                }
                completed = true;
                mainHandler.removeCallbacks(timeoutRunnable);
                removePageScanCallbacks();
                Log.d(TAG, "webview sniff found media, sniffId=" + sniffId
                        + ", source=" + source
                        + ", page=" + TaobaoShareParser.describeUrlForLog(pageUrl)
                        + ", mediaHost=" + describeHost(parseResult.getMediaItems().get(0).getMediaUrl())
                        + ", mediaCount=" + parseResult.getMediaCount()
                        + ", selectedCount=" + parseResult.getSelectedCount()
                        + ", durationMs=" + elapsedMs());
                Callback activeCallback = callback;
                if (activeCallback != null) {
                    activeCallback.onMediaFound(parseResult);
                }
            }
        });
    }

    private void evaluatePageCandidates(final String source) {
        final long expectedSession = sessionGeneration;
        final long expectedPage = pageGeneration;
        if (!isActiveScan(expectedSession, expectedPage)) {
            return;
        }
        if (!taobaoMode) {
            evaluateDomMedia(source, expectedSession, expectedPage);
            return;
        }
        webView.evaluateJavascript(COLLECT_MEDIA_JS, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (!isActiveScan(expectedSession, expectedPage)) {
                    return;
                }
                inspectJavascriptResult(value, source);
            }
        });
    }

    private void evaluateDomMedia(final String source,
                                  final long expectedSession,
                                  final long expectedPage) {
        try {
            webView.evaluateJavascript("webview_player_poll".equals(source)
                    ? WebResourceDomSnapshot.VIDEO_SCRIPT : COLLECT_DOM_MEDIA_JS, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    inspectDomMediaResult(value, source, expectedSession, expectedPage);
                }
            });
        } catch (RuntimeException exception) {
            handleDomScanFailure(source, expectedSession, expectedPage, exception);
        }
    }

    private void inspectDomMediaResult(String value, String source,
                                       long expectedSession, long expectedPage) {
        if (!isActiveScan(expectedSession, expectedPage)
                || ("webview_manual_final".equals(source) && !interactiveSnapshotPending)
                || genericCollector == null
                || TextUtils.isEmpty(value)
                || "null".equals(value)) {
            return;
        }
        try {
            Object parsedValue = new JSONTokener(value).nextValue();
            String jsonText = parsedValue instanceof String ? (String) parsedValue : value;
            JSONObject payload = new JSONObject(jsonText);
            genericCollector.setPageTitle(payload.optString("title"));
            java.util.Set<String> playingUrls = new java.util.HashSet<>();
            JSONArray playing = payload.optJSONArray("playingUrls");
            if (playing != null) {
                for (int i = 0; i < playing.length(); i++) playingUrls.add(playing.optString(i));
            }
            genericCollector.setCurrentPlaybackUrls(playingUrls);
            JSONArray items = payload.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.optJSONObject(i);
                    if (item == null) {
                        continue;
                    }
                    genericCollector.observeDom(
                            item.optString("url"),
                            item.optString("kind"),
                            item.optString("mime"),
                            item.optInt("width"),
                            item.optInt("height"),
                            item.optInt("sourceWidth"),
                            item.optInt("sourceHeight"),
                            item.optBoolean("visible"),
                            item.optBoolean("ready"),
                            "webview_page_final".equals(source)
                                    || "webview_manual_final".equals(source),
                            item.optString("hints"),
                            item.optString("poster")
                    );
                }
            }
            Log.d(TAG, "webview dom scan, sniffId=" + sniffId + ", source=" + source
                    + ", mediaCount=" + genericCollector.size());
            if ("webview_page_final".equals(source)) {
                completeGenericResult(source, expectedSession, expectedPage);
            } else if ("webview_manual_final".equals(source)) {
                completeInteractiveResult(source, expectedSession, expectedPage);
            }
        } catch (Exception exception) {
            Log.d(TAG, "ignore generic dom scan result, sniffId=" + sniffId + ", source=" + source
                    + ", error=" + exception.getClass().getSimpleName());
            handleDomScanFailure(source, expectedSession, expectedPage, exception);
        }
    }

    private void handleDomScanFailure(String source, long expectedSession, long expectedPage,
                                      Exception exception) {
        if (!"webview_manual_final".equals(source)
                || !isActiveScan(expectedSession, expectedPage)) {
            return;
        }
        interactiveSnapshotPending = false;
        mainHandler.removeCallbacks(interactiveSnapshotTimeout);
        Callback activeCallback = callback;
        if (activeCallback != null) {
            activeCallback.onCaptureStateChanged(false);
            activeCallback.onSniffFailed("读取当前页面失败，请保持页面稳定后重试。");
        }
        Log.d(TAG, "manual dom scan failed, sniffId=" + sniffId
                + ", error=" + exception.getClass().getSimpleName());
    }

    private void observeGenericResource(String resourceUrl) {
        WebResourceMediaCollector collector = genericCollector;
        if (!completed && collector != null) {
            if (interactiveMode) {
                collector.observeInteractiveRequest(resourceUrl);
            } else {
                collector.observeRequest(resourceUrl);
            }
        }
    }

    private void completeInteractiveResult(String source, long expectedSession, long expectedPage) {
        if (!isActiveScan(expectedSession, expectedPage) || genericCollector == null
                || !interactiveSnapshotPending) {
            return;
        }
        interactiveSnapshotPending = false;
        mainHandler.removeCallbacks(interactiveSnapshotTimeout);
        if (callback != null) {
            callback.onCaptureStateChanged(false);
        }
        XhsParseResult parseResult = genericCollector.buildResult();
        if (parseResult == null) {
            if (callback != null) {
                callback.onStatusChanged("当前页面还没有可保存媒体，请完成确认或播放后重试。");
            }
            return;
        }
        // 查看清单只是一次快照，保留页面与监听以覆盖广告结束后的后续资源。
        if (callback != null) callback.onMediaFound(parseResult);
    }

    private void publishInteractiveResources() {
        if (genericCollector == null || callback == null) return;
        if (AlipayVideoShareParser.isSupportedPage(webView.getUrl())) return;
        XhsParseResult result = genericCollector.buildResult();
        StringBuilder signature = new StringBuilder();
        signature.append(pageGeneration);
        if (result != null) {
            signature.append(result.getTitle());
            for (com.shuqiang.captain.xhs.model.XhsMediaItem item : result.getMediaItems()) {
                signature.append('|').append(item.getMediaUrl()).append(':')
                        .append(item.getWidth()).append(':').append(item.getHeight())
                        .append(':').append(item.isCurrentPlayback())
                        .append(':').append(item.getCoverUrl());
            }
        }
        String value = signature.toString();
        if (!value.equals(lastDiscoverySignature)) {
            lastDiscoverySignature = value;
            callback.onResourcesChanged(result);
        }
    }

    private boolean completeGenericResult(String source) {
        return completeGenericResult(source, sessionGeneration, pageGeneration);
    }

    private boolean completeGenericResult(String source,
                                          long expectedSession,
                                          long expectedPage) {
        if (!isActiveScan(expectedSession, expectedPage) || genericCollector == null) {
            return false;
        }
        XhsParseResult parseResult = genericCollector.buildResult();
        if (parseResult == null) {
            return false;
        }
        completeWithParseResult(parseResult, source, expectedSession, expectedPage);
        return true;
    }

    private long elapsedMs() {
        return startedAtMs <= 0L ? 0L : System.currentTimeMillis() - startedAtMs;
    }

    private void removePageScanCallbacks() {
        mainHandler.removeCallbacks(pageDelayedRunnable);
        mainHandler.removeCallbacks(genericPrimeRunnable);
        mainHandler.removeCallbacks(genericFinalScanRunnable);
        mainHandler.removeCallbacks(interactiveSnapshotTimeout);
    }

    private boolean isActiveScan(long expectedSession, long expectedPage) {
        return !completed
                && expectedSession == sessionGeneration
                && expectedPage == pageGeneration;
    }

    private boolean isActiveCompletion(long expectedSession, long expectedPage) {
        return !completed
                && expectedSession == sessionGeneration
                && (expectedPage == ANY_PAGE_GENERATION || expectedPage == pageGeneration);
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
            Log.d(TAG, "ignore taobao js sniff result, sniffId=" + sniffId + ", source=" + source
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

    private void inspectTaobaoDetailRequest(WebResourceRequest request) {
        if (request == null || request.getUrl() == null) {
            return;
        }
        inspectTaobaoDetailRequest(request.getUrl().toString(), request.getRequestHeaders());
    }

    private void inspectTaobaoDetailRequest(String requestUrl, Map<String, String> requestHeaders) {
        if (completed || !TaobaoShareParser.isMtopDetailApiUrl(requestUrl)) {
            return;
        }
        final long expectedSession = sessionGeneration;
        final String requestPageUrl = pageUrl;
        final String requestEntrySource = entrySource;
        try {
            Request request = buildProxyRequest(requestUrl, requestHeaders);
            try (Response response = XhsHttpClient.getClient().newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                byte[] responseBytes = responseBody == null ? new byte[0] : responseBody.bytes();
                String responseText = new String(responseBytes, resolveCharset(responseBody));
                Log.d(TAG, "taobao webview mtop response, sniffId=" + sniffId
                        + ", httpCode=" + response.code()
                        + ", bodyLength=" + responseBytes.length
                        + ", hasDetailMediaHint=" + TaobaoShareParser.hasDetailMediaHint(responseText));
                XhsParseResult parseResult = TaobaoShareParser.parseDetailResponse(
                        responseText,
                        requestPageUrl,
                        requestEntrySource,
                        "webview_mtop_detail"
                );
                completeWithParseResult(
                        parseResult,
                        "webview_mtop_detail",
                        expectedSession,
                        ANY_PAGE_GENERATION
                );
            }
        } catch (Exception exception) {
            Log.d(TAG, "ignore taobao mtop sniff response, sniffId=" + sniffId + ", error="
                    + exception.getClass().getSimpleName());
        }
    }

    private Request buildProxyRequest(String requestUrl, Map<String, String> requestHeaders) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(requestUrl)
                .get()
                .header("User-Agent", XhsHttpClient.MOBILE_USER_AGENT);
        if (requestHeaders == null) {
            return requestBuilder.build();
        }
        for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
            String name = header.getKey();
            String value = header.getValue();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value) || shouldSkipProxyHeader(name)) {
                continue;
            }
            requestBuilder.header(name, value);
        }
        return requestBuilder.build();
    }

    private boolean shouldSkipProxyHeader(String headerName) {
        String lowerName = headerName.toLowerCase(Locale.US);
        return lowerName.equals("accept-encoding")
                || lowerName.equals("content-length")
                || lowerName.equals("host")
                || lowerName.equals("connection");
    }

    private Charset resolveCharset(ResponseBody responseBody) {
        if (responseBody == null || responseBody.contentType() == null) {
            return StandardCharsets.UTF_8;
        }
        Charset charset = responseBody.contentType().charset(StandardCharsets.UTF_8);
        return charset == null ? StandardCharsets.UTF_8 : charset;
    }

    private boolean handleInternalTaobaoNavigation(WebView view, String rawUrl, String source) {
        if (interactiveMode && !canSniff(rawUrl)) {
            if (callback != null && !completed) {
                callback.onStatusChanged("已拦截非网页跳转，请继续在当前页面操作。");
            }
            return true;
        }
        inspectCandidateText(rawUrl, source);
        String h5Url = TaobaoShareParser.extractTaobaoH5UrlFromScheme(rawUrl);
        if (!TextUtils.isEmpty(h5Url)) {
            Log.d(TAG, "taobao webview intercept app scheme, source=" + source
                    + ", target=" + TaobaoShareParser.describeUrlForLog(h5Url));
            if (callback != null && !completed) {
                callback.onStatusChanged("已拦截淘宝 App 跳转，继续在页面内嗅探。");
            }
            view.loadUrl(h5Url);
            return true;
        }
        if (TaobaoShareParser.isTaobaoAppScheme(rawUrl)) {
            Log.d(TAG, "taobao webview block app scheme without h5 target, source=" + source);
            return true;
        }
        return false;
    }

    private final class SniffingWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (completed || !canSniff(url)) {
                return;
            }
            pageGeneration++;
            pageErrorLogCount = 0;
            if (interactiveMode && genericCollector != null) {
                genericCollector.resetForPage(url);
                lastDiscoverySignature = null;
                if (callback != null) callback.onResourcesChanged(null);
                mainHandler.removeCallbacks(interactiveDiscoveryPoll);
                mainHandler.postDelayed(interactiveDiscoveryPoll, 1000);
            }
            if (interactiveSnapshotPending && callback != null) {
                callback.onCaptureStateChanged(false);
            }
            interactiveSnapshotPending = false;
            removePageScanCallbacks();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (request != null && request.getUrl() != null) {
                String requestUrl = request.getUrl().toString();
                inspectCandidateText(requestUrl, "webview_request");
                observeGenericResource(requestUrl);
            }
            inspectTaobaoDetailRequest(request);
            return null;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            inspectCandidateText(url, "webview_request_legacy");
            observeGenericResource(url);
            inspectTaobaoDetailRequest(url, null);
            return null;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return request != null && request.getUrl() != null
                    && handleInternalTaobaoNavigation(view, request.getUrl().toString(), "webview_navigation");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleInternalTaobaoNavigation(view, url, "webview_navigation_legacy");
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            inspectCandidateText(url, "webview_resource");
            observeGenericResource(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (completed || !canSniff(url) || !TextUtils.equals(url, view.getUrl())) {
                return;
            }
            // 手动浏览期间仅监听请求；不反复扫描 DOM 或改变页面的懒加载策略。
            if (interactiveMode) {
                Log.d(TAG, "webview page finished, host=" + describeHost(url)
                        + ", elapsedMs=" + elapsedMs());
                return;
            }
            removePageScanCallbacks();
            if (callback != null && !completed) {
                callback.onStatusChanged(interactiveMode
                        ? "页面已打开，请完成确认、关闭弹窗或播放视频后手动提取。"
                        : taobaoMode
                        ? "页面已打开，正在扫描页面数据。"
                        : "页面已打开，正在整理资源来源信息。");
            }
            evaluatePageCandidates("webview_page_finished");
            mainHandler.postDelayed(pageDelayedRunnable, 1200);
            if (!taobaoMode && !interactiveMode) {
                mainHandler.postDelayed(genericPrimeRunnable, GENERIC_PRIME_SCAN_MS);
                mainHandler.postDelayed(genericFinalScanRunnable, GENERIC_FINAL_SCAN_MS);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            logPageError("network", request.getUrl().toString(), error.getErrorCode());
            if (request.isForMainFrame() && callback != null && !completed) {
                callback.onStatusChanged("网页连接失败，可刷新重试（" + error.getErrorCode() + "）。");
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                        WebResourceResponse response) {
            logPageError("http", request.getUrl().toString(), response.getStatusCode());
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.cancel();
            logPageError("tls", error.getUrl(), error.getPrimaryError());
        }

        /** 每页限量记录主机和错误码，不记录 Cookie 或带签名的资源完整地址。 */
        private void logPageError(String kind, String url, int code) {
            if (!completed && pageErrorLogCount++ < 5) {
                Log.w(TAG, "webview resource error, kind=" + kind + ", host=" + describeHost(url)
                        + ", code=" + code + ", elapsedMs=" + elapsedMs());
            }
        }
    }

    private final class SniffingWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (taobaoMode && newProgress >= 80 && canSniff(view.getUrl())) {
                evaluatePageCandidates("webview_progress_" + newProgress);
            }
        }
    }
}
