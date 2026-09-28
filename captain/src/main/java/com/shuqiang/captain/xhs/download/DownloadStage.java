package com.shuqiang.captain.xhs.download;

/** 下载任务的单一阶段枚举，用于通知和日志，不携带媒体 URL。 */
public enum DownloadStage {
    RESOLVING("正在刷新媒体来源"),
    DOWNLOADING_PLAYLIST("正在读取 HLS 清单"),
    PROBING("正在检查系统转封装能力"),
    DOWNLOADING_SEGMENTS("正在下载视频分片"),
    DECRYPTING("正在解密视频分片"),
    REMUXING("正在无损转封装 MP4"),
    VERIFYING("正在校验视频轨道"),
    PUBLISHING("正在发布到系统相册"),
    DIRECT_DOWNLOAD("正在下载直链文件"),
    COMPLETED("保存完成");

    private final String message;

    DownloadStage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
