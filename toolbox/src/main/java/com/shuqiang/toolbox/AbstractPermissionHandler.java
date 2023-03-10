package com.shuqiang.toolbox;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * 权限申请封装
 */
public abstract class AbstractPermissionHandler implements ActivityCompat.OnRequestPermissionsResultCallback {

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
        return "您未开启船长App的“" + tip + "”访问权限，请在系统设置中开启";
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
