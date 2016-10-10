package com.hiroshi.cimoc.fresco;

import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.core.manager.SourceManager;

import okhttp3.Headers;
import okhttp3.OkHttpClient;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ImagePipelineFactoryBuilder {

    public static ImagePipelineFactory build(Context context, int source) {
        OkHttpClient client = CimocApplication.getHttpClient();
        ImagePipelineConfig config =
                OkHttpImagePipelineConfigFactory.newBuilder(context.getApplicationContext(), client, getHeaders(source))
                        .setBitmapsConfig(Bitmap.Config.RGB_565)
                        .build();
        return new ImagePipelineFactory(config);
    }

    public static Headers getHeaders(int id) {
        switch (id) {
            case SourceManager.SOURCE_IKANMAN:
                return Headers.of("Referer", "http://m.ikanman.com");
            case SourceManager.SOURCE_DMZJ:
                return Headers.of("Referer", "http://m.dmzj.com/");
            case SourceManager.SOURCE_HHAAZZ:
                return Headers.of("Referer", "http://hhaazz.com");
            case SourceManager.SOURCE_CCTUKU:
                return Headers.of("Referer", "http://m.tuku.cc");
            case SourceManager.SOURCE_U17:
                return Headers.of("Referer", "http://www.u17.com");
            case SourceManager.SOURCE_DM5:
                return Headers.of("Referer", "http://www.dm5.com");
            case SourceManager.SOURCE_WEBTOON:
                return Headers.of("Referer", "http://m.webtoons.com");
            case SourceManager.SOURCE_HHSSEE:
                return Headers.of("Referer", "http://www.hhssee.com");
            case SourceManager.SOURCE_57MH:
                return Headers.of("Referer", "http://m.57mh.com");
            case SourceManager.SOURCE_EHENTAI:
                return Headers.of("Referer", "http://g.e-hentai.org");
            case SourceManager.SOURCE_EXHENTAI:
                return Headers.of("Referer", "https://exhentai.org", "Cookie", "igneous=583e748d60dc007822213a471d8e71dcba801b6a55cd0ffe04953e8adb63f294d4b60f303d9182b4276281ac883cec4c48a669db0b6c4914da78073945f49b12583e748d60dc007822213a471d8e71dcba801b6a55cd0ffe04953e8adb63f294d4b60f303d9182b4276281ac883cec4c48a669db0b6c4914da78073945f49b12");
            case SourceManager.SOURCE_NHENTAI:
                return Headers.of("Referer", "https://nhentai.net");
            case SourceManager.SOURCE_WNACG:
                return Headers.of("Referer", "https://www.wnacg.com");
            case SourceManager.SOURCE_177PIC:
                return Headers.of("Referer", "http://www.177pic66.com");
        }
        return Headers.of();
    }

}
