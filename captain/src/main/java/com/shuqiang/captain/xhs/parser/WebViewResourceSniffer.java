package com.shuqiang.captain.xhs.parser;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
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
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
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
    private static final int INTERACTIVE_FINAL_SCAN_MS = 1000;
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
    private static final String COLLECT_DOM_MEDIA_JS = "(function(){"
            + "var out=[];var seen={};"
            + "var preview=window.__captainMediaPreview;"
            + "if(!preview){preview={states:{},dimensions:{},images:{},count:0};window.__captainMediaPreview=preview;}"
            + "if(!preview.dimensions){preview.dimensions={};}"
            + "function absolute(u){if(!u||typeof u!=='string'){return '';}"
            + "u=u.replace(/&amp;/g,'&');try{return new URL(u,document.baseURI).href;}catch(e){return '';}}"
            + "function largeBox(b){if(!b||b.width<=0||b.height<=0){return false;}"
            + "var max=Math.max(b.width,b.height);var min=Math.min(b.width,b.height);"
            + "return max>=240&&(b.width*b.height)>=40000&&(max/min)<=4;}"
            + "function rememberSize(u,w,h){if(u&&w>0&&h>0){preview.dimensions[u]={width:Math.round(w),height:Math.round(h)};}}"
            + "function sourceSize(u){return preview.dimensions[u]||{width:0,height:0};}"
            + "function ensureProbe(u,b){u=absolute(u);if(!largeBox(b)||!/^https?:\\/\\//i.test(u)"
            + "||preview.states[u]||preview.count>=60){return;}preview.states[u]='loading';preview.count++;"
            + "var p=new Image();preview.images[u]=p;"
            + "p.onload=function(){preview.states[u]='ready';rememberSize(u,p.naturalWidth,p.naturalHeight);delete preview.images[u];};"
            + "p.onerror=function(){preview.states[u]='failed';delete preview.images[u];};p.src=u;}"
            + "function add(u,k,mime,w,h,sw,sh,v,ready,hint){u=absolute(u);"
            + "if(!/^https?:\\/\\//i.test(u)||seen[u]){return;}seen[u]=1;"
            + "out.push({url:u,kind:k,mime:mime||'',width:w||0,height:h||0,sourceWidth:sw||0,sourceHeight:sh||0,"
            + "visible:!!v,ready:!!ready,hints:hint||''});"
            + "}"
            + "function box(e){var r=e.getBoundingClientRect?e.getBoundingClientRect():null;"
            + "return {width:Math.round(r?r.width:(e.width||0)),height:Math.round(r?r.height:(e.height||0))};}"
            + "function state(e,b){var s=window.getComputedStyle?getComputedStyle(e):null;"
            + "return (!s||(s.display!=='none'&&s.visibility!=='hidden'&&s.opacity!=='0'))&&b.width>0&&b.height>0;}"
            + "function loaded(e){return !!e.complete&&(e.naturalWidth||0)>0&&(e.naturalHeight||0)>0;}"
            + "var imgs=document.querySelectorAll('img');"
            + "for(var i=0;i<imgs.length&&i<240;i++){var e=imgs[i];try{if(e.loading==='lazy'){e.loading='eager';}}catch(q){}"
            + "var b=box(e);var ok=loaded(e);var s=window.getComputedStyle?getComputedStyle(e):null;"
            + "var parent=e.parentElement;var link=e.closest?e.closest('a'):null;"
            + "var hint=(e.className||'')+' '+(e.alt||'')+' '+(e.getAttribute('role')||'')+' '+(e.getAttribute('loading')||'')"
            + "+' '+(s?s.filter:'')+' '+(parent&&typeof parent.className==='string'?parent.className:'')+' '+(link?link.href:'');"
            + "var loadedUrl=ok?absolute(e.currentSrc||e.src):'';"
            + "if(loadedUrl){preview.states[loadedUrl]='ready';rememberSize(loadedUrl,e.naturalWidth,e.naturalHeight);}"
            + "var urls=[e.currentSrc,e.src,e.getAttribute('data-src'),e.getAttribute('data-original'),e.getAttribute('data-lazy-src')];"
            + "for(var j=0;j<urls.length;j++){var resolved=absolute(urls[j]);"
            + "ensureProbe(resolved,b);var d=sourceSize(resolved);add(resolved,'image','',b.width,b.height,d.width,d.height,state(e,b),"
            + "(ok&&resolved===loadedUrl)||preview.states[resolved]==='ready',hint);}"
            + "var sets=[e.getAttribute('srcset'),e.getAttribute('data-srcset')];"
            + "for(var z=0;z<sets.length;z++){if(!sets[z]){continue;}var parts=sets[z].split(',');"
            + "for(var n=0;n<parts.length;n++){var srcPart=parts[n].trim().split(/\\s+/)[0];var setUrl=absolute(srcPart);"
            + "ensureProbe(setUrl,b);var sd=sourceSize(setUrl);add(setUrl,'image','',b.width,b.height,sd.width,sd.height,state(e,b),"
            + "(ok&&setUrl===loadedUrl)||preview.states[setUrl]==='ready',hint);}}}"
            + "var videos=document.querySelectorAll('video,video source');"
            + "for(var x=0;x<videos.length&&x<80;x++){var m=videos[x];var host=m.tagName&&m.tagName.toLowerCase()==='source'?m.parentElement:m;"
            + "var vb=box(host||m);var mime=m.getAttribute('type')||(host&&host.getAttribute?host.getAttribute('type'):'')||'';"
            + "var mediaUrls=[m.currentSrc,m.src,m.getAttribute('src'),m.getAttribute('data-src')];"
            + "for(var q=0;q<mediaUrls.length;q++){add(mediaUrls[q],'video',mime,vb.width,vb.height,(host&&host.videoWidth)||0,(host&&host.videoHeight)||0,state(host||m,vb),!!mediaUrls[q],m.className||'');}}"
            + "var links=document.querySelectorAll('a[href$=\".pdf\"],embed[type=\"application/pdf\"],object[type=\"application/pdf\"]');"
            + "for(var y=0;y<links.length&&y<40;y++){var p=links[y];var pb=box(p);"
            + "add(p.href||p.src||p.data,'pdf','application/pdf',pb.width,pb.height,0,0,state(p,pb),true,p.className||'');}"
            + "return JSON.stringify({title:document.title||'',items:out});"
            + "})()";

    private final WebView webView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
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

    public interface Callback {
        void onStatusChanged(String message);

        void onMediaFound(XhsParseResult parseResult);

        void onSniffFailed(String reason);

        default void onCaptureStateChanged(boolean capturing) {
            // 自动嗅探调用方无需处理手动快照状态。
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

    /** 用户完成页面交互后主动触发两阶段 DOM 快照，空结果不会关闭浏览会话。 */
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
            callback.onStatusChanged("正在读取当前页面媒体，请保持页面不动。");
            callback.onCaptureStateChanged(true);
        }
        evaluatePageCandidates("webview_manual_prime");
        final long expectedSession = sessionGeneration;
        final long expectedPage = pageGeneration;
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isActiveScan(expectedSession, expectedPage)) {
                    interactiveSnapshotPending = false;
                    if (callback != null) {
                        callback.onCaptureStateChanged(false);
                    }
                    return;
                }
                evaluatePageCandidates("webview_manual_final");
            }
        }, INTERACTIVE_FINAL_SCAN_MS);
        return true;
    }

    public void stop() {
        sessionGeneration++;
        pageGeneration = 0L;
        completed = true;
        mainHandler.removeCallbacks(timeoutRunnable);
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
            webView.evaluateJavascript(COLLECT_DOM_MEDIA_JS, new ValueCallback<String>() {
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
                            item.optString("hints")
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
        if (!isActiveScan(expectedSession, expectedPage) || genericCollector == null) {
            return;
        }
        interactiveSnapshotPending = false;
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
        completeWithParseResult(parseResult, source, expectedSession, expectedPage);
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
            if (interactiveMode && genericCollector != null) {
                genericCollector.resetForPage(url);
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
            pageGeneration++;
            removePageScanCallbacks();
            if (callback != null && !completed) {
                callback.onStatusChanged(interactiveMode
                        ? "页面已打开，请完成确认、关闭弹窗或播放视频后手动提取。"
                        : taobaoMode
                        ? "页面已打开，正在扫描页面数据。"
                        : "页面已打开，正在校验最终内容大图和实际预览状态。");
            }
            evaluatePageCandidates("webview_page_finished");
            mainHandler.postDelayed(pageDelayedRunnable, 1200);
            if (!taobaoMode && !interactiveMode) {
                mainHandler.postDelayed(genericPrimeRunnable, GENERIC_PRIME_SCAN_MS);
                mainHandler.postDelayed(genericFinalScanRunnable, GENERIC_FINAL_SCAN_MS);
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
