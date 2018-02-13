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

public class MangaPostprocessor extends BasePostprocessor {

    private ImageUrl mImage;
    private boolean isPaging;
    private boolean isWhiteEdge;

    private int mWidth, mHeight;
    private int mPosX, mPosY;
    private boolean isDone = false;

    public MangaPostprocessor(ImageUrl image, boolean paging, boolean whiteEdge) {
        mImage = image;
        isPaging = paging;
        isWhiteEdge = whiteEdge;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        mWidth = sourceBitmap.getWidth();
        mHeight = sourceBitmap.getHeight();

        if (isPaging) {
            preparePaging();
            isDone = true;
        }
        if (isWhiteEdge) {
            prepareWhiteEdge(sourceBitmap);
            isDone = true;
        }

        if (isDone) {
            CloseableReference<Bitmap> reference = bitmapFactory.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
            try {
                processing(sourceBitmap, reference.get());
                return CloseableReference.cloneOrNull(reference);
            } finally {
                CloseableReference.closeSafely(reference);
            }
        }
        return super.process(sourceBitmap, bitmapFactory);
    }

    private void preparePaging() {
        if (needHorizontalPaging()) {
            mWidth = mWidth / 2;
            if (mImage.getState() == ImageUrl.STATE_NULL) {
                mImage.setState(ImageUrl.STATE_PAGE_1);
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_PICTURE_PAGING, mImage));
            }
            mPosX = mImage.getState() == ImageUrl.STATE_PAGE_1 ? mWidth : 0;
            mPosY = 0;
        } else if (needVerticalPaging()) {
            mHeight = mHeight / 2;
            if (mImage.getState() == ImageUrl.STATE_NULL) {
                mImage.setState(ImageUrl.STATE_PAGE_1);
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_PICTURE_PAGING, mImage));
            }
            mPosX = 0;
            mPosY = mImage.getState() == ImageUrl.STATE_PAGE_1 ? 0 : mHeight;
        }
    }

    private boolean needVerticalPaging() {
        return mHeight > 3 * mWidth;
    }

    private boolean needHorizontalPaging() {
        return mWidth > 1.2 * mHeight;
    }

    private void prepareWhiteEdge(Bitmap bitmap) {
        int y1, y2, x1, x2;
        int[] pixels = new int[(mWidth > mHeight ? mWidth : mHeight) * 20];
        int limit = mPosY + mHeight / 3;

        for (y1 = mPosY; y1 < limit; ++y1) {
            // 确定上线 y1
            bitmap.getPixels(pixels, 0, mWidth, mPosX, y1, mWidth, 1);
            if (!oneDimensionScan(pixels, mWidth)) {
                bitmap.getPixels(pixels, 0, mWidth, 0, y1, mWidth, 10);
                if (!twoDimensionScan(pixels, mWidth, false, false)) {
                    break;
                }
                y1 += 9;
            }
        }

        limit = mPosY + mHeight * 2 / 3;

        for (y2 = mPosY + mHeight - 1; y2 > limit; --y2) {
            // 确定下线 y2
            bitmap.getPixels(pixels, 0, mWidth, mPosX, y2, mWidth, 1);
            if (!oneDimensionScan(pixels, mWidth)) {
                bitmap.getPixels(pixels, 0, mWidth, 0, y2 - 9, mWidth, 10);
                if (!twoDimensionScan(pixels, mWidth, false, true)) {
                    break;
                }
                y2 -= 9;
            }
        }

        int h = y2 - y1 + 1;
        limit = mPosX + mWidth / 3;

        for (x1 = mPosX; x1 < limit; ++x1) {
            // 确定左线 x1
            bitmap.getPixels(pixels, 0, 1, x1, y1, 1, h);
            if (!oneDimensionScan(pixels, h)) {
                bitmap.getPixels(pixels, 0, 10, x1, y1, 10, h);
                if (!twoDimensionScan(pixels, h, true, false)) {
                    break;
                }
                x1 += 9;
            }
        }

        limit = mPosX + mWidth * 2 / 3;

        for (x2 = mPosX + mWidth - 1; x2 > limit; --x2) {
            // 确定右线 x2
            bitmap.getPixels(pixels, 0, 1, x2, y1, 1, h);
            if (!oneDimensionScan(pixels, h)) {
                bitmap.getPixels(pixels, 0, 10, x2 - 9, y1, 10, h);
                if (!twoDimensionScan(pixels, h, true, true)) {
                    break;
                }
                x2 -= 9;
            }
        }

        mWidth = x2 - x1;
        mHeight = y2 - y1;
        mPosX = x1;
        mPosY = y1;
    }

    private void processing(Bitmap src, Bitmap dst) {
        int unit = mHeight / 20;
        int remain = mHeight - 20 * unit;
        int[] pixels = new int[(remain > unit ? remain : unit) * mWidth];
        for (int j = 0; j < 20; ++j) {
            src.getPixels(pixels, 0, mWidth, mPosX, mPosY + j * unit, mWidth, unit);
            dst.setPixels(pixels, 0, mWidth, 0, j * unit, mWidth, unit);
        }
        if (remain > 0) {
            src.getPixels(pixels, 0, mWidth, mPosX, mPosY + 20 * unit, mWidth, remain);
            dst.setPixels(pixels, 0, mWidth, 0, 20 * unit, mWidth, remain);
        }
    }

    @Override
    public CacheKey getPostprocessorCacheKey() {
        return new SimpleCacheKey(StringUtils.format("%s-post-%d", mImage.getUrl(), mImage.getId()));
    }

    /**
     * @return 全白返回 true
     */
    private boolean oneDimensionScan(int[] pixels, int length) {
        for (int i = 0; i < length; ++i) {
            if (!isWhite(pixels[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 10 * 20 方格 按 2:3:3:2 划分为四个区域 权值分别为 0 1 2 3
     * @return 加权值 > 60 返回 false
     */
    private boolean twoDimensionScan(int[] pixels, int length, boolean vertical, boolean reverse) {
        if (length < 20) {
            return false;
        }

        int[] value = new int[20];
        int result = 0;
        for (int i = 0; i < length; ++i) {
            if (result > 60) {
                return false;
            }
            result -= value[i % 20];
            value[i % 20] = 0;
            for (int j = 0; j < 10; ++j) {
                int k = vertical ? (i * 10 + j) : (j * length + i);
                value[i % 20] += getValue(isWhite(pixels[k]), reverse, j);
            }
            result += value[i % 20];
        }
        return true;
    }

    /**
     * 根据方向位置计算权值
     */
    private int getValue(boolean white, boolean reverse, int pos) {
        if (white) {
            return 0;
        }
        if (pos < 2) {
            return reverse ? 3 : 0;
        } else if (pos < 5) {
            return reverse ? 2 : 1;
        } else if (pos < 8) {
            return reverse ? 1 : 2;
        }
        return reverse ? 0 : 3;
    }

    /**
     * 固定阈值 根据灰度判断黑白
     */
    private boolean isWhite(int pixel) {
        int red = ((pixel & 0x00FF0000) >> 16);
        int green = ((pixel & 0x0000FF00) >> 8);
        int blue = (pixel & 0x000000FF);
        int gray = red * 30 + green * 59 + blue * 11;
        return gray > 21500;
    }

}
