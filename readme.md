# 命令行
1. Android依赖信息：` ./gradlew captain:dependencies > log_dependencies.txt `
2. Release 打包：
   ```bash
   ./gradlew :captain:assembleRelease --console=plain
   ```
3. Release 安装到当前唯一已连接设备：
   ```bash
   adb install -r /Users/shengshuqiang/dream/captain/captain/build/outputs/apk/release/captain-release.apk
   ```
4. Release 安装到指定设备：
   ```bash
   adb -s <deviceSerial> install -r /Users/shengshuqiang/dream/captain/captain/build/outputs/apk/release/captain-release.apk
   ```

## Release 打包与安装

- 打包前提：本地需要存在 `key.properties`，且其中 `storeFile` 指向有效 keystore。
- 打包产物：`captain/build/outputs/apk/release/captain-release.apk`
- 查看已连接设备：
  ```bash
  adb devices -l
  ```

# 工具
1. Material Design 官方的icon:
   	1. https://m2.material.io/icons/
   	2. https://fonts.google.com/icons
