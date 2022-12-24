package com.google.zxing.client.android;

import android.app.Activity;

import com.shuqiang.toolbox.PermissionUtils;

/**
 * 相机权限封装
 */
public abstract class BasePermissionActivity extends Activity {
    private PermissionUtils.CameraPermissionHandler cameraPermissionHandler;
    private PermissionUtils.ExternalStoragePermissionHandler externalStoragePermissionHandler;

    public BasePermissionActivity() {
        super();
        cameraPermissionHandler = new PermissionUtils.CameraPermissionHandler(this) {
            @Override
            public void openCamera() {
                BasePermissionActivity.this.openCamera();
            }
        };
        externalStoragePermissionHandler = new PermissionUtils.ExternalStoragePermissionHandler(this) {
            @Override
            public void onReadWriteFile() {
                BasePermissionActivity.this.onReadWriteFile();
            }
        };
    }

    /**
     * 检查权限并拍照
     * 调用相机前先检查权限
     */
    protected void handleRequstPermissionAndCamera() {
        cameraPermissionHandler.handleRequestPermissionAndWork();
    }

    /**
     * 检查权限并拍照
     * 调用相机前先检查权限
     */
    protected void handleRequstPermissionAndReadWriteFile() {
        externalStoragePermissionHandler.handleRequestPermissionAndWork();
    }


    /**
     * 处理权限申请的回调。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        cameraPermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
        externalStoragePermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 打开相机
     */
    public abstract void openCamera();

    /**
     * 读写文件
     */
    public abstract void onReadWriteFile();
}