package com.captain.base;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Utils {
    // 美团黄前景
    public static final int YELLOW_FG_COLOR = 0XFFFE02;
    // 蓝色背景
    public static final int BLUE_BG_COLOR = 0X4351AC;

    public static String getAppVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
//            int versionCode = info.versionCode;
            String versionName = info.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "未知";
    }
    public static void postDelayed(Runnable runnable) {
        new Handler().postDelayed(runnable, 300);
    }

    /**
     * 设置系统图标的颜色（黑/白）
     *
     * @param activity       activity
     * @param isSysIconWhite bool值
     */
    public static void setSysIconColor(Activity activity, boolean isSysIconWhite) {
        if (activity == null || activity.getWindow() == null) {
            return;
        }
        setSysIconColor(activity.getWindow(), isSysIconWhite);
    }

    public static void setSysIconColor(Window window, boolean isSysIconWhite) {
        if (isMarshmallowOnXiaomiOrMeizu() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || window == null) {
            return;
        }
        final View decorView = window.getDecorView();
        int flag = decorView.getSystemUiVisibility();
        if (isSysIconWhite) {
            flag &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            flag |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(flag);
    }

    /**
     * 设置通知栏颜色
     *
     * @param activity activity
     * @param color    颜色值
     */
    public static void setStatusBarColor(@Nullable Activity activity, @ColorInt int color) {
        if (activity == null || activity.getWindow() == null) {
            return;
        }
        setStatusBarColor(activity.getWindow(), color);
    }

    public static void setStatusBarColor(@Nullable Window window, @ColorInt int color) {
        if (isMarshmallowOnXiaomiOrMeizu() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || window == null) {
            return;
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(color);
    }

    /**
     * 将当前页面布满整个屏幕
     *
     * @param activity activity
     */
    public static void addFullScreenFlags(@Nullable final Activity activity) {
        if (activity == null || activity.getWindow() == null) {
            return;
        }
        addFullScreenFlags(activity.getWindow());
    }

    public static void addFullScreenFlags(@Nullable final Window window) {
        if (isMarshmallowOnXiaomiOrMeizu() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || window == null) {
            return;
        }
        View decorView = window.getDecorView();
        int flag = decorView.getSystemUiVisibility();
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(flag);
    }

    /**
     * 去除将当前页面布满整个屏幕的属性
     *
     * @param activity activity
     */
    public static void clearFullScreenFlags(Activity activity) {
        if (isMarshmallowOnXiaomiOrMeizu() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || activity == null || activity.getWindow() == null) {
            return;
        }
        final View decorView = activity.getWindow().getDecorView();
        int flag = decorView.getSystemUiVisibility();
        flag &= ~(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        decorView.setSystemUiVisibility(flag);
    }

    public static boolean isMarshmallowOnXiaomiOrMeizu() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.M && (
                android.os.Build.BRAND.equals("Xiaomi") || android.os.Build.BRAND.equals("Meizu"));
    }


    /**
     * 获取状态栏的高度
     */
    public static int getStatusBarHeight(@NonNull final Context context) {
        final Resources resources = context.getResources();
        int resourceId =
                resources.getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    /**
     * 判断是否有网络连接
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断 WIFI 网络是否可用
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断 MOBILE 网络是否可用
     * @param context
     * @return
     */
    public boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * dp2px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
