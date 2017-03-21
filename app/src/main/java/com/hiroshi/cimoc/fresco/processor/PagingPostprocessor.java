package com.hiroshi.cimoc.fresco.processor;

import android.graphics.Bitmap;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.utils.StringUtils;

/**
 * Created by Hiroshi on 2017/3/3.
 */

public class PagingPostprocessor extends BasePostprocessor {

    private ImageUrl image;

    public PagingPostprocessor(ImageUrl image) {
        this.image = image;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        if (width > 1.15 * height) {
            CloseableReference<Bitmap> reference = bitmapFactory.createBitmap(width / 2, height, Bitmap.Config.RGB_565);
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
        if (image.getState() == ImageUrl.STATE_NULL) {
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_PICTURE_PAGING, image));
            while (image.getState() == ImageUrl.STATE_NULL); // 有种自旋锁的感觉 不知有没问题
        }
        pagePaging(src, dst, width, height);
    }

    private void pagePaging(Bitmap src, Bitmap dst, int width, int height) {
        int unit = height / 20;
        int base = image.getState() == ImageUrl.STATE_PAGE_1 ? width / 2 : 0;
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
        return new SimpleCacheKey(StringUtils.format("%s-page-paging-%d", image.getUrl(), image.getId()));
    }

}
