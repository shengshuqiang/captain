# 船长内嵌浏览器

本模块将 Fulguris 编译为船长的 Android 库，保留其多标签、会话、历史、书签、导航和页面生命周期。浏览器与船长处于同一安装包，资源选择、预览和保存复用船长的媒体模块。

## 来源与许可

- 上游：<https://github.com/Slion/Fulguris>
- 固定提交：`66d56d4501575324f09621a5babcbf4bc7641118`。
- 完整源码位于 `third_party/fulguris` Git 子模块；Preference 与 AppIntro 使用上游固定的子模块提交。
- 保留上游版权、关于页和 CPAL-1.0/MPL-2.0 许可文件。正式分发应按实际文件归属落实相关要求。

## 初始化与构建

在工程根目录执行：

```sh
git submodule update --init third_party/fulguris
git -C third_party/fulguris submodule update --init subs/Preference subs/AppIntro
./gradlew :captain:assembleDebug --max-workers=2
```

并存测试包使用独立包名 `com.shuqiang.captain.browser.test`：

```sh
./gradlew :captain:assembleDebug -PcaptainBrowserTest=true --max-workers=2
```

未指定该属性时，船长保持原包名。构建产物都位于 `captain/build/outputs/apk/debug/`，安装前务必核对 `output-metadata.json` 中的 `applicationId`。

## 接入边界

- `tools/prepare_fulguris.py` 在模块构建目录生成库化副本，校验固定提交；不修改上游子模块源码。
- 库化处理包括宿主 Application/Hilt 初始化、资源变体覆盖、启动入口和内置脚本读取。
- 普通浏览器作为首页工具在 Captain 当前任务内打开；退出只关闭浏览器页，返回原船长首页。隐私浏览的独立进程保持不变。
- `CaptainBrowserBridge` 在上游页面回调及资源菜单转发事件，保留浏览器原有网络、导航、权限和全屏处理。
- 资源按 WebView 文档隔离，页面导航和关闭时释放；播放器停止或播放结束不会清空资源。
- 普通文件原有下载功能继续由 Fulguris 承接；新增资源面板的媒体保存统一进入船长下载服务，不同时触发上游下载器。
- 隐私浏览仍使用上游独立进程；资源导出明确提示暂不支持，避免跨进程移交隐私凭据。
- 视频源仍可能受网站有效期和格式限制；播放缓存有容量限制。没有把 WebView 内部缓存作为可直接导出的完整视频文件。

## 验证

```sh
./gradlew :browser:testDebugUnitTest :captain:testDebugUnitTest \
  -PcaptainBrowserTest=true --tests 'com.shuqiang.captain.xhs.*' \
  --tests 'com.shuqiang.captain.browser.*' --max-workers=2
```

`tools/browser_acceptance_server.py` 可生成包含视频和音频的自有测试片，并通过 Cookie 校验提供网页、MP4 和 HLS：

```sh
python3 tools/browser_acceptance_server.py --root /tmp/captain-browser-media
```

真机验收结果见 `docs/fulguris-browser-acceptance-20261017.md`。测试范围、未验证项和最终包身份分开记录。
