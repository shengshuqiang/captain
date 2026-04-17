package com.shuqiang.captain.chess.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public final class ChessBluetoothPermissionManager {
    public static final int REQUEST_CODE_BLUETOOTH_PERMISSIONS = 0xC811;
    public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 0xC812;
    public static final int REQUEST_CODE_ENABLE_DISCOVERABLE = 0xC813;
    public static final int PERMISSION_NONE = 0;
    // Align runtime permission requests with the exact Bluetooth capability each action uses.
    public static final int PERMISSION_CONNECT = 1;
    public static final int PERMISSION_SCAN = 1 << 1;
    public static final int PERMISSION_ADVERTISE = 1 << 2;
    public static final int HOST_DISCOVERABLE_DURATION_SECONDS = 300;
    private static final int SDK_31 = 31;
    private static final String PERMISSION_BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN";
    private static final String PERMISSION_BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT";
    private static final String PERMISSION_BLUETOOTH_ADVERTISE = "android.permission.BLUETOOTH_ADVERTISE";
    private static final String PERMISSION_ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";

    private ChessBluetoothPermissionManager() {
    }

    public static Intent createEnableBluetoothIntent() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    public static Intent createDiscoverableIntent() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, HOST_DISCOVERABLE_DURATION_SECONDS);
        return intent;
    }

    public static boolean hasRequiredPermissions(Context context, int permissionMask) {
        return getMissingPermissions(context, permissionMask).isEmpty();
    }

    public static boolean requestPermissionsIfNeeded(Activity activity, int permissionMask) {
        List<String> missingPermissions = getMissingPermissions(activity, permissionMask);
        if (missingPermissions.isEmpty()) {
            return false;
        }
        ActivityCompat.requestPermissions(activity, missingPermissions.toArray(new String[0]),
                REQUEST_CODE_BLUETOOTH_PERMISSIONS);
        return true;
    }

    public static List<String> getMissingPermissions(Context context, int permissionMask) {
        List<String> permissions = new ArrayList<>();
        if (permissionMask == PERMISSION_NONE) {
            return permissions;
        }
        int targetSdk = context.getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= SDK_31) {
            if (requiresPermission(permissionMask, PERMISSION_CONNECT)) {
                addIfMissing(context, permissions, PERMISSION_BLUETOOTH_CONNECT);
            }
            if (requiresPermission(permissionMask, PERMISSION_SCAN)) {
                addIfMissing(context, permissions, PERMISSION_BLUETOOTH_SCAN);
            }
            if (requiresPermission(permissionMask, PERMISSION_ADVERTISE)) {
                addIfMissing(context, permissions, PERMISSION_BLUETOOTH_ADVERTISE);
            }
        }
        if (requiresPermission(permissionMask, PERMISSION_SCAN)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && targetSdk < SDK_31) {
            addIfMissing(context, permissions, PERMISSION_ACCESS_FINE_LOCATION);
        }
        return permissions;
    }

    public static boolean requiresPermission(int permissionMask, int permission) {
        return (permissionMask & permission) != 0;
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static void addIfMissing(Context context, List<String> permissions, String permission) {
        if (!isPermissionGranted(context, permission)) {
            permissions.add(permission);
        }
    }
}
