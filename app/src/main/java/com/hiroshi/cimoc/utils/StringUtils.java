package com.hiroshi.cimoc.utils;

/**
 * Created by Hiroshi on 2016/9/3.
 */
public class StringUtils {

    public static boolean isEmpty(String... args) {
        for (String arg : args) {
            if (arg == null || arg.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static String getSplit(String str, String regex, int position) {
        String[] array = str.split(regex);
        if (position < 0) {
            position = array.length + position;
        }
        return position < 0 && position >= array.length ? null : array[position];
    }

}
