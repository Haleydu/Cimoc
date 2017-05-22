package com.hiroshi.cimoc.fresco;

import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.hiroshi.cimoc.App;

import okhttp3.Headers;
import okhttp3.OkHttpClient;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ImagePipelineFactoryBuilder {

    public static ImagePipelineFactory build(Context context, Headers header, boolean down) {
        OkHttpClient client = App.getHttpClient();
        ImagePipelineConfig config =
                OkHttpImagePipelineConfigFactory.newBuilder(context.getApplicationContext(), client, header)
                        .setDownsampleEnabled(down)
                        .setBitmapsConfig(Bitmap.Config.RGB_565)
                        .build();
        return new ImagePipelineFactory(config);
    }

    public static ImagePipelineFactory build(Context context, boolean down) {
        return new ImagePipelineFactory(ImagePipelineConfig
                .newBuilder(context.getApplicationContext())
                .setDownsampleEnabled(down)
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build());
    }

}
