package com.shuqiang.toolbox;

import android.Manifest;
import android.app.Activity;

public final class PermissionUtils {
    // 申请相机权限的requestCode
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;
    // 申请外部存储权限的requestCode
    public static final int PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 0x00000013;

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
}
