package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsRequestMode;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class XhsDownloadRequestTest {
    @Test
    public void sourceBackedHlsDoesNotSerializeShortLivedUrl() throws Exception {
        String signedUrl = "https://cdn.example.com/master.m3u8?token=do-not-persist";
        ArrayList<XhsMediaItem> items = new ArrayList<>();
        items.add(new XhsMediaItem("video", XhsMediaType.VIDEO, signedUrl,
                "https://img.example.com/cover.jpg", 1920, 1080, 100, "mp4", true,
                XhsMediaTransport.HLS_STREAM, "https://example.com/watch/1", XhsRequestMode.DESKTOP,
                "generic_static_hls", 1080));
        XhsParseResult parseResult = new XhsParseResult("note", "https://example.com/watch/1",
                "https://example.com/watch/1", "example", "video", null,
                "test", "manual", items);

        XhsDownloadRequest request = XhsDownloadRequest.from(parseResult);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(request);
        }
        String serialized = new String(bytes.toByteArray(), StandardCharsets.ISO_8859_1);

        Assert.assertNull(request.getItems().get(0).getDirectUrl());
        Assert.assertFalse(serialized.contains("do-not-persist"));
        Assert.assertFalse(serialized.contains("master.m3u8"));
        Assert.assertTrue(serialized.contains("https://example.com/watch/1"));
        Assert.assertTrue(serialized.contains("generic_static_hls"));
    }

    @Test
    public void ordinaryDirectFileKeepsItsDownloadUrl() {
        XhsMediaItem mediaItem = new XhsMediaItem("image", XhsMediaType.IMAGE,
                "https://cdn.example.com/image.jpg", null, 0, 0, 0, "jpg", true);

        XhsDownloadItem item = XhsDownloadItem.from(mediaItem);

        Assert.assertEquals("https://cdn.example.com/image.jpg", item.getDirectUrl());
    }

    @Test
    public void runtimeItemSerializesOnlyOpaqueHandles() throws Exception {
        String signedUrl = "https://cdn.example.com/master.m3u8?token=runtime-secret";
        String pageUrl = "https://example.com/challenge?session=page-secret";
        ArrayList<XhsMediaItem> items = new ArrayList<>();
        items.add(new XhsMediaItem("video", XhsMediaType.VIDEO, signedUrl, null,
                0, 0, 0, "mp4", true, XhsMediaTransport.HLS_STREAM, pageUrl,
                XhsRequestMode.WEBVIEW, null, 0, "opaque-session", "opaque-candidate"));
        XhsParseResult parseResult = new XhsParseResult("note", pageUrl, pageUrl,
                "example", "video", null, "manual webview", "manual", items);

        XhsDownloadRequest request = XhsDownloadRequest.from(parseResult);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(request);
        }
        String serialized = new String(bytes.toByteArray(), StandardCharsets.ISO_8859_1);

        Assert.assertEquals("opaque-session", request.getRuntimeSessionId());
        Assert.assertEquals("opaque-candidate", request.getItems().get(0).getRuntimeCandidateId());
        Assert.assertNull(request.getItems().get(0).getDirectUrl());
        Assert.assertNull(request.getItems().get(0).getSourcePageUrl());
        Assert.assertFalse(serialized.contains("runtime-secret"));
        Assert.assertFalse(serialized.contains("page-secret"));
    }
}
