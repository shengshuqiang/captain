package com.shuqiang.captain.xhs.playback.cache;

import android.net.Uri;
import android.webkit.CookieManager;

import androidx.media3.common.C;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.cache.ContentMetadata;
import androidx.media3.datasource.okhttp.OkHttpDataSource;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

import static org.junit.Assert.*;

/** 使用真实 Media3 缓存与可控 Range 服务验证字节复用和引用所有权。 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class PreviewPlaybackCacheTest {
    private final List<PreviewPlaybackCache.Session> sessions = new ArrayList<>();
    private final List<PreviewPlaybackCache.ExportLease> leases = new ArrayList<>();
    private MockWebServer server;
    private byte[] video;

    @Before public void setUp() throws Exception {
        video = new byte[4096];
        for (int i = 0; i < video.length; i++) video[i] = (byte) (i % 127);
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().startsWith("/cross-domain")) {
                    return new MockResponse().setResponseCode(302).addHeader("Location",
                            server.url("/video").newBuilder().host("127.0.0.1").build());
                }
                if (request.getPath().startsWith("/redirect")) {
                    return new MockResponse().setResponseCode(302)
                            .addHeader("Location", server.url("/video?token=redirect-secret"));
                }
                String range = request.getHeader("Range");
                int from = 0;
                int to = video.length - 1;
                if (range != null) {
                    String[] bounds = range.substring("bytes=".length()).split("-", -1);
                    from = Integer.parseInt(bounds[0]);
                    if (!bounds[1].isEmpty()) to = Math.min(to, Integer.parseInt(bounds[1]));
                }
                MockResponse response = new MockResponse()
                        .setBody(new Buffer().write(Arrays.copyOfRange(video, from, to + 1)))
                        .addHeader("Accept-Ranges", "bytes");
                if (range != null) response.setResponseCode(206)
                        .addHeader("Content-Range", "bytes " + from + "-" + to + "/" + video.length);
                return response;
            }
        });
        server.start();
    }

    @After public void tearDown() throws Exception {
        for (PreviewPlaybackCache.ExportLease lease : leases) lease.close();
        for (PreviewPlaybackCache.Session session : sessions) session.close();
        for (PreviewPlaybackCache.Session session : sessions) awaitDeleted(session.getDirectory());
        server.shutdown();
    }

    @Test public void fullyPlayedVideoCanBeExportedWithoutAnotherNetworkRequest() throws Exception {
        String url = server.url("/video?signature=played-video").toString();
        PreviewPlaybackCache.Session session = session(url);
        assertArrayEquals(video, read(session.cachedFactory(upstream()), url, C.LENGTH_UNSET));
        assertEquals(1, server.getRequestCount());
        PreviewPlaybackCache.ExportLease lease = reserveAndClaim(session, "video");
        DataSource.Factory unavailableNetwork = () -> new DataSource() {
            @Override public void addTransferListener(androidx.media3.datasource.TransferListener listener) { }
            @Override public long open(DataSpec spec) throws java.io.IOException {
                throw new java.io.IOException("Played video must export entirely from cache");
            }
            @Override public int read(byte[] data, int offset, int length) throws java.io.IOException {
                throw new java.io.IOException("Unexpected network read after playback");
            }
            @Override public android.net.Uri getUri() { return null; }
            @Override public void close() { }
        };
        assertArrayEquals(video, read(lease.createDataSourceFactory(unavailableNetwork), url, C.LENGTH_UNSET));
        assertEquals(1, server.getRequestCount());
    }

    @Test public void cachedPrefixIsNotDownloadedAgainWhenSavingWholeVideo() throws Exception {
        String url = server.url("/video?signature=private-secret").toString();
        PreviewPlaybackCache.Session session = session(url);
        DataSource.Factory factory = session.cachedFactory(upstream());
        assertArrayEquals(Arrays.copyOf(video, 1024), read(factory, url, 1024));
        assertEquals("bytes=0-1023", server.takeRequest(1, TimeUnit.SECONDS).getHeader("Range"));
        PreviewPlaybackCache.ExportLease lease = reserveAndClaim(session, "video");
        assertArrayEquals(video, read(lease.createDataSourceFactory(upstream()), url, C.LENGTH_UNSET));
        assertEquals("bytes=1024-", server.takeRequest(1, TimeUnit.SECONDS).getHeader("Range"));
        assertEquals(2, server.getRequestCount());
        assertFalse(session.cache.getKeys().iterator().next().contains("signature"));
    }

    @Test public void reservationKeepsCacheAfterPageExitAndCanOnlyBeClaimedOnce() throws Exception {
        PreviewPlaybackCache.Session session = session(server.url("/video").toString());
        assertTrue(PreviewPlaybackCache.reserveExport(session.getId(), "video"));
        assertFalse(PreviewPlaybackCache.reserveExport(session.getId(), "video"));
        session.close();
        assertNull(PreviewPlaybackCache.claimExport(session.getId(), "different-item"));
        PreviewPlaybackCache.ExportLease lease = PreviewPlaybackCache.claimExport(session.getId(), "video");
        assertNotNull(lease);
        leases.add(lease);
        assertNull(PreviewPlaybackCache.claimExport(session.getId(), "video"));
        assertTrue(session.getDirectory().exists());
        assertArrayEquals(video, read(lease.createDataSourceFactory(upstream()),
                server.url("/video").toString(), C.LENGTH_UNSET));
        lease.close();
        lease.close();
        awaitDeleted(session.getDirectory());
    }

    @Test public void cancelledHandoffReleasesClosedPageAndUnknownItemNeverReserves() throws Exception {
        PreviewPlaybackCache.Session session = session(server.url("/video").toString());
        assertFalse(PreviewPlaybackCache.reserveExport(session.getId(), "unknown"));
        assertTrue(PreviewPlaybackCache.reserveExport(session.getId(), "video"));
        session.close();
        PreviewPlaybackCache.cancelExportReservation(session.getId());
        PreviewPlaybackCache.cancelExportReservation(session.getId());
        awaitDeleted(session.getDirectory());
    }

    @Test public void activeReaderKeepsFilesUntilDataSourceCloses() throws Exception {
        String url = server.url("/video").toString();
        PreviewPlaybackCache.Session session = session(url);
        DataSource source = session.cachedFactory(upstream()).createDataSource();
        source.open(new DataSpec(Uri.parse(url)));
        session.close();
        assertTrue(session.getDirectory().exists());
        source.close();
        awaitDeleted(session.getDirectory());
    }

    @Test public void sessionsAndSignedUrlsAreIsolated() throws Exception {
        String url = server.url("/video?token=one").toString();
        PreviewPlaybackCache.Session first = session(url);
        PreviewPlaybackCache.Session second = session(url);
        read(first.cachedFactory(upstream()), url, C.LENGTH_UNSET);
        read(second.cachedFactory(upstream()), url, C.LENGTH_UNSET);
        read(first.cachedFactory(upstream()), url.replace("token=one", "token=two"), C.LENGTH_UNSET);
        assertEquals(3, server.getRequestCount());
        assertNotEquals(PreviewPlaybackCache.cacheKey(first.getId(), url),
                PreviewPlaybackCache.cacheKey(second.getId(), url));
    }

    @Test public void redirectedSignedUrlDoesNotReachAnyCacheIndexFile() throws Exception {
        String url = server.url("/redirect?token=original-secret").toString();
        PreviewPlaybackCache.Session session = session(url);
        assertArrayEquals(video, read(session.cachedFactory(upstream()), url, C.LENGTH_UNSET));
        String key = PreviewPlaybackCache.cacheKey(session.getId(), url);
        assertNull(ContentMetadata.getRedirectedUri(session.cache.getContentMetadata(key)));
        assertEquals(video.length, ContentMetadata.getContentLength(session.cache.getContentMetadata(key)));
        assertNoSecrets(session.getDirectory());
    }

    @Test public void openingCleansOrphansAndPreservesOtherActiveSessions() throws Exception {
        File root = new File(RuntimeEnvironment.getApplication().getCacheDir(),
                PreviewPlaybackCache.CACHE_DIRECTORY);
        root.mkdirs();
        File orphan = new File(root, "previous-process-cache");
        assertTrue(orphan.mkdirs());
        Files.write(new File(orphan, "old-index").toPath(), new byte[] {1});
        PreviewPlaybackCache.Session active = session(server.url("/video").toString());
        PreviewPlaybackCache.Session next = session(server.url("/other").toString());
        awaitDeleted(orphan);
        assertTrue(active.getDirectory().exists());
        assertTrue(next.getDirectory().exists());
    }

    @Test public void invalidRegistrationDoesNotBreakOtherItemsOrAcceptCookieHeaders() throws Exception {
        String url = server.url("/video").toString();
        PreviewPlaybackCache.Session session = session(url);
        session.register(new XhsMediaItem("bad", XhsMediaType.VIDEO, "not-a-url", null,
                0, 0, 0, "mp4", false), Collections.emptyMap());
        assertFalse(PreviewPlaybackCache.reserveExport(session.getId(), "bad"));
        assertNotNull(PreviewPlaybackCache.playbackSource(url, session.getDirectory()));
        assertNull(PreviewPlaybackCache.playbackSource("https://unknown.invalid", session.getDirectory()));
        assertNull(PreviewNetworkSource.from(url, false, Collections.singletonMap("Bad\nHeader", "x")));
        assertNotNull(PreviewNetworkSource.from(url, false, Collections.singletonMap("cOoKiE", "secret")));
    }

    @Test public void hlsPlaylistAndDrmKeysBypassCacheWhileMediaIsCached() throws Exception {
        String url = server.url("/video?token=hls-secret").toString();
        PreviewPlaybackCache.Session session = session(url);
        read(() -> session.hlsDataSource(C.DATA_TYPE_MANIFEST, upstream()), url, C.LENGTH_UNSET);
        read(() -> session.hlsDataSource(C.DATA_TYPE_DRM, upstream()), url, C.LENGTH_UNSET);
        assertEquals(0L, session.cache.getCacheSpace());
        read(() -> session.hlsDataSource(C.DATA_TYPE_MEDIA_INITIALIZATION, upstream()), url, C.LENGTH_UNSET);
        assertEquals(video.length, session.cache.getCacheSpace());
        read(() -> session.hlsDataSource(C.DATA_TYPE_MEDIA, upstream()), url, C.LENGTH_UNSET);
        assertEquals(3, server.getRequestCount());
    }

    @Test public void redirectUsesDestinationCookieAndNeverCapturedCookieFromHeaders() throws Exception {
        String url = server.url("/cross-domain").toString();
        CookieManager manager = CookieManager.getInstance();
        manager.setCookie(url, "source=one; Path=/");
        manager.setCookie(server.url("/video").newBuilder().host("127.0.0.1").build().toString(),
                "destination=two; Path=/");
        PreviewNetworkSource network = PreviewNetworkSource.from(url, false,
                Collections.singletonMap("cOoKiE", "captured=never-send"));
        read(network.factory(PreviewNetworkSource.newClient()), url, C.LENGTH_UNSET);
        RecordedRequest original = server.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest redirected = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("source=one", original.getHeader("Cookie"));
        assertEquals("destination=two", redirected.getHeader("Cookie"));
    }

    @Test(timeout = 10000) public void savingDoesNotWaitForPausedPlaybackWriteLock() throws Exception {
        String url = server.url("/video").toString();
        PreviewPlaybackCache.Session session = session(url);
        DataSource pausedPlayback = session.cachedFactory(upstream()).createDataSource();
        pausedPlayback.open(new DataSpec(Uri.parse(url)));
        try {
            assertEquals(128, pausedPlayback.read(new byte[128], 0, 128));
            PreviewPlaybackCache.ExportLease lease = reserveAndClaim(session, "video");
            assertArrayEquals(video, read(lease.createDataSourceFactory(upstream()), url, C.LENGTH_UNSET));
            assertEquals(2, server.getRequestCount());
        } finally { pausedPlayback.close(); }
    }

    private PreviewPlaybackCache.Session session(String url) throws Exception {
        PreviewPlaybackCache.Session session = PreviewPlaybackCache.open(RuntimeEnvironment.getApplication());
        sessions.add(session);
        session.cache.getCacheSpace(); // 等待内部索引初始化，避免测试依赖线程调度。
        session.register(new XhsMediaItem("video", XhsMediaType.VIDEO, url, null,
                0, 0, 0, "mp4", false), Collections.emptyMap());
        return session;
    }
    private PreviewPlaybackCache.ExportLease reserveAndClaim(PreviewPlaybackCache.Session session, String item) {
        assertTrue(PreviewPlaybackCache.reserveExport(session.getId(), item));
        PreviewPlaybackCache.ExportLease lease = PreviewPlaybackCache.claimExport(session.getId(), item);
        assertNotNull(lease);
        leases.add(lease);
        return lease;
    }
    private DataSource.Factory upstream() { return new OkHttpDataSource.Factory(new OkHttpClient()); }
    private byte[] read(DataSource.Factory factory, String url, long length) throws Exception {
        DataSource source = factory.createDataSource();
        try {
            source.open(new DataSpec.Builder().setUri(url).setLength(length).build());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int count;
            while ((count = source.read(buffer, 0, buffer.length)) != C.RESULT_END_OF_INPUT) {
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        } finally { source.close(); }
    }
    private static void awaitDeleted(File directory) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (directory.exists() && System.nanoTime() < deadline) Thread.sleep(10);
        assertFalse("缓存没有按引用释放", directory.exists());
    }
    private static void assertNoSecrets(File directory) throws Exception {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) { assertNoSecrets(file); continue; }
            String data = new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
            assertFalse(data.contains("original-secret"));
            assertFalse(data.contains("redirect-secret"));
            assertFalse(data.contains("http://"));
        }
    }
}
