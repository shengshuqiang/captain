package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsRequestMode;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class RuntimeMediaSessionStoreTest {
    @Test public void longPlaybackRetainsTheOneShotDownloadSessionPastIdleTimeout() {
        java.util.concurrent.atomic.AtomicLong now = new java.util.concurrent.atomic.AtomicLong(0);
        RuntimeMediaSessionStore store = new RuntimeMediaSessionStore(1000, now::get);
        XhsMediaItem item = store.capture(buildResult(), "ua", "https://page.example.com/final", null)
                .getMediaItems().get(0);
        Assert.assertTrue(store.retainForPreview(item.getRuntimeSessionId()));
        now.set(60_000);
        Assert.assertEquals("ua", store.previewHeaders(item).get("User-Agent"));
        Assert.assertNotNull(store.claim(item.getRuntimeSessionId()));
        Assert.assertNull(store.claim(item.getRuntimeSessionId()));
        store.releasePreview(item.getRuntimeSessionId());
    }

    @Test public void releasePreviewRestoresBoundedIdleExpiryAndNeverRevivesClaimedSession() {
        java.util.concurrent.atomic.AtomicLong now = new java.util.concurrent.atomic.AtomicLong(0);
        RuntimeMediaSessionStore store = new RuntimeMediaSessionStore(1000, now::get);
        XhsMediaItem item = store.capture(buildResult(), "ua", "https://page.example.com/final", null)
                .getMediaItems().get(0);
        Assert.assertTrue(store.retainForPreview(item.getRuntimeSessionId()));
        now.set(60_000);
        store.releasePreview(item.getRuntimeSessionId());
        Assert.assertEquals(1, store.sizeForTest());
        now.set(61_001);
        Assert.assertEquals(0, store.sizeForTest());
        Assert.assertFalse(store.retainForPreview(item.getRuntimeSessionId()));
        Assert.assertNull(store.claim(item.getRuntimeSessionId()));
    }

    @Test
    public void captureProducesSingleUseSessionWithScopedCookies() {
        RuntimeMediaSessionStore store = new RuntimeMediaSessionStore(60_000L);
        XhsParseResult raw = buildResult();

        XhsParseResult registered = store.capture(raw, "captured-agent",
                "https://page.example.com/final", new RuntimeMediaSessionStore.CookieProvider() {
                    @Override
                    public String getCookie(String url) {
                        if (url.contains("page.example.com")) {
                            return "age=confirmed";
                        }
                        if (url.contains("cdn.example.com")) {
                            return "media=allowed";
                        }
                        return null;
                    }
                });

        XhsMediaItem item = registered.getMediaItems().get(0);
        RuntimeMediaSession session = store.claim(item.getRuntimeSessionId());

        Assert.assertNotNull(session);
        Assert.assertNull(store.claim(item.getRuntimeSessionId()));
        Assert.assertEquals("age=confirmed", session.getCookieHeader("https://page.example.com/other"));
        Assert.assertEquals("media=allowed", session.getCookieHeader("https://cdn.example.com/segment.ts"));
        Assert.assertNull(session.getCookieHeader("https://third.example.com/segment.ts"));
        Assert.assertEquals("captured-agent", session.getUserAgent());
        Assert.assertEquals("https://page.example.com/final", session.getPageReferer());
        Assert.assertNotNull(session.resolve(item.getRuntimeCandidateId()));
        session.close();
        Assert.assertNull(session.resolve(item.getRuntimeCandidateId()));
    }

    @Test
    public void discardRemovesUnclaimedSession() {
        RuntimeMediaSessionStore store = new RuntimeMediaSessionStore(60_000L);
        XhsParseResult registered = store.capture(buildResult(), "ua",
                "https://page.example.com/final", null);
        String sessionId = registered.getMediaItems().get(0).getRuntimeSessionId();

        store.discard(sessionId);

        Assert.assertNull(store.claim(sessionId));
        Assert.assertEquals(0, store.sizeForTest());
    }

    @Test
    public void cookieMergeLetsLiveCookieOverrideCapturedValue() {
        Assert.assertEquals("age=confirmed; media=new; fresh=yes",
                DownloadHttpSession.mergeCookies(
                        "age=confirmed; media=old", "media=new; fresh=yes"));
    }

    @Test
    public void previewUsesCapturedContextWithoutConsumingDownloadSession() {
        RuntimeMediaSessionStore store = new RuntimeMediaSessionStore(60_000L);
        XhsParseResult raw = buildResult();
        raw.getMediaItems().get(0).setDiscoveryInfo(1234L, true);
        XhsMediaItem item = store.capture(raw, "captured-agent", raw.getPageUrl(),
                url -> url.contains("cdn.example.com") ? "media=allowed" : "page=only")
                .getMediaItems().get(0);
        Assert.assertEquals("media=allowed", store.previewHeaders(item).get("Cookie"));
        Assert.assertEquals("captured-agent", store.previewHeaders(item).get("User-Agent"));
        Assert.assertEquals(1234L, item.getDiscoveredAtMs());
        Assert.assertTrue(item.isCurrentPlayback());
        Assert.assertNotNull(store.claim(item.getRuntimeSessionId()));
        Assert.assertThrows(IllegalStateException.class, () -> store.previewHeaders(item));
    }

    private XhsParseResult buildResult() {
        ArrayList<XhsMediaItem> items = new ArrayList<>();
        items.add(new XhsMediaItem("video", XhsMediaType.VIDEO,
                "https://cdn.example.com/master.m3u8?token=short", null,
                0, 0, 0, "mp4", true, XhsMediaTransport.HLS_STREAM,
                "https://page.example.com/final", XhsRequestMode.WEBVIEW, null, 0));
        return new XhsParseResult("note", "https://page.example.com/final",
                "https://page.example.com/final", "example", "video", null,
                "manual webview", "manual", items);
    }
}
