package com.captain.base;
import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.List;

public final class PermissionUtils {
    // 申请相机权限的requestCode
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;
    // 申请外部存储权限的requestCode
    public static final int PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 0x00000013;
    // 隐私权限弹窗 SharedPreferences key
    public static final String GRANT_PRIVACY_PERMISSION_PREFS_KEY = "launch_privacy_dialog";


    private static final String TAG = "PrivacyDlgHelper";
    private static int privacyPermissionGrantState = -1;

    public static boolean hasGrantPrivacyPermission(Context context) {
        // 未初始化== -1 ，初始化后 如果已经显示过，值为1，未显示过 值为0
        if (privacyPermissionGrantState == -1) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean value = prefs.getBoolean(GRANT_PRIVACY_PERMISSION_PREFS_KEY, false);
                privacyPermissionGrantState = value ? 1 : 0;
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
        return privacyPermissionGrantState == 1;
    }

    static void grantPrivacyPermission(Context context) {
        try {
            privacyPermissionGrantState = 1;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putBoolean(GRANT_PRIVACY_PERMISSION_PREFS_KEY, true).commit();
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }

    public interface OnPrivacyAgreeListener {
        void agree();
        void disAgree();
    }

    public static void showPolicyDialog(FragmentActivity activity) {
        showPolicyDialog(activity, null);
    }
    public static void showPolicyDialog(FragmentActivity activity, OnPrivacyAgreeListener onPrivacyAgreeListener) {
        boolean showPrivacyDialog = !PermissionUtils.hasGrantPrivacyPermission(activity);
        if (showPrivacyDialog) {
            PrivacyPolicyDialog privacyDialog = new PrivacyPolicyDialog();
            privacyDialog.setOnDismissListener(dialog -> {
                dialog.dismiss();
                if (null != onPrivacyAgreeListener) {
                    onPrivacyAgreeListener.disAgree();
                }
            });
            privacyDialog.setOnArgeeListener(dialog -> {
                dialog.dismiss();
                if (null != onPrivacyAgreeListener) {
                    onPrivacyAgreeListener.agree();
                }
                grantPrivacyPermission(activity);
            });
            privacyDialog.show(activity.getSupportFragmentManager(), "PrivacyDialog");
        } else {
            if (null != onPrivacyAgreeListener) {
                onPrivacyAgreeListener.agree();
            }
        }
    }

    // 重启App的方法
    public static void restartApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            ComponentName componentName = intent.getComponent();
            Intent mainIntent = Intent.makeRestartActivityTask(componentName);
            context.startActivity(mainIntent);
            killProcess(context);
        }
    }

    // 杀死所有进程
    public static void killProcess(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> processList = mActivityManager.getRunningAppProcesses();
            if (processList != null) {
                for (ActivityManager.RunningAppProcessInfo process : processList) {
                    if (process.pid != android.os.Process.myPid() &&
                            process.processName.contains(context.getPackageName())) {
                        android.os.Process.killProcess(process.pid);
                    }
                }
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 相机权限封装
     */
    public static abstract class CameraPermissionHandler extends AbstractPermissionHandler {

        public CameraPermissionHandler(Activity activity) {
            super(activity,  Manifest.permission.CAMERA, PERMISSION_CAMERA_REQUEST_CODE);
        }

        @Override
        public void handlePermissionPermit() {
            openCamera();
        }

        public abstract void openCamera();
    }

    /**
     * 外部存储权限封装
     */
    public static abstract class ExternalStoragePermissionHandler extends AbstractPermissionHandler {

        public ExternalStoragePermissionHandler(Activity activity) {
            super(activity,  Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE);
        }

        @Override
        public void handlePermissionPermit() {
            onReadWriteFile();
        }

        public abstract void onReadWriteFile();
    }

    /**
     * 权限申请封装
     */
    public abstract static class AbstractPermissionHandler implements ActivityCompat.OnRequestPermissionsResultCallback {

        private Activity activity;
        private String permission;
        private int requestCode;
        // 弹窗是否已显示
        private boolean hasShowDialog = false;

        public AbstractPermissionHandler(Activity activity, String permission, int requestCode) {
            this.activity = activity;
            this.permission = permission;
            this.requestCode = requestCode;
        }

        /**
         * 检查存储权限
         */
        public void handleRequestPermissionAndWork() {
            int hasPermission = ContextCompat.checkSelfPermission(activity, permission);
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                // 权限已申请
                handlePermissionPermit();
            } else {
                // 没有权限，申请权限
                ActivityCompat.requestPermissions(activity, new String[]{ permission }, requestCode);
            }
        }

        /**
         * 处理权限申请回调
         */
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode == this.requestCode) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 允许权限，调起权限成功回调
                    handlePermissionPermit();
                } else {
                    if (hasShowDialog) {
                        return;
                    }
                    hasShowDialog = true;
                    // 拒绝权限，弹出提示框
                    AlertDialog.Builder dialog = (new AlertDialog.Builder(activity))
                            .setMessage(getPermissionMessage())
                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                                    activity.startActivity(intent);
                                }
                            }).setNegativeButton("暂不", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    onPermissionReject();
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                public void onCancel(DialogInterface dialog) {
                                    onPermissionReject();
                                }
                            }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    hasShowDialog = false;
                                }
                            });
                    if (!activity.isFinishing()) {
                        dialog.show();
                    }
                }
            }
        }

        private String getPermissionMessage() {
            String tip;
            switch (permission) {
                case Manifest.permission.CAMERA:
                    tip = "相机";
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    tip = "媒体和文件";
                    break;
                default:
                    tip = "未知";
                    break;
            }
            return "您未开启船长 App 的“" + tip + "”访问权限，请在系统设置中开启";
        }

        /**
         * 权限允许回调，执行已获取后权限操作
         */
        public abstract void handlePermissionPermit();

        /**
         * 拒绝默认关闭当前页面
         */
        protected void onPermissionReject() {
            activity.finish();
        }
    }
}
