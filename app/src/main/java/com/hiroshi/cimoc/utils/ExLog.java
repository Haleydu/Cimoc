package com.hiroshi.cimoc.utils;

import android.util.Log;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class ExLog {

    private static boolean DEBUG = true;

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d("ExLog", tag + " - " +  msg);
        }
    }

}
