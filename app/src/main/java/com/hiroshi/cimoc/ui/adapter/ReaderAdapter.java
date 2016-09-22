package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.SplitPostprocessor;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnLongPressListener;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnSingleTapListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class ReaderAdapter extends BaseAdapter<ImageUrl> {

    public static final int MODE_PAGE = 0;
    public static final int MODE_STREAM = 1;

    private static final int TYPE_LOADING = 0;
    private static final int TYPE_IMAGE = 1;

    @IntDef({MODE_PAGE, MODE_STREAM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PictureMode {}

    private PipelineDraweeControllerBuilder mControllerBuilder;
    private OnSingleTapListener mSingleTapListener;
    private OnLongPressListener mLongPressListener;
    private OnLazyLoadListener mLazyLoadListener;
    private @PictureMode int mode;
    private boolean split = false;

    public ReaderAdapter(Context context, List<ImageUrl> list) {
        super(context, list);
    }

    class ImageHolder extends BaseViewHolder {
        @BindView(R.id.reader_image_view) PhotoDraweeView photoView;
        ImageHolder(View view) {
            super(view);
        }
    }

    class LoadingHolder extends BaseViewHolder {
        LoadingHolder(View view) {
            super(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDataSet.get(position).isLazy() ? TYPE_LOADING : TYPE_IMAGE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            View view = mInflater.inflate(R.layout.item_picture, parent, false);
            return new ImageHolder(view);
        }
        View view = mInflater.inflate(R.layout.item_loading, parent, false);
        return new LoadingHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ImageUrl imageUrl = mDataSet.get(position);
        if (imageUrl.isLazy()) {
            if (!imageUrl.isLoading() && mLazyLoadListener != null) {
                imageUrl.setLoading(true);
                mLazyLoadListener.onLoad(imageUrl);
            }
            return;
        }
        final PhotoDraweeView draweeView = ((ImageHolder) holder).photoView;
        switch (mode) {
            case MODE_PAGE:
                draweeView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                draweeView.setHorizontalMode();
                mControllerBuilder.setControllerListener(new BaseControllerListener<ImageInfo>() {
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
                mControllerBuilder.setControllerListener(new BaseControllerListener<ImageInfo>() {
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
        draweeView.setOnSingleTapListener(mSingleTapListener);
        draweeView.setOnLongPressListener(mLongPressListener);
        mControllerBuilder.setOldController(draweeView.getController()).setTapToRetryEnabled(true);
        ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(imageUrl.getUrl()));
        if (split) {
            imageRequestBuilder.setPostprocessor(new SplitPostprocessor(imageUrl.getUrl()));
        }
        draweeView.setController(mControllerBuilder.setImageRequest(imageRequestBuilder.build()).build());
    }

    public void setControllerBuilder(PipelineDraweeControllerBuilder builder) {
        this.mControllerBuilder = builder;
    }

    public void setSingleTapListener(OnSingleTapListener listener) {
        this.mSingleTapListener = listener;
    }

    public void setLongPressListener(OnLongPressListener listener) {
        this.mLongPressListener = listener;
    }

    public void setLazyLoadListener(OnLazyLoadListener listener) {
        this.mLazyLoadListener = listener;
    }

    public void setAutoSplit(boolean split) {
        this.split = split;
    }

    public void setPictureMode(@PictureMode int mode) {
        this.mode = mode;
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

    public void update(int id, String url) {
        for (int i = 0; i != mDataSet.size(); ++i) {
            ImageUrl imageUrl = mDataSet.get(i);
            if (imageUrl.getId() == id && imageUrl.isLoading()) {
                if (url == null) {
                    imageUrl.setLoading(false);
                    return;
                }
                imageUrl.setUrl(url);
                imageUrl.setLoading(false);
                imageUrl.setLazy(false);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public interface OnLazyLoadListener {
        void onLoad(ImageUrl imageUrl);
    }

}
