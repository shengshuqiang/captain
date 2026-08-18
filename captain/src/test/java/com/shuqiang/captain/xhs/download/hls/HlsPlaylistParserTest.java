package com.shuqiang.captain.xhs.download.hls;

import org.junit.Assert;
import org.junit.Test;

public class HlsPlaylistParserTest {
    private final HlsPlaylistParser parser = new HlsPlaylistParser();

    @Test
    public void parseMasterResolvesRelativeVariantsAndQuotedCodecs() throws Exception {
        String content = "#EXTM3U\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=854x480,CODECS=\"avc1.4d401f,mp4a.40.2\"\n"
                + "480/index.m3u8\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=4000000,RESOLUTION=1920x1080,CODECS=\"avc1.640028,mp4a.40.2\"\n"
                + "../1080/index.m3u8?token=short\n";

        HlsPlaylist playlist = parser.parse("https://cdn.example.com/path/master.m3u8", content);

        Assert.assertEquals(HlsPlaylist.Type.MASTER, playlist.getType());
        Assert.assertEquals(2, playlist.getVariants().size());
        Assert.assertEquals(1080, playlist.getVariants().get(1).getHeight());
        Assert.assertEquals("avc1.640028,mp4a.40.2", playlist.getVariants().get(1).getCodecs());
        Assert.assertEquals("https://cdn.example.com/1080/index.m3u8?token=short",
                playlist.getVariants().get(1).getUrl());
    }

    @Test
    public void parseVodCarriesAesKeyAndMediaSequence() throws Exception {
        String content = "#EXTM3U\n#EXT-X-MEDIA-SEQUENCE:7\n"
                + "#EXT-X-KEY:METHOD=AES-128,URI=\"../keys/key.bin\",IV=0x00000000000000000000000000000007\n"
                + "#EXTINF:4.004,\nseg-7.ts\n#EXT-X-KEY:METHOD=NONE\n"
                + "#EXTINF:5,\nseg-8.ts\n#EXT-X-ENDLIST\n";

        HlsPlaylist playlist = parser.parse("https://cdn.example.com/video/playlist.m3u8", content);

        Assert.assertEquals(HlsPlaylist.Type.MEDIA, playlist.getType());
        Assert.assertTrue(playlist.hasEndList());
        Assert.assertEquals(2, playlist.getSegments().size());
        Assert.assertEquals(7, playlist.getSegments().get(0).getSequence());
        Assert.assertEquals("https://cdn.example.com/keys/key.bin",
                playlist.getSegments().get(0).getKey().getUrl());
        Assert.assertNull(playlist.getSegments().get(1).getKey());
        Assert.assertEquals(9.004, playlist.getTotalDurationSec(), 0.001);
    }

    @Test
    public void parseSurfacesUnsupportedLayoutFlags() throws Exception {
        String content = "#EXTM3U\n#EXT-X-MAP:URI=\"init.mp4\"\n#EXT-X-BYTERANGE:100@0\n"
                + "#EXT-X-DISCONTINUITY\n#EXTINF:4,\nchunk.m4s\n#EXT-X-ENDLIST\n";

        HlsPlaylist playlist = parser.parse("https://cdn.example.com/video/index.m3u8", content);

        Assert.assertTrue(playlist.hasMap());
        Assert.assertTrue(playlist.hasByteRange());
        Assert.assertTrue(playlist.hasDiscontinuity());
    }

    @Test
    public void parseKeepsRepeatedKeyDeclarationsAsSeparateRotations() throws Exception {
        String content = "#EXTM3U\n#EXT-X-KEY:METHOD=AES-128,URI=\"key.bin\"\n"
                + "#EXTINF:4,\none.ts\n#EXT-X-KEY:METHOD=AES-128,URI=\"key.bin\"\n"
                + "#EXTINF:4,\ntwo.ts\n#EXT-X-ENDLIST\n";

        HlsPlaylist playlist = parser.parse("https://cdn.example.com/video/index.m3u8", content);

        Assert.assertNotSame(playlist.getSegments().get(0).getKey(), playlist.getSegments().get(1).getKey());
        Assert.assertEquals(playlist.getSegments().get(0).getKey().getUrl(),
                playlist.getSegments().get(1).getKey().getUrl());
    }
}
