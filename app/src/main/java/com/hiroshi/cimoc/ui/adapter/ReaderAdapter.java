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
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilderSupplier;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
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

    public static final int READER_PAGE = 0;
    public static final int READER_STREAM = 1;

    public static final int TYPE_LOADING = 2016101214;
    public static final int TYPE_IMAGE = 2016101215;

    @IntDef({READER_PAGE, READER_STREAM})
    @Retention(RetentionPolicy.SOURCE)
    @interface ReaderMode {}

    private PipelineDraweeControllerBuilderSupplier mControllerSupplier;
    private OnSingleTapListener mSingleTapListener;
    private OnLongPressListener mLongPressListener;
    private OnLazyLoadListener mLazyLoadListener;
    private @ReaderMode int reader;
    private int turn;
    private boolean split = false;

    public ReaderAdapter(Context context, List<ImageUrl> list) {
        super(context, list);
    }

    class ImageHolder extends BaseViewHolder {
        @BindView(R.id.reader_image_view) PhotoDraweeView photoView;
        ImageHolder(View view) {
            super(view);
            photoView.setOnSingleTapListener(mSingleTapListener);
            photoView.setOnLongPressListener(mLongPressListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDataSet.get(position).isLazy() ? TYPE_LOADING : TYPE_IMAGE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int resId = viewType == TYPE_IMAGE ? R.layout.item_picture : R.layout.item_loading;
        View view = mInflater.inflate(resId, parent, false);
        return new ImageHolder(view);
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
        if (turn == PreferenceManager.READER_TURN_ATB) {
            draweeView.setVerticalMode();
        } else {
            draweeView.setHorizontalMode();
        }
        PipelineDraweeControllerBuilder builder = mControllerSupplier.get();
        switch (reader) {
            case READER_PAGE:
                draweeView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                builder.setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        if (imageInfo == null) {
                            return;
                        }
                        draweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                });
                break;
            case READER_STREAM:
                draweeView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                builder.setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                        if (imageInfo != null) {
                            if (turn == PreferenceManager.READER_TURN_ATB) {
                                draweeView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            } else {
                                draweeView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                            }
                            draweeView.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
                        }
                    }

                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        if (imageInfo != null) {
                            if (turn == PreferenceManager.READER_TURN_ATB) {
                                draweeView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            } else {
                                draweeView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                            }
                            draweeView.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
                            draweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                        }
                    }
                });
                break;
        }
        String[] url = imageUrl.getUrl();
        ImageRequest[] request = new ImageRequest[url.length];
        for (int i = 0; i != url.length; ++i) {
            ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(url[i]));
            if (reader == READER_STREAM && turn == PreferenceManager.READER_TURN_ATB && split) {
                imageRequestBuilder.setPostprocessor(new SplitPostprocessor(url[i]));
            }
            request[i] = imageRequestBuilder.build();
        }
        builder.setOldController(draweeView.getController()).setTapToRetryEnabled(true);
        draweeView.setController(builder.setFirstAvailableImageRequests(request).build());
    }

    public void setControllerSupplier(PipelineDraweeControllerBuilderSupplier supplier) {
        this.mControllerSupplier = supplier;
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

    public void setReaderMode(@ReaderMode int reader) {
        this.reader = reader;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        switch (reader) {
            default:
            case READER_PAGE:
                return new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.set(0, 0, 0, 0);
                    }
                };
            case READER_STREAM:
                return new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        if (turn == PreferenceManager.READER_TURN_ATB) {
                            outRect.set(0, 10, 0, 10);
                        } else {
                            outRect.set(10, 0, 10, 0);
                        }
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
