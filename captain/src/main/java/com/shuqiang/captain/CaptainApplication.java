package com.shuqiang.captain;

import android.app.Application;
import android.os.Build;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatDelegate;

import com.facebook.soloader.SoLoader;

@dagger.hilt.android.HiltAndroidApp
public class CaptainApplication extends fulguris.App {
    @Override
    public void onCreate() {
        super.onCreate();
        // 首版固定浅色资源；计时器昼夜由天气/时间状态单独驱动。
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // 多进程 WebView 必须在创建前绑定稳定的数据目录后缀。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !getIncognito()) {
            String processName = Application.getProcessName();
            if (processName != null && !getPackageName().equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        SoLoader.init(this, false);
        fulguris.integration.CaptainBrowserBridge.setListener(
                new com.shuqiang.captain.browser.CaptainBrowserResources());
    }
}
