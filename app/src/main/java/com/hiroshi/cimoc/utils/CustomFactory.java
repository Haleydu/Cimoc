package com.hiroshi.cimoc.utils;

import android.content.Context;

import com.hiroshi.cimoc.core.Kami;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class CustomFactory {

    public static Picasso getPicasso(Context context, int source) {
        final String referer = Kami.getHostById(source);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder request = chain.request().newBuilder();
                request.addHeader("Referer", referer);
                return chain.proceed(request.build());
            }
        }).build();
        return new Picasso.Builder(context).downloader(new OkHttp3Downloader(okHttpClient)).build();
    }


}
