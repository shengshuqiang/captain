package com.shuqiang.captain;

import android.app.Application;
import android.os.Build;
import android.webkit.WebView;

import com.facebook.soloader.SoLoader;

public class CaptainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 多进程 WebView 必须在创建前绑定稳定的数据目录后缀。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            if (processName != null && !getPackageName().equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        SoLoader.init(this, false);
    }
}
