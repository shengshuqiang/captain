package com.shuqiang.captain;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import com.facebook.soloader.SoLoader;
public class CaptainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //    多进程
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName(this);
            if (!this.getPackageName().equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        SoLoader.init(this, false);
    }

    private String getProcessName(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }
}
