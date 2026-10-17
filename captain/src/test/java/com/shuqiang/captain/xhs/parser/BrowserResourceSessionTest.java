package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsParseResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Browser playback completion must not make previously discovered video disappear. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class BrowserResourceSessionTest {
    @Test public void endedAndRemovedVideoRemainsAvailableForSave() throws Exception {
        BrowserResourceSession session = new BrowserResourceSession("https://example.com/watch");
        session.observeRequest("https://cdn.example.com/movie.mp4");
        session.observeSnapshot("{\"title\":\"Movie\",\"playingUrls\":[\"https://cdn.example.com/movie.mp4\"],\"items\":[]}");
        assertTrue(session.snapshot().getMediaItems().get(0).isCurrentPlayback());
        session.observeSnapshot("{\"title\":\"Movie\",\"playingUrls\":[],\"items\":[]}");
        XhsParseResult ended = session.snapshot();
        assertEquals(1, ended.getMediaCount());
        assertEquals("https://cdn.example.com/movie.mp4", ended.getMediaItems().get(0).getMediaUrl());
        assertFalse(ended.getMediaItems().get(0).isCurrentPlayback());
    }

    @Test public void hlsManifestRemainsAfterBlobPlayerStops() throws Exception {
        BrowserResourceSession session = new BrowserResourceSession("https://example.com/watch");
        session.observeRequest("https://cdn.example.com/master.m3u8?token=temporary");
        session.observeSnapshot("{\"playingUrls\":[\"blob:https://example.com/player\"],\"items\":[]}");
        session.observeSnapshot("{\"playingUrls\":[],\"items\":[]}");
        assertEquals(1, session.snapshot().getMediaCount());
        assertEquals(com.shuqiang.captain.xhs.model.XhsMediaTransport.HLS_STREAM,
                session.snapshot().getMediaItems().get(0).getTransport());
    }

    @Test public void separateDocumentsNeverShareCandidates() {
        BrowserResourceSession first = new BrowserResourceSession("https://example.com/one");
        BrowserResourceSession second = new BrowserResourceSession("https://example.com/two");
        first.observeRequest("https://cdn.example.com/one.mp4");
        assertNull(second.snapshot());
        second.observeRequest("https://cdn.example.com/two.mp4");
        assertEquals("https://cdn.example.com/one.mp4", first.snapshot().getMediaItems().get(0).getMediaUrl());
        assertEquals("https://cdn.example.com/two.mp4", second.snapshot().getMediaItems().get(0).getMediaUrl());
    }
}
