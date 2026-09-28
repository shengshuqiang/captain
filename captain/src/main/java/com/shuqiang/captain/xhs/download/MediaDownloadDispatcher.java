package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.download.hls.HlsDownloadEngine;
import com.shuqiang.captain.xhs.model.XhsSaveItemResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** 按媒体传输方式选择下载引擎，Service 不感知 HLS 实现细节。 */
public final class MediaDownloadDispatcher {
    private final List<MediaDownloadEngine> engines;

    public MediaDownloadDispatcher() {
        DirectMediaDownloadEngine directEngine = new DirectMediaDownloadEngine();
        engines = Arrays.<MediaDownloadEngine>asList(directEngine, new HlsDownloadEngine(directEngine));
    }

    public XhsSaveItemResult download(MediaDownloadContext context, XhsDownloadItem item) throws IOException {
        for (MediaDownloadEngine engine : engines) {
            if (engine.supports(item)) {
                return engine.download(context, item);
            }
        }
        throw new IOException("没有支持该传输方式的下载引擎");
    }
}
