package com.shuqiang.captain.xhs.model;

import java.io.Serializable;

/**
 * 记录媒体来源页面的获取方式，供下载开始时使用同一类请求上下文重新解析。
 */
public enum XhsRequestMode implements Serializable {
    AUTO,
    DESKTOP,
    MOBILE_RETRY,
    WEBVIEW
}
