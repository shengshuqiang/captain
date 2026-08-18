package com.shuqiang.captain.xhs.download.hls;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class HlsSupportValidatorTest {
    private final HlsPlaylistParser parser = new HlsPlaylistParser();
    private final HlsSupportValidator validator = new HlsSupportValidator();

    @Test
    public void selectVariantDoesNotSilentlyDowngradeRequestedQuality() throws Exception {
        HlsPlaylist playlist = parser.parse("https://cdn.example.com/master.m3u8",
                "#EXTM3U\n#EXT-X-STREAM-INF:RESOLUTION=1280x720,CODECS=\"avc1.4d401f,mp4a.40.2\"\n"
                        + "720.m3u8\n");

        try {
            validator.selectVariant(playlist, 1080);
            Assert.fail("expected quality mismatch");
        } catch (IOException exception) {
            Assert.assertTrue(exception.getMessage().contains("不自动降级"));
        }
    }

    @Test
    public void validateMediaPlaylistRejectsLiveAndSampleAes() throws Exception {
        HlsPlaylist playlist = parser.parse("https://cdn.example.com/live.m3u8",
                "#EXTM3U\n#EXT-X-KEY:METHOD=SAMPLE-AES,URI=\"key.bin\"\n"
                        + "#EXTINF:4,\nsegment.ts\n");

        try {
            validator.validateMediaPlaylist(playlist);
            Assert.fail("expected unsupported live playlist");
        } catch (IOException exception) {
            Assert.assertTrue(exception.getMessage().contains("直播"));
        }
    }

    @Test
    public void validateMediaPlaylistAcceptsAes128IdentityVod() throws Exception {
        HlsPlaylist playlist = parser.parse("https://cdn.example.com/vod.m3u8",
                "#EXTM3U\n#EXT-X-KEY:METHOD=AES-128,URI=\"key.bin\",KEYFORMAT=\"identity\"\n"
                        + "#EXTINF:4,\nsegment.ts\n#EXT-X-ENDLIST\n");

        validator.validateMediaPlaylist(playlist);
    }
}
