package com.hiroshi.cimoc.fresco.processor;

import android.graphics.Bitmap;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * 二值化处理 仅用于测试
 * Created by Hiroshi on 2017/1/10.
 */

public class BinaryPostprocessor extends BasePostprocessor {

    private String url;

    public BinaryPostprocessor(String url) {
        this.url = url;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        int[] pixels = new int[width * height];
        sourceBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int pixel = pixels[i * width + j];
                int red = ((pixel & 0x00FF0000) >> 16);
                int green = ((pixel & 0x0000FF00) >> 8);
                int blue = (pixel & 0x000000FF);
                int gray = red * 30 + green * 59 + blue * 11;
                pixels[i * width + j] = gray > 21500 ? 0xFFFFFF : 0x000000;
            }
        }

        CloseableReference<Bitmap> reference = bitmapFactory.createBitmap(width, height, Bitmap.Config.RGB_565);
        try {
            Bitmap bitmap = reference.get();
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return CloseableReference.cloneOrNull(reference);
        } finally {
            CloseableReference.closeSafely(reference);
        }
    }

    @Override
    public CacheKey getPostprocessorCacheKey() {
        return new SimpleCacheKey(url.concat("binary"));
    }

}

