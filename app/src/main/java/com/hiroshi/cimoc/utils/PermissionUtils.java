package com.hiroshi.cimoc.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Created by Hiroshi on 2016/10/20.
 */

public class PermissionUtils {

    public static boolean hasStoragePermission(Activity activity) {
        int writeResult = checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return writeResult == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasAllPermissions(Activity activity) {
        int readResult = checkPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeResult = checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPhoneState = checkPermission(activity, Manifest.permission.READ_PHONE_STATE);
        return readResult == PackageManager.PERMISSION_GRANTED &&
                writeResult == PackageManager.PERMISSION_GRANTED &&
                readPhoneState == PackageManager.PERMISSION_GRANTED;
    }

    public static int checkPermission(@NonNull Activity activity, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(activity, permission);
    }
}
