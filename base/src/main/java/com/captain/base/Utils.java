package com.captain.base;

import android.os.Handler;

public class Utils {
    public static void postDelayed(Runnable runnable) {
        new Handler().postDelayed(runnable, 300);
    }
}
