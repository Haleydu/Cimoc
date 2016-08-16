package com.hiroshi.cimoc.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilderSupplier;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.hiroshi.cimoc.core.manager.SourceManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ControllerBuilderFactory {

    private static SparseArray<PipelineDraweeControllerBuilder> builderArray = new SparseArray<>();

    public static PipelineDraweeControllerBuilder getCoverControllerBuilder(int source, Context context) {
        PipelineDraweeControllerBuilder builder = builderArray.get(source);
        if (builder == null) {
            String cookie = source == SourceManager.SOURCE_EXHENTAI ? "igneous=583e748d60dc007822213a471d8e71dcba801b6a55cd0ffe04953e8adb63f294d4b60f303d9182b4276281ac883cec4c48a669db0b6c4914da78073945f49b12583e748d60dc007822213a471d8e71dcba801b6a55cd0ffe04953e8adb63f294d4b60f303d9182b4276281ac883cec4c48a669db0b6c4914da78073945f49b12" : null;
            ImagePipelineFactory factory = buildFactory(context.getApplicationContext(), source, cookie);
            builder = new PipelineDraweeControllerBuilderSupplier(context.getApplicationContext(), factory).get();
            builderArray.put(source, builder);
        }
        return builder;
    }

    public static PipelineDraweeControllerBuilder getControllerBuilder(int source, Context context) {
        String cookie = source == SourceManager.SOURCE_EXHENTAI ? "igneous=583e748d60dc007822213a471d8e71dcba801b6a55cd0ffe04953e8adb63f294d4b60f303d9182b4276281ac883cec4c48a669db0b6c4914da78073945f49b12583e748d60dc007822213a471d8e71dcba801b6a55cd0ffe04953e8adb63f294d4b60f303d9182b4276281ac883cec4c48a669db0b6c4914da78073945f49b12" : null;
        ImagePipelineFactory factory = buildFactory(context.getApplicationContext(), source, cookie);
        return new PipelineDraweeControllerBuilderSupplier(context.getApplicationContext(), factory).get();
    }

    private static ImagePipelineFactory buildFactory(Context context, final int source, final String cookie) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                    String referer = getReferer(source);
                    Request.Builder request = chain.request().newBuilder();
                    request.addHeader("Referer", referer);
                    if (cookie != null) {
                        request.header("Cookie", cookie);
                    }
                    return chain.proceed(request.build());
                    }
                }).build();
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory.newBuilder(context, client)
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();
        return new ImagePipelineFactory(config);
    }

    private static String getReferer(int id) {
        switch (id) {
            case SourceManager.SOURCE_IKANMAN:
                return "http://m.ikanman.com";
            case SourceManager.SOURCE_DMZJ:
                return "http://m.dmzj.com/";
            case SourceManager.SOURCE_HHAAZZ:
                return "http://hhaazz.com";
            case SourceManager.SOURCE_CCTUKU:
                return "http://m.tuku.cc";
            case SourceManager.SOURCE_U17:
                return "http://www.u17.com";
            case SourceManager.SOURCE_EHENTAI:
                return "http://lofi.e-hentai.org";
            case SourceManager.SOURCE_EXHENTAI:
                return "https://exhentai.org";
            case SourceManager.SOURCE_NHENTAI:
                return "https://nhentai.net";
            case SourceManager.SOURCE_WNACG:
                return "http://www.wnacg.com";
        }
        return "";
    }

}
