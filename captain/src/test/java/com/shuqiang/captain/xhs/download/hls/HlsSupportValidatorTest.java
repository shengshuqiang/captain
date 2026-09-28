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

    @Test
    public void validateMediaPlaylistAcceptsPlainFragmentedMp4Vod() throws Exception {
        HlsPlaylist playlist = parser.parse("https://cdn.example.com/vod.m3u8",
                "#EXTM3U\n#EXT-X-MAP:URI=\"init.mp4\"\n"
                        + "#EXTINF:2,\npart0.m4s\n#EXT-X-ENDLIST\n");

        validator.validateMediaPlaylist(playlist);
    }

    @Test
    public void validateMediaPlaylistRejectsEncryptedFragmentedMp4() throws Exception {
        HlsPlaylist playlist = parser.parse("https://cdn.example.com/vod.m3u8",
                "#EXTM3U\n#EXT-X-MAP:URI=\"init.mp4\"\n"
                        + "#EXT-X-KEY:METHOD=AES-128,URI=\"key.bin\"\n"
                        + "#EXTINF:2,\npart0.m4s\n#EXT-X-ENDLIST\n");

        try {
            validator.validateMediaPlaylist(playlist);
            Assert.fail("expected encrypted fMP4 rejection");
        } catch (IOException exception) {
            Assert.assertTrue(exception.getMessage().contains("加密的 fMP4"));
        }
    }

    @Test
    public void selectVariantAcceptsHevcAndVideoOnlyCodecs() throws Exception {
        HlsPlaylist hevc = parser.parse("https://cdn.example.com/master.m3u8",
                "#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=1000000,"
                        + "CODECS=\"hvc1.1.6.L93.B0,mp4a.40.2\"\nhevc.m3u8\n");
        HlsPlaylist silent = parser.parse("https://cdn.example.com/master.m3u8",
                "#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=500000,"
                        + "CODECS=\"avc1.42e01e\"\nsilent.m3u8\n");

        Assert.assertEquals("https://cdn.example.com/hevc.m3u8",
                validator.selectVariant(hevc, 0).getUrl());
        Assert.assertEquals("https://cdn.example.com/silent.m3u8",
                validator.selectVariant(silent, 0).getUrl());
    }
}
