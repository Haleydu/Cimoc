package com.haleydu.cimoc.utils;

import android.util.Log;

/**
 *  * log打印工具类
 *  
 */
public class LogUtil {

    private LogUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isDebug = true;// 是否需要打印bug，在application的onCreate函数里面初始化
    private static final String TAG = "Haleydu";

    // 下面四个是默认tag的函数
    public static void i(String msg) {
        if (isDebug) Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (isDebug)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (isDebug)
            Log.e(TAG, msg);
    }

    public static void v(String msg) {
        if (isDebug)
            Log.v(TAG, msg);
    }

    // 下面是传入自定义tag的函数
    public static void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    /**
     *      * 截断输出日志
     *      *
     *      * @param msg
     *      
     */
    public static void eLength(String tag, String msg) {
        if (isDebug) {
            if (tag == null || tag.length() == 0
                    || msg == null || msg.length() == 0)
                return;

            int segmentSize = 3 * 1024;
            long length = msg.length();
            if (length <= segmentSize) {// 长度小于等于限制直接打印
                Log.e(tag, msg);
            } else {
                while (msg.length() > segmentSize) {// 循环分段打印日志
                    String logContent = msg.substring(0, segmentSize);
                    msg = msg.replace(logContent, "");
                    Log.e(tag, logContent);
                }
                Log.e(tag, msg);// 打印剩余日志
            }
        }
    }

    public static void iLength(String tag, String msg) {
        if (isDebug) {
            if (tag == null || tag.length() == 0
                    || msg == null || msg.length() == 0)
                return;

            int segmentSize = 3 * 1024;
            long length = msg.length();
            if (length <= segmentSize) {// 长度小于等于限制直接打印
                Log.i(tag, msg);
            } else {
                while (msg.length() > segmentSize) {// 循环分段打印日志
                    String logContent = msg.substring(0, segmentSize);
                    msg = msg.replace(logContent, "");
                    Log.i(tag, logContent);
                }
                Log.i(tag, msg);// 打印剩余日志
            }
        }
    }
}

