#!/usr/bin/env python3
"""Prepare a library copy of pinned upstream sources without editing the submodule."""
from pathlib import Path
import shutil
import json
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

UPSTREAM_COMMIT = "66d56d4501575324f09621a5babcbf4bc7641118"
ANDROID = "{http://schemas.android.com/apk/res/android}"
BRIDGE = "fulguris.integration.CaptainBrowserBridge"


def replace_once(path, old, new):
    """Fail on upstream drift instead of silently producing an unhooked browser."""
    text = path.read_text()
    if text.count(old) != 1:
        raise RuntimeError(f"Expected one integration anchor in {path.name}: {old[:90]}")
    path.write_text(text.replace(old, new, 1))


def prepare(upstream, output):
    revision = subprocess.check_output(
        ["git", "-C", str(upstream), "rev-parse", "HEAD"], text=True).strip()
    if revision != UPSTREAM_COMMIT:
        raise RuntimeError("Fulguris revision changed; review the integration before building")
    app = upstream / "app"
    output.mkdir(parents=True, exist_ok=True)
    java = output / "java"
    res = output / "res"
    # Only this task's generated source/resource folders are replaced.
    for folder in [java, res]:
        if folder.exists():
            shutil.rmtree(folder)
    shutil.copytree(app / "src/main/java", java)
    shutil.copytree(app / "src/fdroid/java", java, dirs_exist_ok=True)
    shutil.copytree(app / "src/main/res", res)
    # Reproduce app flavor overlay priority inside one library resource source set.
    for flavor in ["slions", "fdroid"]:
        for source in (app / f"src/{flavor}/res").rglob("*"):
            if not source.is_file():
                continue
            target = res / source.relative_to(app / f"src/{flavor}/res")
            target.parent.mkdir(parents=True, exist_ok=True)
            if target.exists() and source.parent.name.startswith("values"):
                base_tree = ET.parse(target)
                base = base_tree.getroot()
                for entry in ET.parse(source).getroot():
                    for old in list(base):
                        if old.tag == entry.tag and old.get("name") == entry.get("name"):
                            base.remove(old)
                    base.append(entry)
                base_tree.write(target, encoding="utf-8", xml_declaration=True)
            else:
                shutil.copyfile(source, target)

    replace_once(java / "fulguris/App.kt", "@HiltAndroidApp\nclass App",
                 "// CaptainApplication owns the Hilt application component.\nopen class App")
    replace_once(java / "fulguris/App.kt", "    override fun attachBaseContext(base: Context) {",
                 "    override fun attachBaseContext(base: Context) {\n"
                 "        // Host Hilt injection runs before this base Application.onCreate.\n"
                 "        app = this")
    replace_once(java / "fulguris/App.kt",
                 "val component = ComponentName(context.packageName, SplashActivity::class.java.name)",
                 "val component = context.packageManager.getLaunchIntentForPackage(context.packageName)?.component ?: return")
    client = java / "fulguris/view/WebPageClient.kt"
    replace_once(client,
                 "        val wasBlocked = response != null",
                 "        val wasBlocked = response != null\n"
                 f"        if (!wasBlocked) {BRIDGE}.request(view, request)")
    replace_once(client,
                 "    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {",
                 "    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {\n"
                 f"        {BRIDGE}.pageStarted(view, url)")
    replace_once(client, "    override fun onPageFinished(view: WebView, url: String) {",
                 "    override fun onPageFinished(view: WebView, url: String) {\n"
                 f"        {BRIDGE}.pageFinished(view)")
    replace_once(java / "fulguris/view/WebPageTab.kt", "    private fun destroyWebView() {",
                 "    private fun destroyWebView() {\n"
                 f"        {BRIDGE}.destroyed(webView)")
    activity = java / "fulguris/activity/WebBrowserActivity.kt"
    # A normal browser is a Captain tool page; closing it must preserve the host task.
    # Incognito still owns its separate process/task and retains upstream cleanup.
    replace_once(activity, "        finishAndRemoveTask()",
                 "        if (isIncognito()) finishAndRemoveTask() else finish()")
    replace_once(activity, "        exitProcess(1)",
                 "        if (isIncognito()) exitProcess(1)")
    replace_once(activity, "        sessionsManager.clearAllSavedState()",
                 "        // Captain stays alive: release tabs and stop onStop from saving them again.\n"
                 "        if (!isIncognito()) tabsManager.shutdown()\n"
                 "        sessionsManager.clearAllSavedState()")
    anchor = "            onMenuItemClicked(iBinding.menuItemDownloads)"
    text = activity.read_text()
    if text.count(anchor) != 1:
        raise RuntimeError("Upstream downloads menu integration anchor changed")
    activity.write_text(text.replace(anchor,
        "            onMenuItemClicked(iBinding.menuItemCaptainResources) {\n"
        "                dismiss()\n"
        f"                {BRIDGE}.openResources(this@WebBrowserActivity, "
        "tabsManager.currentTab?.webView, isIncognito())\n"
        "            }\n" + anchor, 1))
    menu = res / "layout/menu_custom.xml"
    anchor = '                    <TextView\n                        android:id="@+id/menuItemDownloads"'
    item = ('                    <TextView\n'
            '                        android:id="@+id/menuItemCaptainResources"\n'
            '                        style="@style/BrowserTextMenuItem"\n'
            '                        app:drawableStartCompat="@drawable/ic_download_outline"\n'
            '                        android:text="@string/captain_browser_resources" />\n\n')
    replace_once(menu, anchor, item + anchor)

    # Preserve the host's launcher, Application and backup/network policy.
    ET.register_namespace("android", ANDROID[1:-1])
    ET.register_namespace("tools", "http://schemas.android.com/tools")
    tree = ET.parse(app / "src/main/AndroidManifest.xml")
    application = tree.getroot().find("application")
    application.attrib.clear()
    for activity in list(application.findall("activity")):
        for intent in list(activity.findall("intent-filter")):
            activity.remove(intent)
        activity.set(ANDROID + "exported", "false")
        if activity.get(ANDROID + "name") == "fulguris.activity.MainActivity":
            # Avoid the upstream standalone browser becoming the root of a new task.
            activity.set(ANDROID + "launchMode", "standard")
            for attribute in ["alwaysRetainTaskState", "clearTaskOnLaunch"]:
                activity.attrib.pop(ANDROID + attribute, None)
    tree.write(output / "AndroidManifest.xml", encoding="utf-8", xml_declaration=True)

    # Keep the upstream locale switcher functional without copying its app build plugin.
    locales = ["en"] + sorted(p.name[7:] for p in res.glob("values-*")
                               if (p / "strings.xml").exists())
    locale = java / "fulguris/locale/LocaleList.java"
    locale.parent.mkdir(parents=True, exist_ok=True)
    quoted = ",".join('"' + value + '"' for value in locales)
    locale.write_text("package fulguris.locale;\n"
                      "/** Upstream resource locales for the embedded browser. */\n"
                      "public final class LocaleList {\n"
                      " public static final java.util.List<String> BUNDLED_LOCALES = "
                      f"java.util.Collections.unmodifiableList(java.util.Arrays.asList({quoted}));\n"
                      "}\n")
    # The upstream app plugin computes incorrect keys for external library file paths.
    # Generate the same pure-string reader using the unchanged annotation-relative keys.
    builtin_paths = re.findall(r'"(src/main/(?:js|html)/[^\"]+)"', (app / "build.gradle").read_text())
    reader = java / "com/anthonycr/mezzanine/MezzanineReader.kt"
    reader.parent.mkdir(parents=True, exist_ok=True)
    entries = []
    for relative in dict.fromkeys(builtin_paths):
        literal = json.dumps((app / relative).read_text(), ensure_ascii=False).replace("$", "\\$")
        entries.append(f'  "{relative}" -> {literal}')
    if not entries:
        raise RuntimeError("No upstream browser builtins found")
    reader.write_text("package com.anthonycr.mezzanine\n"
                      "/** Builtins copied from the pinned Fulguris sources, retaining relative keys. */\n"
                      "fun readFromMezzanine(path: String): String = when(path) {\n"
                      + "\n".join(entries) + '\n  else -> error("Unsupported builtin path")\n}\n')
    print(f"Prepared Fulguris {revision[:12]} with Captain resource hooks")


if __name__ == "__main__":
    prepare(Path(sys.argv[1]).resolve(), Path(sys.argv[2]).resolve())
