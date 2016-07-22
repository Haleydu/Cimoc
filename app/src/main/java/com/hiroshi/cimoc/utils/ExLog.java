package com.hiroshi.cimoc.utils;

import android.util.Log;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class ExLog {

    private static boolean flag = false;

    public static void enable() {
        flag = true;
    }

    public static void d(String tag, String msg) {
        if (flag) {
            Log.d("ExLog", tag + " - " +  msg);
        }
    }

}
