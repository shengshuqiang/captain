package com.shuqiang.captain;

import android.app.Application;
import com.facebook.soloader.SoLoader;
public class CaptainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, false);
    }
}
