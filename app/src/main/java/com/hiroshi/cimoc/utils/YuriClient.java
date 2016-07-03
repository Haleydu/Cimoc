package com.hiroshi.cimoc.utils;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class YuriClient {

    public static YuriClient instance;

    private OkHttpClient client;

    private YuriClient() {
        client = new OkHttpClient();
    }

    public void enqueue(String url, Callback responseCallback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(responseCallback);
    }

    public static YuriClient getInstance() {
        if (instance == null) {
            instance = new YuriClient();
        }
        return instance;
    }

}
