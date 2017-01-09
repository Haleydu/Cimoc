package com.hiroshi.cimoc.fresco;

import android.graphics.Bitmap;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * Created by Hiroshi on 2016/9/22.
 */

public class SplitPostprocessor extends BasePostprocessor {

    private String url;

    public SplitPostprocessor(String url) {
        this.url = url;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        if (width > 1.15 * height) {
            CloseableReference<Bitmap> reference = bitmapFactory.createBitmap(width / 2, height * 2, Bitmap.Config.RGB_565);
            try {
                Bitmap bitmap = reference.get();
                int unit = height / 20;
                int base = 0;
                int remain = height - 20 * unit;
                int[] pixels = new int[(remain > unit ? remain : unit) * width];
                for (int i = 1; i >= 0; --i) {
                    for (int j = 0; j < 20; ++j) {
                        sourceBitmap.getPixels(pixels, 0, width, i * width / 2, j * unit, width / 2, unit);
                        bitmap.setPixels(pixels, 0, width, 0, base + j * unit, width / 2, unit);
                    }
                    if (remain > 0) {
                        sourceBitmap.getPixels(pixels, 0, width, i * width / 2, 20 * unit, width / 2, remain);
                        bitmap.setPixels(pixels, 0, width, 0, base + 20 * unit, width / 2, remain);
                    }
                    base += height;
                }
                return CloseableReference.cloneOrNull(reference);
            } finally {
                CloseableReference.closeSafely(reference);
            }
        }
        return super.process(sourceBitmap, bitmapFactory);
    }

    @Override
    public CacheKey getPostprocessorCacheKey() {
        return new SimpleCacheKey(url.concat("split"));
    }

}
