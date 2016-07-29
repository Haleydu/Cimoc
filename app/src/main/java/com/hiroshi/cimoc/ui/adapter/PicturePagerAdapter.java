package com.hiroshi.cimoc.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.custom.LimitedViewPager;
import com.hiroshi.cimoc.ui.custom.zoomable.ZoomableDraweeView;

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
    private SimpleOnGestureListener listener;
    private PipelineDraweeControllerBuilder builder;
    private int prev;
    private int next;
    private int pStatus;
    private int nStatus;

    public PicturePagerAdapter(List<String> images, LayoutInflater inflater, SimpleOnGestureListener listener,
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

    public void notifyPrevPage(int status) {
        pStatus = status;
        notifyDataSetChanged();
    }

    public void notifyNextPage(int status) {
        nStatus = status;
        notifyDataSetChanged();
    }

    public int getLimitByPosition(int position) {
        if (position == prev) {
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
            ZoomableDraweeView draweeView = (ZoomableDraweeView) child.findViewById(R.id.picture_image_view);
            draweeView.setController(builder.setUri(images.get(position - prev - 1)).setTapToRetryEnabled(true).build());
            draweeView.setTapListener(listener);
            child.setTag(POSITION_UNCHANGED);
        }
        container.addView(child);
        return child;
    }

}
