package com.shuqiang.captain.xhs.download;

import android.content.Context;
import android.net.Uri;

import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.playback.cache.PreviewPlaybackCache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

import static org.junit.Assert.*;

/** 验证下载引擎真正读取的入口，避免只证明播放器自己的缓存可以命中。 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class DownloadMediaInputTest {
    private MockWebServer server;
    private DownloadHttpSession http;
    private PreviewPlaybackCache.Session session;
    private PreviewPlaybackCache.ExportLease lease;

    @Before public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        http = new DownloadHttpSession();
    }

    @After public void tearDown() throws Exception {
        if (lease != null) lease.close();
        if (session != null) session.close();
        http.close();
        server.shutdown();
    }

    private ResolvedMediaSource source(String url) {
        return new ResolvedMediaSource(url, XhsMediaTransport.DIRECT_FILE, "CaptainTest",
                null, 0, null, 0);
    }

    private void openCache(String url) throws Exception {
        Context context = RuntimeEnvironment.getApplication();
        session = PreviewPlaybackCache.open(context);
        assertTrue(session.register(new XhsMediaItem("video", XhsMediaType.VIDEO, url, null,
                0, 0, 0, "mp4", true), Collections.emptyMap()));
        assertTrue(PreviewPlaybackCache.reserveExport(session.getId(), "video"));
        lease = PreviewPlaybackCache.claimExport(session.getId(), "video");
        assertNotNull(lease);
    }

    @Test public void uncachedReadsKeepResponseTypeAndTaskHeaders() throws Exception {
        server.enqueue(new MockResponse().setHeader("Content-Type", "video/mp4").setBody("video-bytes"));
        String url = server.url("/video").toString();
        try (DownloadMediaInput input = DownloadMediaInput.open(http, source(url), url, null)) {
            assertEquals("video/mp4", input.contentType());
            assertEquals(11, input.length());
            assertEquals("video-bytes", new String(input.stream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        }
        RecordedRequest request = server.takeRequest();
        assertEquals("CaptainTest", request.getHeader("User-Agent"));
        assertEquals("zh-CN,zh;q=0.9", request.getHeader("Accept-Language"));
    }

    @Test public void exportFillsOnlyMissingRangeAndProducesAllOriginalBytes() throws Exception {
        byte[] bytes = new byte[32768];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) (i * 31);
        AtomicInteger transferred = new AtomicInteger();
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                String range = request.getHeader("Range");
                int start = 0;
                int end = bytes.length - 1;
                if (range != null) {
                    String[] bounds = range.substring("bytes=".length()).split("-", -1);
                    start = Integer.parseInt(bounds[0]);
                    if (!bounds[1].isEmpty()) end = Math.min(end, Integer.parseInt(bounds[1]));
                }
                int count = end - start + 1;
                transferred.addAndGet(count);
                return new MockResponse().setResponseCode(range == null ? 200 : 206)
                        .setHeader("Content-Type", "video/mp4")
                        .setHeader("Content-Range", "bytes " + start + "-" + end + "/" + bytes.length)
                        .setBody(new Buffer().write(bytes, start, count));
            }
        });
        String url = server.url("/video?signature=one").toString();
        openCache(url);
        DataSource prefix = lease.createDataSourceFactory(http.mediaDataSourceFactory(source(url))).createDataSource();
        prefix.open(new DataSpec.Builder().setUri(Uri.parse(url)).setLength(8192).build());
        byte[] buffer = new byte[8192];
        while (prefix.read(buffer, 0, buffer.length) != -1) { }
        prefix.close();
        // 模拟点击保存后立即返回，任务仍持有可读取的缓存。
        session.close();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (DownloadMediaInput input = DownloadMediaInput.open(http, source(url), url, lease)) {
            int count;
            while ((count = input.stream().read(buffer)) != -1) output.write(buffer, 0, count);
        }
        assertArrayEquals(bytes, output.toByteArray());
        assertEquals(bytes.length, transferred.get());
        assertEquals("bytes=0-8191", server.takeRequest().getHeader("Range"));
        assertEquals("bytes=8192-", server.takeRequest().getHeader("Range"));
    }

    @Test public void cachedHttpFailureKeepsExpiredSourceStatusContract() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(403).setBody("expired"));
        String url = server.url("/video").toString();
        openCache(url);
        try {
            DownloadMediaInput.open(http, source(url), url, lease);
            fail("Expected HTTP status failure");
        } catch (DownloadHttpSession.HttpStatusException error) {
            assertEquals(403, error.getStatusCode());
            assertTrue(error.indicatesExpiredSource());
        }
    }

    @Test public void uncachedHttpFailureRetainsStatus() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(410));
        String url = server.url("/video").toString();
        try {
            DownloadMediaInput.open(http, source(url), url, null);
            fail("Expected HTTP status failure");
        } catch (DownloadHttpSession.HttpStatusException error) {
            assertEquals(410, error.getStatusCode());
        }
    }

    @Test public void previewExportUsesTargetDomainCookieForRedirectAndUncachedKey() throws Exception {
        http.close();
        http = new DownloadHttpSession(null, true);
        android.webkit.CookieManager cookies = android.webkit.CookieManager.getInstance();
        cookies.setCookie("http://localhost", "page=origin");
        cookies.setCookie("http://127.0.0.1", "segment=target");
        String target = server.url("/segment").newBuilder().host("127.0.0.1").build().toString();
        String origin = server.url("/start").toString();
        server.enqueue(new MockResponse().setResponseCode(302).setHeader("Location", target));
        server.enqueue(new MockResponse().setBody("media-bytes"));
        openCache(origin);
        try (DownloadMediaInput input = DownloadMediaInput.open(http, source(origin), origin, lease)) {
            assertEquals("media-bytes", new String(input.stream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        }
        assertEquals("page=origin", server.takeRequest().getHeader("Cookie"));
        assertEquals("segment=target", server.takeRequest().getHeader("Cookie"));
        // 密钥不经过缓存，但仍必须携带目标域的 Cookie。
        server.enqueue(new MockResponse().setBody("key-only-in-memory"));
        try (DownloadMediaInput key = DownloadMediaInput.open(http, source(origin), target + "/key", null)) {
            assertTrue(key.stream().readAllBytes().length > 0);
        }
        assertEquals("segment=target", server.takeRequest().getHeader("Cookie"));
        cookies.removeAllCookies(null);
    }

    @Test public void ordinaryDownloadDoesNotAcquirePreviewCookies() throws Exception {
        android.webkit.CookieManager cookies = android.webkit.CookieManager.getInstance();
        cookies.setCookie("http://localhost", "preview=private");
        String url = server.url("/video").toString();
        server.enqueue(new MockResponse().setBody("video"));
        try (DownloadMediaInput input = DownloadMediaInput.open(http, source(url), url, null)) {
            assertTrue(input.stream().readAllBytes().length > 0);
        }
        assertNull(server.takeRequest().getHeader("Cookie"));
        cookies.removeAllCookies(null);
    }
}
