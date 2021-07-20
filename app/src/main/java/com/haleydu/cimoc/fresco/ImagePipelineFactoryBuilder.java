package com.haleydu.cimoc.fresco;

import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.haleydu.cimoc.App;

import okhttp3.Headers;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ImagePipelineFactoryBuilder {

    public static ImagePipelineFactory build(Context context, Headers header, boolean down) {
        ImagePipelineConfig.Builder builder =
                ImagePipelineConfig.newBuilder(context.getApplicationContext())
                        .setDownsampleEnabled(down)
                        .setBitmapsConfig(down ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888);
        if (header != null) {
            builder.setNetworkFetcher(new OkHttpNetworkFetcher(App.getHttpClient(), header));
        }
        return new ImagePipelineFactory(builder.build());
    }

}
