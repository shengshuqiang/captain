package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsSaveItemResult;

import java.io.IOException;

/** 媒体下载策略接口，由传输方式选择实现。 */
public interface MediaDownloadEngine {
    boolean supports(XhsDownloadItem item);

    XhsSaveItemResult download(MediaDownloadContext context, XhsDownloadItem item) throws IOException;
}
