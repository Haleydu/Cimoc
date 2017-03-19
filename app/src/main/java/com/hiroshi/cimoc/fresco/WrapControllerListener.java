package com.hiroshi.cimoc.fresco;

import android.graphics.drawable.Animatable;
import android.view.ViewGroup;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;

/**
 * Created by Hiroshi on 2017/3/4.
 */

public class WrapControllerListener extends BaseControllerListener<ImageInfo> {

    private PhotoDraweeView mDraweeView;
    private int mImageId;
    private boolean isVertical;

    public WrapControllerListener(PhotoDraweeView draweeView, boolean vertical, int id) {
        mDraweeView = draweeView;
        mImageId = id;
        isVertical = vertical;
    }

    @Override
    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
        if (imageInfo != null) {
            if (isVertical) {
                mDraweeView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            } else {
                mDraweeView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            mDraweeView.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
            mDraweeView.update(mImageId, imageInfo.getWidth(), imageInfo.getHeight());
        }
    }

}
