# 船长浏览器接入方案：Fulguris 与资源嗅探下载

核对日期：2026-10-17（Asia/Shanghai）。

## 结论与当前状态

采用 Fulguris 的完整浏览器能力，在船长中接入资源嗅探、资源清单和下载保存。保持船长宿主及已有业务，浏览器内核使用系统 WebView。该路线可在 macOS 开发，无需建立 Chromium 的 Linux 全量构建环境。

用户已将 Brave/Chromium 路线调整为接入方便的方案。Brave 路线停止；此前对 Brave 的源码调查不代表已拉取仓库、构建或集成。

当前已将固定版本 Fulguris 库化接入船长，完成被动资源观察、普通标签资源面板、现有预览与下载链路接线；原版基线与船长并存测试包均已构建成功。普通标签下的 MP4 已通过网页内播完后保存、船长预览播完后保存两条真机路径。其余格式和边界按验收报告分别记录，不由 MP4 结果推定通过。

实现入口见 `browser/README.md`，实际验证见 `docs/fulguris-browser-acceptance-20261017.md`。本方案与设备回执记录实施时快照；提交及推送状态以 Git 历史为准。案牍等预存改动不属于本次浏览器提交。

## 固定依据

- 船长仓库：`/Users/shengshuqiang/dream/captain`。
- 分支：`codex/captain-web-media-save`；HEAD：`ffafb3537abb3c29e896a4a24db0be7e9fa10dc7`。
- 工作区已有语音、案牍联动、Manifest、入口及备份相关改动，必须保留；不把这些改动归入浏览器交付。
- 船长配置：Java/传统 View，AGP 8.10.1，Gradle 8.11.1，compileSdk 36，minSdk 23，targetSdk 29；GSYVideoPlayer 14.0.0 与 Media3 1.10.1。
- Fulguris 调查提交：`66d56d4501575324f09621a5babcbf4bc7641118`。这只是已检查的源码快照，不代表已验证稳定构建。
- Fulguris 配置：传统 View/DataBinding，Java/Kotlin/Hilt，AGP 8.13.1，Gradle 8.14.3，compileSdk 36，minSdk 21，Java 17；有 Preference、AppIntro 等子模块。
- 本机已有 Java 17、Android SDK 36、Gradle 8.14.3 缓存、Kotlin 2.2.10 缓存；未命中 AGP 8.13.1 缓存。缓存存在不代表整个工程依赖已齐全。
- 本机空闲磁盘约 15 GiB；独立构建前应再次核对空间，不能无上限下载或自动清理用户文件。

## 可定制入口及职责

| 能力 | 已检查入口 | 拟接入方式 | 边界 |
| --- | --- | --- | --- |
| 浏览器资源按钮 | Fulguris `activity/WebBrowserActivity.kt` | 在现有菜单/页面操作入口展示当前标签的资源列表 | 保留浏览器导航、权限、全屏播放和返回处理 |
| 请求与页面观察 | Fulguris `view/WebPageClient.kt`、`view/WebPageChromeClient.kt` | 在现有回调中通知资源观察层；页面扫描补充 DOM 媒体信息 | 请求观察不是完整响应抓包；不承诺可见所有响应头或响应体 |
| 标签隔离 | Fulguris `browser/TabsManager.kt`、`browser/TabModel.kt`、`view/WebPageTab.kt` | 资源以标签 ID、隐私模式及页面代次区分；导航/关闭时清理 | 浏览器已有会话 owner 不重复建立；普通与隐私模式不串资源 |
| 普通下载 | Fulguris `download/LightningDownloadListener.kt` | 接入统一任务入口，明确上游普通文件与船长媒体任务的分工 | 不同时调用两个下载器，不生成重复通知和历史 |
| 媒体发现 | 船长 `xhs/parser/WebViewResourceSniffer.java`、`WebResourceMediaCollector.java`、`WebResourceDomSnapshot.java` | 把可复用的候选识别、去重和 DOM 采集改为被动观察接口 | 旧嗅探器直接设置 WebViewClient/WebChromeClient，不能原样挂到浏览器页面 |
| 下载会话 | 船长 `xhs/download/RuntimeMediaSessionStore.java`、`RuntimeMediaSession.java`、`DownloadHttpSession.java` | 下载开始时登记独立的会话与资源快照，按来源获取所需 Cookie 和请求信息 | 当前会话是进程内短时、一次领取，不能称为持久化或重启恢复 |
| 下载与预览 | 船长 `xhs/download/`、`xhs/playback/`、`xhs/storage/` | 复用现有协调器、直链/HLS 引擎、播放和保存能力 | GSY/Media3 能播放，不代表所有协议均能下载合并 |

Fulguris 文件均位于 `app/src/main/java/fulguris/`；船长上述路径均位于 `captain/src/main/java/com/shuqiang/captain/`。文件职责与接口仍须在具体接入前按真实调用链细化。

## 原功能、目标差异、最小改动与验收

| 用户场景 | 当前功能 | 改后目标 | 最小改动 | 验收判定 |
| --- | --- | --- | --- | --- |
| 浏览多个网页 | 下载页内手动浏览与资源清单 | 成熟浏览器、多标签、导航、历史和书签 | 引入 Fulguris 浏览模块和必要宿主适配 | 打开/切换/关闭/恢复标签，登录、返回和全屏播放正常 |
| 发现网页资源 | 当前页面持续发现候选 | 每个标签展示各自图片、视频、PDF等候选 | 被动观察层、按标签管理候选、资源入口 | 两个标签显示各自资源，跳转后旧候选不混入；分片和重复候选受控 |
| 选择资源并保存 | 预览、选择、会话登记、前台 Service 保存 | 浏览器候选进入现有保存流程 | 会话快照与统一任务路由 | MP4/图片/PDF 文件可打开、保存位置可见；登录和重定向资源携带正确会话；无重复任务 |
| 取消及离开页面 | 已有下载状态和取消入口 | 继续下载或明确取消，行为按既有合同保留 | 生命周期与任务状态接线 | 关闭标签/返回/后台不误取消已开始任务，主动取消后不发布不完整文件 |
| HLS及复杂视频 | 直链和受限 HLS；已有 AES-128 处理 | 先保持已支持格式，再按协议扩展 | 复用现有引擎，必要协议独立实现 | 检查最终音视频轨、时长、可播放与失败提示，不以嗅探到地址代替下载成功 |

候选发现不等于可保存。`blob:`/MSE 需要识别背后的媒体请求或清单，不能直接交给普通下载器。DRM 不属于普通资源下载支持范围。

现有 `MediaDownloadDispatcher` 只有直链与 HLS 引擎。`HlsSupportValidator` 明确限制独立音轨、直播、Byte Range、跳轨等。DASH 音视频分离与合并属于后续能力，不因接入 Fulguris 自动获得。

## 分阶段实施

1. **上游独立基线（已通过构建）**：在独立目录锁定 Fulguris 提交与子模块，遵循其 AGENTS.md，使用 `python scripts/tools/build.py` 构建默认 `slionsFullAgentDebug` 并存测试包。原版包未替换船长；临时副本已清理并保留校验回执。
2. **船长浏览模块**：在保留当前宿主、包名和签名的前提下评估 Fulguris 的 Activity、Application/Hilt、资源和子模块依赖，形成必要的构建对齐改动。完整应用源码不是现成 AAR；独立测试包成功不代表同包接入成功。
3. **最小资源闭环**：当前标签资源观察 → 资源清单 → 会话登记 → MP4/图片/PDF 保存。先证明浏览器原功能不受影响。
4. **现有媒体能力接入**：接入播放、支持范围内的 HLS、取消和保存结果；按来源隔离 Cookie，处理重定向和短效链接。
5. **协议扩展**：按真实资源需求分别增加 DASH、HLS 独立音轨等；直播、断点恢复与进程重启恢复单独定义，不作为首轮默认承诺。

## 范围与决策点

- 允许范围：浏览器源码模块、必要宿主/资源/依赖接线、候选观察、资源列表、会话与下载路由、直接相关验证。
- 保护范围：船长语音、案牍联动等其他业务及现有未提交改动；不更换正式包名、签名，不清理或迁移现有数据，不默认提升最低 Android 版本。
- 上游最低版本兼容不等于接入后编译必然兼容；需验证 Kotlin、Hilt、Gradle、资源和旧依赖冲突。若需要扩大升级范围，先给出具体影响。
- [Fulguris 许可证](https://github.com/Slion/Fulguris/blob/66d56d4501575324f09621a5babcbf4bc7641118/LICENSE) 为 CPAL-1.0/MPL-2.0 混合许可。复制源码和正式分发前需按实际文件归属落实许可证、来源及署名等要求；当前未确定船长的分发许可策略。
- 隐私标签使用上游独立进程，首版明确提示暂不支持资源导出；普通与隐私凭据不跨进程共享。
- 预览页持有下载会话，避免长视频播放超过原 15 分钟空闲超时后无法保存；退出预览后恢复普通清理规则。
- 已完成和未完成项以验收报告为准；未验证项不得记为通过。

## 上游证据

- [Fulguris 源码快照](https://github.com/Slion/Fulguris/tree/66d56d4501575324f09621a5babcbf4bc7641118)
- [应用构建配置](https://github.com/Slion/Fulguris/blob/66d56d4501575324f09621a5babcbf4bc7641118/app/build.gradle)
- [页面与回调接线](https://github.com/Slion/Fulguris/blob/66d56d4501575324f09621a5babcbf4bc7641118/app/src/main/java/fulguris/view/WebPageTab.kt)
- [页面请求处理](https://github.com/Slion/Fulguris/blob/66d56d4501575324f09621a5babcbf4bc7641118/app/src/main/java/fulguris/view/WebPageClient.kt)
- [标签管理](https://github.com/Slion/Fulguris/blob/66d56d4501575324f09621a5babcbf4bc7641118/app/src/main/java/fulguris/browser/TabsManager.kt)
- [下载入口](https://github.com/Slion/Fulguris/blob/66d56d4501575324f09621a5babcbf4bc7641118/app/src/main/java/fulguris/download/LightningDownloadListener.kt)

这些链接用于定位上游源码；构建、设备运行及保存文件证据见独立验收报告，不能互相替代。
