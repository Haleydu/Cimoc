package com.hiroshi.cimoc.fresco.processor;

import android.graphics.Bitmap;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.utils.StringUtils;

/**
 * Created by Hiroshi on 2017/3/20.
 */

public class StreamPagingPostprocessor extends BasePostprocessor {

    private ImageUrl image;

    public StreamPagingPostprocessor(ImageUrl image) {
        this.image = image;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        if (width > 1.15 * height) {
            CloseableReference<Bitmap> reference = bitmapFactory.createBitmap(width / 2, height * 2, Bitmap.Config.RGB_565);
            try {
                paging(sourceBitmap, reference.get(), width, height);
                return CloseableReference.cloneOrNull(reference);
            } finally {
                CloseableReference.closeSafely(reference);
            }
        }
        return super.process(sourceBitmap, bitmapFactory);
    }

    private void paging(Bitmap src, Bitmap dst, int width, int height) {
        int unit = height / 20;
        int base = 0;
        int remain = height - 20 * unit;
        int[] pixels = new int[(remain > unit ? remain : unit) * width];
        for (int i = 1; i >= 0; --i) {
            for (int j = 0; j < 20; ++j) {
                src.getPixels(pixels, 0, width, i * width / 2, j * unit, width / 2, unit);
                dst.setPixels(pixels, 0, width, 0, base + j * unit, width / 2, unit);
            }
            if (remain > 0) {
                src.getPixels(pixels, 0, width, i * width / 2, 20 * unit, width / 2, remain);
                dst.setPixels(pixels, 0, width, 0, base + 20 * unit, width / 2, remain);
            }
            base += height;
        }
    }

    @Override
    public CacheKey getPostprocessorCacheKey() {
        return new SimpleCacheKey(StringUtils.format("%s-stream-paging-%d", image.getUrl(), image.getId()));
    }

}
