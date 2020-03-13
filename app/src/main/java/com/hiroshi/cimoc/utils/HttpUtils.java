package com.hiroshi.cimoc.utils;

import okhttp3.Request;

/**
 * Created by toesbieya on 2020/03/04
 * okhttp的常用方法
 */
public class HttpUtils {
    public static Request getSimpleMobileRequest(String url) {
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/12.0 Mobile/15A372 Safari/604.1")
                .url(url)
                .build();
    }
}
