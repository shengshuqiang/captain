package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.download.hls.HlsPlaylist;
import com.shuqiang.captain.xhs.download.hls.HlsPlaylistParser;
import com.shuqiang.captain.xhs.download.hls.HlsSupportValidator;
import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsParseResult;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PornhubMediaParserTest {
    @Test
    public void parseSelectsHighestHlsAndKeepsExactOgMp4AsFallback() {
        String html = "<html><head>"
                + "<link rel=\"canonical\" href=\"https://www.pornhub.com/view_video.php?viewkey=test123\"/>"
                + "<meta property=\"og:title\" content=\"Authorized fixture\"/>"
                + "<meta property=\"og:site_name\" content=\"Example video site\"/>"
                + "<meta property=\"og:image\" content=\"https://img.example.com/poster.jpg\"/>"
                + "<meta property=\"og:video\" content=\"https://cdn.example.com/video-240.mp4?token=short\"/>"
                + "<meta property=\"og:video:url\" content=\"https://www.example.com/embed/test123\"/>"
                + "<meta property=\"og:video:height\" content=\"240\"/>"
                + "<meta property=\"video:duration\" content=\"1989\"/>"
                + "</head><body><script>var flashvars_1={\"note\":\"] inside string\",\"mediaDefinitions\":["
                + "{\"format\":\"hls\",\"quality\":\"240\",\"videoUrl\":\"https://cdn.example.com/240/master.m3u8?t=1\"},"
                + "{\"format\":\"hls\",\"quality\":\"1080\",\"videoUrl\":\"https://cdn.example.com/1080/master.m3u8?t=2\"},"
                + "{\"format\":\"hls\",\"quality\":\"720\",\"videoUrl\":\"https://cdn.example.com/720/master.m3u8?t=3\"}"
                + "]};</script>"
                + "<video src=\"https://ads.example.com/recommendation.mp4\"></video>"
                + "</body></html>";

        XhsParseResult result = PornhubMediaParser.parse(html,
                "https://www.pornhub.com/view_video.php?viewkey=test123", "manual_input", "desktop");

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.getMediaCount());
        Assert.assertEquals(XhsMediaTransport.HLS_STREAM, result.getMediaItems().get(0).getTransport());
        Assert.assertEquals(1080, result.getMediaItems().get(0).getQualityHeight());
        Assert.assertTrue(result.getMediaItems().get(0).isSelected());
        Assert.assertEquals("https://cdn.example.com/1080/master.m3u8?t=2",
                result.getMediaItems().get(0).getMediaUrl());
        Assert.assertEquals(XhsMediaTransport.DIRECT_FILE, result.getMediaItems().get(1).getTransport());
        Assert.assertFalse(result.getMediaItems().get(1).isSelected());
        Assert.assertEquals("https://cdn.example.com/video-240.mp4?token=short",
                result.getMediaItems().get(1).getMediaUrl());
    }

    @Test
    public void parseRejectsSameMarkupOnUnrelatedHost() {
        String html = "<script>var x={\"mediaDefinitions\":[{\"format\":\"hls\","
                + "\"quality\":\"1080\",\"videoUrl\":\"https://cdn.example.com/master.m3u8\"}]};</script>";

        Assert.assertNull(PornhubMediaParser.parse(html, "https://example.com/watch/1", "manual", "desktop"));
    }

    @Test
    public void parseOptionalAuthorizedLivePageFixture() throws Exception {
        String fixturePath = authorizedFixturePath();
        String html = new String(Files.readAllBytes(Paths.get(fixturePath)), StandardCharsets.UTF_8);

        XhsParseResult result = PornhubMediaParser.parse(html,
                "https://www.pornhub.com/view_video.php?viewkey=661e74a2afc13", "manual", "desktop");

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.getMediaCount());
        Assert.assertEquals(XhsMediaTransport.HLS_STREAM, result.getMediaItems().get(0).getTransport());
        Assert.assertEquals(1080, result.getMediaItems().get(0).getQualityHeight());
        Assert.assertEquals(XhsMediaTransport.DIRECT_FILE, result.getMediaItems().get(1).getTransport());
        Assert.assertTrue(result.getMediaItems().get(1).getQualityHeight() > 0);
        Assert.assertTrue(result.getMediaItems().get(1).getQualityHeight() < 1080);
    }

    @Test
    public void validateOptionalAuthorizedLiveHlsManifestContract() throws Exception {
        String fixturePath = authorizedFixturePath();
        String pageUrl = "https://www.pornhub.com/view_video.php?viewkey=661e74a2afc13";
        String html = new String(Files.readAllBytes(Paths.get(fixturePath)), StandardCharsets.UTF_8);
        XhsParseResult result = PornhubMediaParser.parse(html, pageUrl, "manual", "desktop");
        Assert.assertNotNull(result);
        String playlistUrl = result.getMediaItems().get(0).getMediaUrl();
        HlsPlaylistParser playlistParser = new HlsPlaylistParser();
        HlsSupportValidator validator = new HlsSupportValidator();
        OkHttpClient client = new OkHttpClient();
        HlsPlaylist playlist = null;
        for (int depth = 0; depth < 3; depth++) {
            Request request = new Request.Builder().url(playlistUrl)
                    .header("User-Agent", XhsHttpClient.DESKTOP_USER_AGENT)
                    .header("Referer", pageUrl).build();
            try (Response response = client.newCall(request).execute()) {
                Assert.assertTrue(response.isSuccessful());
                Assert.assertNotNull(response.body());
                playlist = playlistParser.parse(playlistUrl, response.body().string());
            }
            if (playlist.getType() == HlsPlaylist.Type.MEDIA) {
                break;
            }
            playlistUrl = validator.selectVariant(playlist, 1080).getUrl();
        }
        Assert.assertNotNull(playlist);
        validator.validateMediaPlaylist(playlist);
        Assert.assertTrue(playlist.getSegments().size() > 1);
        Assert.assertTrue(playlist.getTotalDurationSec() > 0);
    }

    private String authorizedFixturePath() {
        String fixturePath = System.getProperty("captain.authorizedPageFixture");
        if (fixturePath == null || fixturePath.trim().isEmpty()) {
            fixturePath = System.getenv("CAPTAIN_AUTHORIZED_PAGE_FIXTURE");
        }
        Assume.assumeTrue(fixturePath != null && !fixturePath.trim().isEmpty());
        return fixturePath;
    }
}
