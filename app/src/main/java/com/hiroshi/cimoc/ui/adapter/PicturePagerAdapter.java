package com.hiroshi.cimoc.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hiroshi.cimoc.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PicturePagerAdapter extends PagerAdapter {

    private String[] images;
    private LayoutInflater inflater;
    private Picasso picasso;

    public PicturePagerAdapter(String[] images, LayoutInflater inflater, Picasso picasso) {
        this.images = images;
        this.inflater = inflater;
        this.picasso = picasso;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View child = inflater.inflate(R.layout.item_picture, container, false);
        ImageView imageView = (ImageView) child.findViewById(R.id.picture_image_view);
        picasso.load(images[position]).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
        container.addView(child, 0);
        return child;
    }

}
