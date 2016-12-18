package com.hiroshi.cimoc.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.hiroshi.cimoc.service.DownloadService;

/**
 * Created by Hiroshi on 2016/12/4.
 */

public class ServiceUtils {

    public static boolean isServiceRunning(Context context, Class<?> service) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (info.service.getClassName().equals(service.getName())) {
                return true;
            }
        }
        return false;
    }

    public static void stopService(Context context, Class<?> service) {
        context.stopService(new Intent(context, DownloadService.class));
    }

}
