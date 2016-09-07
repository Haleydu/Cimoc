package com.hiroshi.cimoc.utils;

import java.util.Locale;

/**
 * Created by Hiroshi on 2016/9/3.
 */
public class StringUtils {

    public static boolean isEmpty(String... args) {
        for (String arg : args) {
            if (arg == null || arg.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static String getSplit(String str, String regex, int position) {
        String[] array = str.split(regex);
        if (position < 0) {
            position = array.length + position;
        }
        return position < 0 && position >= array.length ? null : array[position];
    }

    public static String format(String format, Object... args) {
        return String.format(Locale.getDefault(), format, args);
    }

    public static String progress(int progress, int max) {
        return format("%d/%d", progress, max);
    }

}
