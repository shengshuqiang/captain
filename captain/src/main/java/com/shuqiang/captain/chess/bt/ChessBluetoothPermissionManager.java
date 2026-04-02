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
    private static final int SDK_31 = 31;
    private static final String PERMISSION_BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN";
    private static final String PERMISSION_BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT";
    private static final String PERMISSION_ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";

    private ChessBluetoothPermissionManager() {
    }

    public static Intent createEnableBluetoothIntent() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    public static boolean hasRequiredPermissions(Context context, boolean includeDiscovery) {
        return getMissingPermissions(context, includeDiscovery).isEmpty();
    }

    public static boolean requestPermissionsIfNeeded(Activity activity, boolean includeDiscovery) {
        List<String> missingPermissions = getMissingPermissions(activity, includeDiscovery);
        if (missingPermissions.isEmpty()) {
            return false;
        }
        ActivityCompat.requestPermissions(activity, missingPermissions.toArray(new String[0]),
                REQUEST_CODE_BLUETOOTH_PERMISSIONS);
        return true;
    }

    public static List<String> getMissingPermissions(Context context, boolean includeDiscovery) {
        List<String> permissions = new ArrayList<>();
        int targetSdk = context.getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= SDK_31 && targetSdk >= SDK_31) {
            addIfMissing(context, permissions, PERMISSION_BLUETOOTH_CONNECT);
            if (includeDiscovery) {
                addIfMissing(context, permissions, PERMISSION_BLUETOOTH_SCAN);
            }
            return permissions;
        }
        if (includeDiscovery && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addIfMissing(context, permissions, PERMISSION_ACCESS_FINE_LOCATION);
        }
        return permissions;
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
