package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnSingleTapListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class ReaderAdapter extends BaseAdapter<String> {

    public static final int MODE_PAGE = 0;
    public static final int MODE_STREAM = 1;

    @IntDef({MODE_PAGE, MODE_STREAM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PictureMode {}

    private PipelineDraweeControllerBuilder builder;
    private OnSingleTapListener listener;
    private @PictureMode int mode;
    private boolean split = false;

    public ReaderAdapter(Context context, List<String> list) {
        super(context, list);
    }

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.reader_image_view) PhotoDraweeView photoView;

        public ViewHolder(View view) {
            super(view);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_picture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final PhotoDraweeView draweeView = ((ViewHolder) holder).photoView;
        switch (mode) {
            case MODE_PAGE:
                draweeView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                draweeView.setHorizontalMode();
                builder.setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imageInfo == null) {
                            return;
                        }
                        draweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                });
                break;
            case MODE_STREAM:
                draweeView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                draweeView.setVerticalMode();
                builder.setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                        super.onIntermediateImageSet(id, imageInfo);
                        if (imageInfo != null) {
                            draweeView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            draweeView.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
                        }
                    }

                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imageInfo != null) {
                            draweeView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            draweeView.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
                            draweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                        }
                    }
                });
                break;
        }
        draweeView.setOnSingleTapListener(listener);
        builder.setTapToRetryEnabled(true);
        if (split) {
            ImageRequest request = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(mDataSet.get(position)))
                    .setPostprocessor(new SplitPostprocessor(mDataSet.get(position)))
                    .build();
            draweeView.setController(builder.setImageRequest(request).build());
        } else {
            draweeView.setController(builder.setUri(mDataSet.get(position)).build());
        }
    }

    public void setControllerBuilder(PipelineDraweeControllerBuilder builder) {
        this.builder = builder;
    }

    public void setSingleTapListener(OnSingleTapListener listener) {
        this.listener = listener;
    }

    public void setAutoSplit(boolean split) {
        this.split = split;
    }

    public void setPictureMode(@PictureMode int mode) {
        this.mode = mode;
    }

    class SplitPostprocessor extends BasePostprocessor {
        String url;

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
                    int[] pixels = new int[unit * width];
                    int base = 0;
                    int remain = height - 20 * unit;
                    for (int i = 1; i >= 0; --i) {
                        for (int j = 0; j != 20; ++j) {
                            sourceBitmap.getPixels(pixels, 0, width, i * width / 2, j % 20 * unit, width / 2, unit);
                            bitmap.setPixels(pixels, 0, width, 0, base + j % 20 * unit, width / 2, unit);
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
            return new SimpleCacheKey(url + "split");
        }
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        switch (mode) {
            default:
            case MODE_PAGE:
                return new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.set(0, 0, 0, 0);
                    }
                };
            case MODE_STREAM:
                return new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.set(0, 10, 0, 10);
                    }
                };
        }
    }

}
