package com.shuqiang.captain.xhs.download;

import java.io.IOException;

/** 用户主动取消或 Service 被终止时使用的可识别异常。 */
public final class DownloadCancelledException extends IOException {
    public DownloadCancelledException() {
        super("下载已取消");
    }
}
