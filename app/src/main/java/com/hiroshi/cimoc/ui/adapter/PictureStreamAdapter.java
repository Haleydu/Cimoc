package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnSingleTapListener;

import java.util.LinkedList;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class PictureStreamAdapter extends BaseAdapter<String> {

    private PipelineDraweeControllerBuilder builder;
    private OnSingleTapListener listener;

    public PictureStreamAdapter(Context context, PipelineDraweeControllerBuilder builder, OnSingleTapListener listener) {
        super(context, new LinkedList<String>());
        this.builder = builder;
        this.listener = listener;
    }

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.picture_image_view) PhotoDraweeView photoView;

        public ViewHolder(View view) {
            super(view);
        }

        public boolean isEquals(PhotoDraweeView draweeView) {
            return draweeView == photoView;
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
        draweeView.setId(position);
        draweeView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        draweeView.setVerticalMode();
        draweeView.setOnSingleTapListener(listener);
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
        }).setTapToRetryEnabled(true);
        draweeView.setController(builder.setUri(mDataSet.get(position)).build());
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 10, 0, 10);
            }
        };
    }
}
