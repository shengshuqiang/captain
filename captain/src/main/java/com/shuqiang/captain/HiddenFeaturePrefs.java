package com.shuqiang.captain;

import android.content.Context;
import android.content.SharedPreferences;

// 统一管理隐藏功能开关，避免入口显隐只在当前页面内生效。
public final class HiddenFeaturePrefs {
    private static final String PREFS_NAME = "captain_hidden_features";
    private static final String KEY_HIDDEN_FEATURES_ENABLED = "hidden_features_enabled";

    private HiddenFeaturePrefs() {
    }

    public static boolean isHiddenFeaturesEnabled(Context context) {
        return getPreferences(context).getBoolean(KEY_HIDDEN_FEATURES_ENABLED, false);
    }

    public static boolean enableHiddenFeatures(Context context) {
        if (isHiddenFeaturesEnabled(context)) {
            return false;
        }
        return getPreferences(context).edit().putBoolean(KEY_HIDDEN_FEATURES_ENABLED, true).commit();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
