package com.hiroshi.cimoc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String getProgress(int progress, int max) {
        return format("%d/%d", progress, max);
    }

    public static String getDateStringWithSuffix(String suffix) {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()).concat(".").concat(suffix);
    }

    public static String[] range(int start, int end, int step) {
        if (step > 0) {
            int size = (end - start) / step;
            if (size >= 0) {
                size += 1;
                String[] array = new String[size];
                for (int i = 0; i != size; ++i) {
                    array[i] = String.valueOf(start + i * step);
                }
                return array;
            }
        }
        return new String[0];
    }

    public static String match(String regex, String input, int group) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(group);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] match(String regex, String input, int... group) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String[] result = new String[group.length];
                for (int i = 0; i != result.length; ++i) {
                    result[i] = matcher.group(group[i]);
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> matchAll(String regex, String input, int group) {
        LinkedList<String> list = new LinkedList<>();
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                list.add(matcher.group(group));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
