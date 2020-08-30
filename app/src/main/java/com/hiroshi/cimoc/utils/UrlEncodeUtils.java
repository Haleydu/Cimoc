package com.hiroshi.cimoc.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlEncodeUtils {

    public static String urlEncodeChinese(String url) {
        try {
            Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(url);
            String tmp = "";
            while (matcher.find()) {
                tmp = matcher.group();
                url = url.replaceAll(tmp, URLEncoder.encode(tmp, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }
}
