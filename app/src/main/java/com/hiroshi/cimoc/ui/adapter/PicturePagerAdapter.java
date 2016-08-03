package com.hiroshi.cimoc.ui.adapter;

import android.graphics.drawable.Animatable;
import android.support.v4.view.PagerAdapter;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.custom.LimitedViewPager;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnSingleTapListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PicturePagerAdapter extends PagerAdapter {

    public static final int MAX_COUNT = 20000;

    public static final int STATUS_LOAD = 0;
    public static final int STATUS_NULL = 1;
    public static final int STATUS_ERROR = 2;

    private List<String> images;
    private LayoutInflater inflater;
    private OnSingleTapListener listener;
    private PipelineDraweeControllerBuilder builder;
    private int prev;
    private int next;
    private int pStatus;
    private int nStatus;

    public PicturePagerAdapter(List<String> images, LayoutInflater inflater, OnSingleTapListener listener,
                               PipelineDraweeControllerBuilder builder) {
        this.images = images;
        this.inflater = inflater;
        this.listener = listener;
        this.builder = builder;
        this.prev = MAX_COUNT / 2;
        this.next = MAX_COUNT / 2 + 1;
        this.pStatus = STATUS_LOAD;
        this.nStatus = STATUS_LOAD;
    }

    public void setPrevImages(String[] array) {
        images.addAll(0, Arrays.asList(array));
        prev -= array.length;
        notifyDataSetChanged();
    }

    public void setNextImages(String[] array) {
        images.addAll(Arrays.asList(array));
        next += array.length;
        notifyDataSetChanged();
    }

    public void notifySpecialPage(boolean isFirst, int status) {
        if (isFirst) {
            pStatus = status;
        } else {
            nStatus = status;
        }
        notifyDataSetChanged();
    }

    public int getLimitByPosition(int position) {
        if (prev + 1 == next) {
            return LimitedViewPager.LIMIT_BOTH;
        } else if (position == prev) {
            return  LimitedViewPager.LIMIT_RIGHT;
        } else if (position == next) {
            return LimitedViewPager.LIMIT_LEFT;
        }
        return LimitedViewPager.LIMIT_NONE;
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
        View child;
        if (position <= prev || position >= next) {
            child = inflater.inflate(R.layout.item_picture_msg, container, false);
            TextView textView = (TextView) child.findViewById(R.id.picture_msg);
            int what = STATUS_LOAD;
            if (position == prev) {
                what = pStatus;
            } else if (position == next) {
                what = nStatus;
            }
            switch (what) {
                case STATUS_LOAD:
                    textView.setText("等待加载中...");
                    break;
                case STATUS_NULL:
                    textView.setText("没有了 :(");
                    break;
                case STATUS_ERROR:
                    textView.setText("加载错误 :(");
            }
            child.setTag(POSITION_NONE);
        } else {
            child = inflater.inflate(R.layout.item_picture, container, false);
            final PhotoDraweeView draweeView = (PhotoDraweeView) child.findViewById(R.id.picture_image_view);
            draweeView.setOnSingleTapListener(listener);
            builder.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null || draweeView == null) {
                        return;
                    }
                    draweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            }).setTapToRetryEnabled(true);
            draweeView.setController(builder.setUri(images.get(position - prev - 1)).build());
            child.setTag(POSITION_UNCHANGED);
        }
        container.addView(child);
        return child;
    }

}
