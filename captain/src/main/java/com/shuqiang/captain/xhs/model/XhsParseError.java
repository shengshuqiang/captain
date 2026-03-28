package com.shuqiang.captain.xhs.model;

/**
 * 统一收敛解析与保存流程的错误码，避免页面侧散落字符串判断。
 */
public enum XhsParseError {
    INVALID_INPUT("未识别到有效网页链接，请重新复制分享文案或页面地址。"),
    SHORT_LINK_EXPIRED("短链已失效，请先在浏览器打开后复制完整页面链接。"),
    NOTE_UNAVAILABLE("当前网页暂时无法访问，请稍后重试。"),
    NO_MEDIA_FOUND("页面已打开，但没有提取到可保存的图片、视频或 PDF。"),
    NETWORK_ERROR("网络异常，请稍后重试。"),
    SAVE_FAILED("保存失败，请稍后重试。"),
    PARTIAL_SUCCESS("部分资源已保存，其余资源保存失败。");

    private final String userMessage;

    XhsParseError(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
