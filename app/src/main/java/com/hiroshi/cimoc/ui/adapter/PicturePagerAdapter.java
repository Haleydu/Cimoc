package com.hiroshi.cimoc.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hiroshi.cimoc.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PicturePagerAdapter extends PagerAdapter {

    private List<String> images;
    private LayoutInflater inflater;
    private int offset;

    public PicturePagerAdapter(List<String> images, LayoutInflater inflater) {
        this.images = images;
        this.inflater = inflater;
        this.offset = 0;
    }

    public void setPrevImages(String[] array) {
        images.addAll(0, Arrays.asList(array));
        notifyDataSetChanged();
    }

    public void setNextImages(String[] array) {
        images.addAll(Arrays.asList(array));
        notifyDataSetChanged();
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int getItemPosition(Object object) {
        View view = (View) object;
        return (Integer) view.getTag() + offset;
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
        View child = inflater.inflate(R.layout.item_picture, container, false);
        child.setTag(position);
        SimpleDraweeView draweeView = (SimpleDraweeView) child.findViewById(R.id.picture_image_view);
        draweeView.setImageURI(images.get(position));
        container.addView(child, 0);
        return child;
    }

}
