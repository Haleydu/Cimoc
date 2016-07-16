package com.hiroshi.cimoc.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.custom.zoomable.ZoomableDraweeView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PicturePagerAdapter extends PagerAdapter {

    private List<String> images;
    private LayoutInflater inflater;
    private int offset;
    private boolean absence;

    public PicturePagerAdapter(List<String> images, LayoutInflater inflater) {
        this.images = images;
        this.images.add("");
        this.offset = 0;
        this.inflater = inflater;
    }

    public void setAbsence(boolean absence) {
        this.absence = absence;
    }

    public void setPrevImages(String[] array) {
        images.addAll(1, Arrays.asList(array));
        offset = array.length;
        notifyDataSetChanged();
    }

    public void setNextImages(String[] array) {
        images.addAll(Arrays.asList(array));
        offset = 0;
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        View view = (View) object;
        int position = (Integer) view.getTag();
        if (position == 0) {
            return POSITION_NONE;
        }
        return  position + offset;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (position == 0 && absence) {
            View child = inflater.inflate(R.layout.item_absence, container, false);
            container.addView(child, 0);
            return child;
        }
        View child = inflater.inflate(R.layout.item_picture, container, false);
        ZoomableDraweeView draweeView = (ZoomableDraweeView) child.findViewById(R.id.picture_image_view);
        child.setTag(position);
        draweeView.setController(Fresco.newDraweeControllerBuilder().setUri(images.get(position)).build());
        container.addView(child, 0);
        return child;
    }

}
