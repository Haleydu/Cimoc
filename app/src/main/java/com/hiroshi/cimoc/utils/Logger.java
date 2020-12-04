package com.hiroshi.cimoc.utils;

import android.util.Log;

import java.util.Locale;


public class Logger {
    private static boolean isShow = true;

    private static final String TAG = Logger.class.getSimpleName();
    private static final String PRE = "###";
    private static final String SPACE = "====================================================================================================";
    private static final int MAX_LENGTH = 1024;

    /**
     * 初始化Log参数，默认开启
     *
     * @param isShowLog 是否开启日志打印，建议使用: BuildConfig.DEBUG
     */
    public static void init(boolean isShowLog) {
        isShow = isShowLog;
    }

    public static void v(String tag, String format, Object... args) {
        String message = buildMessage(format, args);
        tag = formatLength(PRE + tag, 28);
        printLargeLog(Level.V, tag, message);
    }

    public static void v(Throwable throwable, String tag, String format, Object... args) {
        String message = buildMessage(format, args);
        tag = formatLength(PRE + tag, 28);

        Log.v(tag, message, throwable);
    }

    public static void d(String tag, String format, Object... args) {
        String message = buildMessage(format, args);
        tag = formatLength(PRE + tag, 28);

        printLargeLog(Level.D, tag, message);
    }

    public static void d(Throwable throwable, String tag, String format, Object... args) {
        String message = buildMessage(format, args);
        tag = formatLength(PRE + tag, 28);
        Log.d(tag, message, throwable);
    }

    public static void i(String tag, String format, Object... args) {
        String message = buildMessage(format, args);
        tag = formatLength(PRE + tag, 28);
        printLargeLog(Level.I, tag, message);
    }

    public static void i(Throwable throwable, String tag, String format, Object... args) {
        String message = buildMessage(format, args);
        tag = formatLength(PRE + tag, 28);
        Log.i(tag, message, throwable);
    }

    public static void w(String tag, String format, Object... args) {
        String message = buildMessage(format, args);
        tag = formatLength(PRE + tag, 28);
        printLargeLog(Level.W, tag, message);
    }

    public static void w(Throwable throwable, String tag, String format, Object... args) {
        String message = buildMessage(format, args);
        tag = formatLength(PRE + tag, 28);

        Log.w(tag, message, throwable);
    }

    public static void e(String tag, String format, Object... args) {
        String message = buildMessage(format, args);
        tag = formatLength(PRE + tag, 28);

        printLargeLog(Level.E, tag, message);
    }

    public static void e(Throwable throwable, String tag, String format, Object... args) {
        String message = buildMessage(format, args);
        tag = formatLength(PRE + tag, 28);
        Log.e(tag, message, throwable);
    }

    private static void printLog(Level level, String tag, String message) {
        switch (level) {
            case V:
                Log.v(tag, message);
                break;
            case D:
                Log.d(tag, message);
                break;
            case I:
                Log.i(tag, message);
                break;
            case W:
                Log.w(tag, message);
                break;
            case E:
                Log.e(tag, message);
                break;
            default:
                break;
        }
    }

    private static void printLargeLog(Level level, String tag, String message) {
        if (!isShow) {
            return;
        }

        int length = message.length();
        int size = length % MAX_LENGTH == 0 ? length / MAX_LENGTH : length / MAX_LENGTH + 1;
        for (int i = 0; i < size; i++) {
            String subMessage = message.substring(i * MAX_LENGTH, (i + 1) * MAX_LENGTH > length ? length : (i + 1) * MAX_LENGTH);
            printLog(level, tag, subMessage);
        }
    }

    private static String buildMessage(String format, Object[] args) {
        try {
            String msg = (args == null || args.length == 0) ? format : String.format(Locale.getDefault(), format, args);
            if (!isShow) {
                return msg;
            }
            StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
            String caller = "";
            String callingClass = "";
            String callFile = "";
            int lineNumber = 0;
            for (int i = 2; i < trace.length; i++) {
                Class<?> clazz = trace[i].getClass();
                if (!clazz.equals(Logger.class)) {
                    callingClass = trace[i].getClassName();
                    callingClass = callingClass.substring(callingClass
                            .lastIndexOf('.') + 1);
                    caller = trace[i].getMethodName();
                    callFile = trace[i].getFileName();
                    lineNumber = trace[i].getLineNumber();
                    break;
                }
            }

            String method = String.format(Locale.getDefault(), "[%03d] %s.%s(%s:%d)"
                    , Thread.currentThread().getId(), callingClass, caller, callFile, lineNumber);

            return String.format(Locale.getDefault(), "%s> %s", formatLength(method, 93), msg);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return "----->ERROR LOG STRING<------";
    }

    private static String formatLength(String src, int len) {
        StringBuilder sb = new StringBuilder();
        if (src.length() >= len) {
            sb.append(src);
        } else {
            sb.append(src);
            sb.append(SPACE.substring(0, len - src.length()));
        }
        return sb.toString();
    }

    enum Level {
        V, D, I, W, E,
    }

}
