package com.shuqiang.captain.xhs.parser;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseError;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;
import okhttp3.Response;

/**
 * 微信视频号短链是典型的运行时拉数页，业内通用解法是跑真实浏览器上下文后再提取数据。
 */
public final class WeixinSphRuntimeParser {
    private static final Pattern SHORT_LINK_PATH_PATTERN = Pattern.compile("^/sph/([A-Za-z0-9]+)$");
    private static final long PARSE_TIMEOUT_MS = 16_000L;
    private static final long POLL_INTERVAL_MS = 350L;
    private static final String CAPTURE_HOOK = buildCaptureHook();
    private static final String SNAPSHOT_SCRIPT = buildSnapshotScript();

    private WeixinSphRuntimeParser() {
    }

    public static boolean isSupportedUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        try {
            URI uri = new URI(url);
            String host = lower(uri.getHost());
            String path = uri.getPath() == null ? "" : uri.getPath();
            if ("weixin.qq.com".equals(host)) {
                return SHORT_LINK_PATH_PATTERN.matcher(path).matches();
            }
            return "channels.weixin.qq.com".equals(host) && "/finder-preview/pages/sph".equals(path);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static XhsParseResult parse(Context context, String url, String entrySource) throws XhsParserException {
        if (context == null) {
            throw new XhsParserException(XhsParseError.NETWORK_ERROR);
        }
        return new Session(context, normalizeEntryUrl(url), entrySource).awaitResult();
    }

    private static String normalizeEntryUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        try {
            URI uri = new URI(url);
            String host = lower(uri.getHost());
            String path = uri.getPath() == null ? "" : uri.getPath();
            if (!"weixin.qq.com".equals(host)) {
                return url;
            }
            Matcher matcher = SHORT_LINK_PATH_PATTERN.matcher(path);
            if (!matcher.matches()) {
                return url;
            }
            return "https://channels.weixin.qq.com/finder-preview/pages/sph?id=" + matcher.group(1);
        } catch (Exception ignored) {
            return url;
        }
    }

    private static String buildCaptureHook() {
        return "<script>(function(){"
                + "if(window.__CAPTAIN_WX_CAPTURE__)return;"
                + "var store=window.__CAPTAIN_WX_CAPTURE__={requests:[],lastFeedInfo:null,lastAuthorInfo:null,lastSceneInfo:null,lastError:null};"
                + "function toJson(text){try{return JSON.parse(text);}catch(e){return null;}}"
                + "function remember(url,payload){try{store.requests.push(String(url||''));if(store.requests.length>6)store.requests.shift();"
                + "if(payload&&typeof payload.errCode!=='undefined'&&payload.errCode!==0){store.lastError={errCode:payload.errCode,errMsg:payload.errMsg||payload.desc||''};}"
                + "var data=payload&&payload.data?payload.data:null;"
                + "if(data&&data.feedInfo)store.lastFeedInfo=data.feedInfo;"
                + "if(data&&data.authorInfo)store.lastAuthorInfo=data.authorInfo;"
                + "if(data&&data.sceneInfo)store.lastSceneInfo=data.sceneInfo;"
                + "}catch(e){}}"
                + "var rawFetch=window.fetch;"
                + "if(rawFetch){window.fetch=function(){return rawFetch.apply(this,arguments).then(function(resp){try{var cloned=resp.clone();cloned.text().then(function(text){var payload=toJson(text);var requestUrl=(cloned&&cloned.url)||'';if(payload&&/feed\\/get_feed_info/.test(requestUrl))remember(requestUrl,payload);});}catch(e){}return resp;});};}"
                + "var rawOpen=XMLHttpRequest.prototype.open;"
                + "var rawSend=XMLHttpRequest.prototype.send;"
                + "XMLHttpRequest.prototype.open=function(method,url){this.__captain_url=url;return rawOpen.apply(this,arguments);};"
                + "XMLHttpRequest.prototype.send=function(){this.addEventListener('load',function(){try{var payload=toJson(this.responseText);var requestUrl=this.responseURL||this.__captain_url||'';if(payload&&/feed\\/get_feed_info/.test(requestUrl))remember(requestUrl,payload);}catch(e){}});return rawSend.apply(this,arguments);};"
                + "})();</script>";
    }

    private static String buildSnapshotScript() {
        return "(function(){"
                + "var store=window.__CAPTAIN_WX_CAPTURE__||{};"
                + "function urls(selector,attr){var list=[];var nodes=document.querySelectorAll(selector);for(var i=0;i<nodes.length;i++){var value=nodes[i].getAttribute(attr);if(value&&list.indexOf(value)<0)list.push(value);}return list;}"
                + "function compactPicInfo(picInfo){var out=[];if(!Array.isArray(picInfo))return out;for(var i=0;i<picInfo.length&&i<12;i++){var item=picInfo[i]||{};out.push({url:item.url||item.coverUrl||'',coverUrl:item.coverUrl||item.url||''});}return out;}"
                + "var feed=store.lastFeedInfo||null;"
                + "var author=store.lastAuthorInfo||null;"
                + "var scene=store.lastSceneInfo||null;"
                + "var reducedFeed=feed?{description:feed.description||'',objectId:feed.objectId||'',coverUrl:feed.coverUrl||'',videoUrl:feed.videoUrl||'',mediaType:feed.mediaType||0,h264VideoInfo:feed.h264VideoInfo?{videoUrl:feed.h264VideoInfo.videoUrl||''}:null,h265VideoInfo:feed.h265VideoInfo?{videoUrl:feed.h265VideoInfo.videoUrl||''}:null,picInfo:compactPicInfo(feed.picInfo)}:null;"
                + "var reducedAuthor=author?{nickname:author.nickname||author.name||'',name:author.name||author.nickname||''}:null;"
                + "var reducedScene=scene?{dynamicExportId:scene.dynamicExportId||'',entryScene:scene.entryScene||'',entryCardType:scene.entryCardType||'',commentScene:scene.commentScene||''}:null;"
                + "return JSON.stringify({currentUrl:location.href,documentTitle:document.title||'',feedInfo:reducedFeed,authorInfo:reducedAuthor,sceneInfo:reducedScene,lastError:store.lastError||null,domVideos:urls('video[src],source[src]','src'),domPosters:urls('video[poster]','poster')});"
                + "})()";
    }

    private static final class Session {
        private final Context context;
        private final String pageUrl;
        private final String entrySource;
        private final Handler mainHandler = new Handler(Looper.getMainLooper());
        private final CountDownLatch latch = new CountDownLatch(1);

        private volatile XhsParseResult result;
        private volatile XhsParserException error;
        private WebView webView;
        private ViewGroup parentView;
        private boolean completed;

        private final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                completeError(new XhsParserException(XhsParseError.NO_MEDIA_FOUND));
            }
        };

        private final Runnable pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (completed || webView == null) {
                    return;
                }
                webView.evaluateJavascript(SNAPSHOT_SCRIPT, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        RuntimeSnapshot snapshot = parseSnapshot(value);
                        if (snapshot == null) {
                            scheduleNextPoll();
                            return;
                        }
                        if (snapshot.hasAccessError()) {
                            completeError(new XhsParserException(XhsParseError.NOTE_UNAVAILABLE));
                            return;
                        }
                        if (snapshot.hasMedia()) {
                            try {
                                completeSuccess(buildParseResult(snapshot, entrySource));
                            } catch (XhsParserException parserException) {
                                completeError(parserException);
                            }
                            return;
                        }
                        scheduleNextPoll();
                    }
                });
            }
        };

        private Session(Context context, String pageUrl, String entrySource) {
            this.context = context;
            this.pageUrl = pageUrl;
            this.entrySource = entrySource;
        }

        private XhsParseResult awaitResult() throws XhsParserException {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            });
            try {
                boolean finished = latch.await(PARSE_TIMEOUT_MS + 2_000L, TimeUnit.MILLISECONDS);
                if (!finished) {
                    throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new XhsParserException(XhsParseError.NETWORK_ERROR, e);
            }
            if (error != null) {
                throw error;
            }
            if (result == null) {
                throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
            }
            return result;
        }

        private void start() {
            if (completed || TextUtils.isEmpty(pageUrl)) {
                completeError(new XhsParserException(XhsParseError.INVALID_INPUT));
                return;
            }
            Context webContext = context instanceof Activity ? context : context.getApplicationContext();
            webView = new WebView(webContext);
            attachHiddenWebView();
            configureWebView();
            mainHandler.postDelayed(timeoutRunnable, PARSE_TIMEOUT_MS);
            webView.loadUrl(pageUrl);
        }

        private void attachHiddenWebView() {
            if (!(context instanceof Activity)) {
                webView.layout(0, 0, 1080, 1920);
                return;
            }
            Activity activity = (Activity) context;
            parentView = activity.findViewById(android.R.id.content);
            if (parentView == null) {
                return;
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1, 1);
            webView.setVisibility(View.INVISIBLE);
            webView.setBackgroundColor(Color.TRANSPARENT);
            parentView.addView(webView, params);
        }

        private void configureWebView() {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(webView, true);
            }
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setLoadsImagesAutomatically(true);
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
            settings.setMediaPlaybackRequiresUserGesture(false);
            settings.setUserAgentString(XhsHttpClient.MOBILE_USER_AGENT);
            webView.setWebViewClient(new RuntimeWebViewClient());
        }

        private void scheduleNextPoll() {
            if (completed) {
                return;
            }
            mainHandler.removeCallbacks(pollRunnable);
            mainHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
        }

        private void completeSuccess(XhsParseResult parseResult) {
            if (completed) {
                return;
            }
            completed = true;
            result = parseResult;
            finish();
        }

        private void completeError(XhsParserException parserException) {
            if (completed) {
                return;
            }
            completed = true;
            error = parserException;
            finish();
        }

        private void finish() {
            mainHandler.removeCallbacks(timeoutRunnable);
            mainHandler.removeCallbacks(pollRunnable);
            if (webView != null) {
                final WebView localWebView = webView;
                webView = null;
                if (parentView != null) {
                    parentView.removeView(localWebView);
                }
                localWebView.stopLoading();
                localWebView.loadUrl("about:blank");
                localWebView.destroy();
            }
            latch.countDown();
        }

        private final class RuntimeWebViewClient extends WebViewClient {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (request == null || !request.isForMainFrame()) {
                    return super.shouldInterceptRequest(view, request);
                }
                String requestUrl = request.getUrl().toString();
                if (!shouldInjectRuntimeHook(requestUrl)) {
                    return super.shouldInterceptRequest(view, request);
                }
                return buildInjectedResponse(requestUrl, request.getRequestHeaders());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                scheduleNextPoll();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request != null && request.isForMainFrame()) {
                    completeError(new XhsParserException(XhsParseError.NOTE_UNAVAILABLE));
                }
            }
        }
    }

    @Nullable
    private static WebResourceResponse buildInjectedResponse(String url, Map<String, String> requestHeaders) {
        Request.Builder builder = new Request.Builder().url(url).get();
        if (requestHeaders != null) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                if (!TextUtils.isEmpty(entry.getKey()) && entry.getValue() != null) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }
        }
        builder.header("User-Agent", XhsHttpClient.MOBILE_USER_AGENT);
        try (Response response = XhsHttpClient.getClient().newCall(builder.build()).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            String html = response.body().string();
            String injectedHtml = injectHook(html);
            return new WebResourceResponse(
                    "text/html",
                    "utf-8",
                    response.code(),
                    response.message(),
                    null,
                    new ByteArrayInputStream(injectedHtml.getBytes(StandardCharsets.UTF_8))
            );
        } catch (IOException ignored) {
            return null;
        }
    }

    private static boolean shouldInjectRuntimeHook(String url) {
        try {
            URI uri = new URI(url);
            return "channels.weixin.qq.com".equals(lower(uri.getHost()))
                    && "/finder-preview/pages/sph".equals(uri.getPath());
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String injectHook(String html) {
        if (TextUtils.isEmpty(html) || html.contains("__CAPTAIN_WX_CAPTURE__")) {
            return html;
        }
        String lowerHtml = html.toLowerCase(Locale.US);
        int scriptIndex = lowerHtml.indexOf("<script");
        if (scriptIndex >= 0) {
            return html.substring(0, scriptIndex) + CAPTURE_HOOK + html.substring(scriptIndex);
        }
        int headEnd = lowerHtml.indexOf("</head>");
        if (headEnd >= 0) {
            return html.substring(0, headEnd) + CAPTURE_HOOK + html.substring(headEnd);
        }
        return CAPTURE_HOOK + html;
    }

    @Nullable
    private static RuntimeSnapshot parseSnapshot(String rawValue) {
        if (TextUtils.isEmpty(rawValue) || "null".equals(rawValue)) {
            return null;
        }
        try {
            JsonElement outer = new JsonParser().parse(rawValue);
            if (outer == null || outer.isJsonNull()) {
                return null;
            }
            String innerJson = outer.getAsString();
            if (TextUtils.isEmpty(innerJson)) {
                return null;
            }
            JsonObject root = new JsonParser().parse(innerJson).getAsJsonObject();
            return new RuntimeSnapshot(root);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static XhsParseResult buildParseResult(RuntimeSnapshot snapshot, String entrySource) throws XhsParserException {
        JsonObject feedInfo = snapshot.feedInfo;
        JsonObject authorInfo = snapshot.authorInfo;
        JsonObject sceneInfo = snapshot.sceneInfo;
        String noteId = firstNonEmpty(
                getString(sceneInfo, "dynamicExportId"),
                getString(feedInfo, "objectId"),
                getQueryParam(snapshot.currentUrl, "id"),
                buildPageId(snapshot.currentUrl)
        );
        String title = firstNonEmpty(
                getString(feedInfo, "description"),
                snapshot.documentTitle,
                "微信视频号"
        );
        String author = firstNonEmpty(
                getString(authorInfo, "nickname"),
                getString(authorInfo, "name"),
                extractHost(snapshot.currentUrl)
        );
        String videoUrl = firstNonEmpty(
                getNestedString(feedInfo, "h264VideoInfo", "videoUrl"),
                getNestedString(feedInfo, "h265VideoInfo", "videoUrl"),
                getString(feedInfo, "videoUrl"),
                firstUrl(snapshot.domVideos)
        );
        String coverUrl = firstNonEmpty(
                getString(feedInfo, "coverUrl"),
                firstPicUrl(feedInfo),
                firstUrl(snapshot.domPosters),
                videoUrl
        );

        ArrayList<XhsMediaItem> mediaItems = new ArrayList<>();
        LinkedHashSet<String> addedUrls = new LinkedHashSet<>();
        if (XhsNetworkPolicy.isAllowedMediaUrl(videoUrl)) {
            mediaItems.add(new XhsMediaItem(
                    noteId + "_video",
                    XhsMediaType.VIDEO,
                    videoUrl,
                    coverUrl,
                    0,
                    0,
                    0,
                    guessExtension(videoUrl, XhsMediaType.VIDEO),
                    true
            ));
            addedUrls.add(videoUrl);
        }
        if (XhsNetworkPolicy.isAllowedMediaUrl(coverUrl) && !addedUrls.contains(coverUrl)) {
            mediaItems.add(new XhsMediaItem(
                    noteId + "_cover",
                    XhsMediaType.IMAGE,
                    coverUrl,
                    coverUrl,
                    0,
                    0,
                    0,
                    guessExtension(coverUrl, XhsMediaType.IMAGE),
                    true
            ));
            addedUrls.add(coverUrl);
        }
        appendPictureItems(mediaItems, addedUrls, feedInfo, noteId);
        if (mediaItems.isEmpty()) {
            throw new XhsParserException(XhsParseError.NO_MEDIA_FOUND);
        }
        return new XhsParseResult(
                noteId,
                snapshot.currentUrl,
                snapshot.currentUrl,
                author,
                title,
                coverUrl,
                "mobile_webview · 微信视频号运行时采集",
                entrySource,
                mediaItems
        );
    }

    private static void appendPictureItems(ArrayList<XhsMediaItem> mediaItems, LinkedHashSet<String> addedUrls,
                                           JsonObject feedInfo, String noteId) {
        JsonArray picInfo = getArray(feedInfo, "picInfo");
        if (picInfo == null) {
            return;
        }
        for (int i = 0; i < picInfo.size(); i++) {
            JsonObject item = getObject(picInfo.get(i));
            String imageUrl = firstNonEmpty(getString(item, "url"), getString(item, "coverUrl"));
            if (!XhsNetworkPolicy.isAllowedMediaUrl(imageUrl) || addedUrls.contains(imageUrl)) {
                continue;
            }
            mediaItems.add(new XhsMediaItem(
                    noteId + "_image_" + (i + 1),
                    XhsMediaType.IMAGE,
                    imageUrl,
                    imageUrl,
                    0,
                    0,
                    0,
                    guessExtension(imageUrl, XhsMediaType.IMAGE),
                    true
            ));
            addedUrls.add(imageUrl);
        }
    }

    private static String firstPicUrl(JsonObject feedInfo) {
        JsonArray picInfo = getArray(feedInfo, "picInfo");
        if (picInfo == null || picInfo.size() == 0) {
            return null;
        }
        JsonObject firstItem = getObject(picInfo.get(0));
        return firstNonEmpty(getString(firstItem, "coverUrl"), getString(firstItem, "url"));
    }

    private static String getNestedString(JsonObject root, String parent, String child) {
        return getString(getObject(root, parent), child);
    }

    private static String guessExtension(String mediaUrl, XhsMediaType mediaType) {
        if (!TextUtils.isEmpty(mediaUrl)) {
            String lowerUrl = mediaUrl.toLowerCase(Locale.US);
            int queryIndex = lowerUrl.indexOf('?');
            if (queryIndex >= 0) {
                lowerUrl = lowerUrl.substring(0, queryIndex);
            }
            int dotIndex = lowerUrl.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < lowerUrl.length() - 1) {
                String extension = lowerUrl.substring(dotIndex + 1);
                if (extension.length() <= 5) {
                    return extension;
                }
            }
        }
        return mediaType == XhsMediaType.VIDEO ? "mp4" : "jpg";
    }

    private static String buildPageId(String url) {
        String host = extractHost(url);
        String hostPart = TextUtils.isEmpty(host) ? "weixin" : host.replace('.', '_');
        int hash = TextUtils.isEmpty(url) ? 0 : Math.abs(url.hashCode());
        return hostPart + "_" + hash;
    }

    private static String extractHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String getQueryParam(String url, String key) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(key)) {
            return null;
        }
        try {
            String query = new URI(url).getRawQuery();
            if (TextUtils.isEmpty(query)) {
                return null;
            }
            for (String part : query.split("&")) {
                String[] pair = part.split("=", 2);
                if (pair.length == 0 || !key.equals(pair[0])) {
                    continue;
                }
                return pair.length > 1 ? URLDecoder.decode(pair[1], "UTF-8") : "";
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private static String firstUrl(ArrayList<String> urls) {
        return urls == null || urls.isEmpty() ? null : urls.get(0);
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String getString(JsonObject object, String key) {
        if (object == null || TextUtils.isEmpty(key) || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        try {
            return object.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static JsonObject getObject(JsonObject object, String key) {
        if (object == null || TextUtils.isEmpty(key) || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return getObject(object.get(key));
    }

    private static JsonObject getObject(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return null;
        }
        return element.getAsJsonObject();
    }

    private static JsonArray getArray(JsonObject object, String key) {
        if (object == null || TextUtils.isEmpty(key) || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        JsonElement element = object.get(key);
        return element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.US);
    }

    private static final class RuntimeSnapshot {
        private final String currentUrl;
        private final String documentTitle;
        private final JsonObject feedInfo;
        private final JsonObject authorInfo;
        private final JsonObject sceneInfo;
        private final JsonObject lastError;
        private final ArrayList<String> domVideos;
        private final ArrayList<String> domPosters;

        private RuntimeSnapshot(JsonObject root) {
            currentUrl = getString(root, "currentUrl");
            documentTitle = getString(root, "documentTitle");
            feedInfo = getObject(root, "feedInfo");
            authorInfo = getObject(root, "authorInfo");
            sceneInfo = getObject(root, "sceneInfo");
            lastError = getObject(root, "lastError");
            domVideos = readStringList(root, "domVideos");
            domPosters = readStringList(root, "domPosters");
        }

        private boolean hasMedia() {
            // 只在拿到视频主资源后结束，避免头像/logo/二维码等普通图片触发假阳性。
            return XhsNetworkPolicy.isAllowedMediaUrl(firstNonEmpty(
                    getNestedString(feedInfo, "h264VideoInfo", "videoUrl"),
                    getNestedString(feedInfo, "h265VideoInfo", "videoUrl"),
                    getString(feedInfo, "videoUrl"),
                    firstUrl(domVideos)
            ));
        }

        private boolean hasAccessError() {
            String errMsg = getString(lastError, "errMsg");
            return !TextUtils.isEmpty(errMsg)
                    && (errMsg.toLowerCase(Locale.US).contains("permission")
                    || errMsg.toLowerCase(Locale.US).contains("illegal"));
        }

        private ArrayList<String> readStringList(JsonObject root, String key) {
            ArrayList<String> values = new ArrayList<>();
            JsonArray array = getArray(root, key);
            if (array == null) {
                return values;
            }
            for (JsonElement element : array) {
                if (element == null || element.isJsonNull()) {
                    continue;
                }
                try {
                    String value = element.getAsString();
                    if (!TextUtils.isEmpty(value)) {
                        values.add(value);
                    }
                } catch (Exception ignored) {
                    // ignore malformed JS snapshot values
                }
            }
            return values;
        }
    }
}
