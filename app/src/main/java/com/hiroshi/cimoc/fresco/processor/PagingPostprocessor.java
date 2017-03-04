package com.hiroshi.cimoc.fresco.processor;

import android.graphics.Bitmap;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;

/**
 * Created by Hiroshi on 2017/3/3.
 */

public class PagingPostprocessor extends BasePostprocessor {

    public static final int MODE_STREAM = 0;
    public static final int MODE_PAGE_1 = 1;
    public static final int MODE_PAGE_2 = 2;

    private int mode;
    private int id;
    private String url;

    public PagingPostprocessor(String url, int id, int mode) {
        this.url = url;
        this.id = id;
        this.mode = mode;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        if (width > 1.15 * height) {
            CloseableReference<Bitmap> reference = getBitmap(bitmapFactory, width, height);
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
        switch (mode) {
            case MODE_STREAM:
                streamPaging(src, dst, width, height);
                break;
            case MODE_PAGE_1:
                if (id != -1) {
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_PICTURE_PAGING, id));
                }
            case MODE_PAGE_2:
                pagePaging(src, dst, width, height);
                break;
        }
    }

    private CloseableReference<Bitmap> getBitmap(PlatformBitmapFactory bitmapFactory, int width, int height) {
        if (mode == MODE_STREAM) {
            return bitmapFactory.createBitmap(width / 2, height * 2, Bitmap.Config.RGB_565);
        }
        return bitmapFactory.createBitmap(width / 2, height, Bitmap.Config.RGB_565);
    }

    private void streamPaging(Bitmap src, Bitmap dst, int width, int height) {
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

    private void pagePaging(Bitmap src, Bitmap dst, int width, int height) {
        int unit = height / 20;
        int base = mode == MODE_PAGE_1 ? width / 2 : 0;
        int remain = height - 20 * unit;
        int[] pixels = new int[(remain > unit ? remain : unit) * (width / 2)];
        for (int j = 0; j < 20; ++j) {
            src.getPixels(pixels, 0, width / 2, base, j * unit, width / 2, unit);
            dst.setPixels(pixels, 0, width / 2, 0, j * unit, width / 2, unit);
        }
        if (remain > 0) {
            src.getPixels(pixels, 0, width / 2, base, 20 * unit, width / 2, remain);
            dst.setPixels(pixels, 0, width / 2, 0, 20 * unit, width / 2, remain);
        }
    }

    @Override
    public CacheKey getPostprocessorCacheKey() {
        switch (mode) {
            default:
            case MODE_STREAM:
                return new SimpleCacheKey(url.concat("paging"));
            case MODE_PAGE_1:
                return new SimpleCacheKey(url.concat("paging1"));
            case MODE_PAGE_2:
                return new SimpleCacheKey(url.concat("paging2"));
        }
    }

}
