package com.hiroshi.cimoc.ui.adapter;

import android.graphics.drawable.Animatable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnSingleTapListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PicturePageAdapter extends PagerAdapter {

    public static final int MAX_COUNT = 20000;

    private List<String> images;
    private LayoutInflater inflater;
    private OnSingleTapListener listener;
    private PipelineDraweeControllerBuilder builder;
    private int left;
    private int right;

    private int current;

    public PicturePageAdapter(List<String> images, LayoutInflater inflater, PipelineDraweeControllerBuilder builder,
                              OnSingleTapListener listener) {
        this.images = images;
        this.inflater = inflater;
        this.builder = builder;
        this.listener = listener;
        this.left = MAX_COUNT / 2;
        this.current = MAX_COUNT / 2 + 1;
        this.right = MAX_COUNT / 2 + 1;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getCurrent() {
        return current;
    }

    public void setPrevImages(String[] array) {
        images.addAll(0, Arrays.asList(array));
        left -= array.length;
        notifyDataSetChanged();
    }

    public void setNextImages(String[] array) {
        images.addAll(Arrays.asList(array));
        right += array.length;
        notifyDataSetChanged();
    }

    public boolean isToLeft() {
        return current == left + 1;
    }

    public boolean isToRight() {
        return current == right - 1;
    }

    @Override
    public int getItemPosition(Object object) {
        View view = (View) object;
        return (int) view.getTag();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return MAX_COUNT;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View child = inflater.inflate(R.layout.item_picture, container, false);
        if (left < position && position < right) {
            final PhotoDraweeView draweeView = (PhotoDraweeView) child.findViewById(R.id.picture_image_view);
            draweeView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            draweeView.setHorizontalMode();
            draweeView.setOnSingleTapListener(listener);
            builder.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null) {
                        return;
                    }
                    draweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            }).setTapToRetryEnabled(true);
            draweeView.setController(builder.setUri(images.get(position - left - 1)).build());
            child.setTag(POSITION_UNCHANGED);
        } else {
            child.setTag(POSITION_NONE);
        }
        container.addView(child);
        return child;
    }

}
