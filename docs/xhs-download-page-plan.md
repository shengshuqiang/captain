# 船长 App 小红书公开笔记下载页

## 目标

- 支持用户粘贴小红书公开分享文案或链接，解析视频/图片资源后再自行勾选保存。
- 仅支持公开网页内容，不依赖登录态，不承诺去水印，不绕过校验或风控。
- 保持现工程技术栈：Java、传统 View、Activity。

## 实现摘要

- 首页九宫格新增“小红书下载”入口。
- 新增 `XhsDownloadActivity` 承接普通启动和 `ACTION_SEND text/plain` 分享入口。
- 解析链路为：分享文案抽取链接 -> 短链解析 -> HTML 抓取 -> `og:*`/`__INITIAL_STATE__` 解析。
- 保存链路为：应用内串行下载 -> `MediaStore` 或公共目录写入 -> 系统相册可见。
- 下载任务交给 `XhsDownloadService`，避免页面退出后中断。

## 关键约束

- 只接受 `xiaohongshu.com`、`xhslink.com`、`xhscdn.com`、`xhscdn.net` 白名单域名。
- Android 10 及以上使用 `MediaStore`，Android 9 及以下复用存储权限后写公共目录。
- 页面状态固定为 `IDLE / PARSING / PARSE_SUCCESS / PARSE_FAILED / SAVING / SAVE_PARTIAL_SUCCESS / SAVE_SUCCESS`。

## 测试重点

- 分享文案、直链、短链、无效输入解析。
- 视频笔记与图文笔记 HTML 解析。
- 重复保存跳过、保存失败回滚、保存成功后系统相册可见。
