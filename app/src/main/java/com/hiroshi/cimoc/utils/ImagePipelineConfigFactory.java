package com.hiroshi.cimoc.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.hiroshi.cimoc.core.Kami;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ImagePipelineConfigFactory {

    private static ImagePipelineConfig defaultConfig;
    private static ImagePipelineConfig ikanmanConfig;
    private static ImagePipelineConfig dmzjConfig;

    private ImagePipelineConfigFactory() {}

    public static ImagePipelineConfig getImagePipelineConfig(Context context, int source) {
        switch (source) {
            case Kami.SOURCE_IKANMAN:
                if (ikanmanConfig == null) {
                    OkHttpClient client = getHeaderClient(source);
                    ikanmanConfig = buildConfig(context, client);
                }
                return ikanmanConfig;
            case Kami.SOURCE_DMZJ:
                if (dmzjConfig == null) {
                    OkHttpClient client = getHeaderClient(source);
                    dmzjConfig = buildConfig(context, client);
                }
                return dmzjConfig;
            default:
                return getImagePipelineConfig(context);
        }
    }

    public static ImagePipelineConfig getImagePipelineConfig(Context context) {
        if (defaultConfig == null) {
            OkHttpClient client = new OkHttpClient();
            defaultConfig =  buildConfig(context, client);
        }
        return defaultConfig;
    }

    private static ImagePipelineConfig buildConfig(Context context, OkHttpClient client) {
        return OkHttpImagePipelineConfigFactory.newBuilder(context, client)
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();
    }

    private static OkHttpClient getHeaderClient(final int source) {
        return new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                String referer = Kami.getRefererById(source);
                Request.Builder request = chain.request().newBuilder();
                request.addHeader("Referer", referer);
                return chain.proceed(request.build());
            }
        }).build();
    }


}
