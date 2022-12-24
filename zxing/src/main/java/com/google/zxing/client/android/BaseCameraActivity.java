package com.google.zxing.client.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * 相机权限封装
 */
public abstract class BaseCameraActivity extends Activity {

    // 申请相机权限的requestCode
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;
    // 弹窗是否已显示
    private boolean hasShowDialog = false;

    /**
     * 检查权限并拍照。
     * 调用相机前先检查权限。
     */
    protected boolean checkPermissionAndCamera() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.CAMERA);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //有调起相机拍照。
            return true;
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
            return false;
        }
    }

    /**
     * 处理权限申请的回调。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，有调起相机拍照。
                openCamera();
            } else {
                if (hasShowDialog) {
                    return;
                }
                hasShowDialog = true;
//                //拒绝权限，弹出提示框。
//                Toast.makeText(this,"拍照权限被拒绝",Toast.LENGTH_LONG).show();
                AlertDialog.Builder dialog = (new AlertDialog.Builder(this))
                        .setMessage("您未开启船长的相机服务，请在系统设置中开启")
                        .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                }).setNegativeButton("暂不", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        hasShowDialog = false;
                    }
                });
                if (!isFinishing()) {
                    dialog.show();
                }
            }
        }
    }

    public abstract void openCamera();
}