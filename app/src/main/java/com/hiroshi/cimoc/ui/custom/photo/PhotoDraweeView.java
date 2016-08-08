package com.hiroshi.cimoc.ui.custom.photo;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

public class PhotoDraweeView extends SimpleDraweeView  {

    private PhotoDraweeViewController mPhotoDraweeViewController;

    public PhotoDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init();
    }

    public PhotoDraweeView(Context context) {
        super(context);
        init();
    }

    public PhotoDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        if (mPhotoDraweeViewController == null || mPhotoDraweeViewController.getDraweeView() == null) {
            mPhotoDraweeViewController = new PhotoDraweeViewController(this);
        }
    }

    public boolean retry() {
        AbstractDraweeController controller = (AbstractDraweeController) getController();
        return controller != null && controller.onClick();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override protected void onDraw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        canvas.concat(mPhotoDraweeViewController.getDrawMatrix());
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override protected void onAttachedToWindow() {
        init();
        super.onAttachedToWindow();
    }

    @Override protected void onDetachedFromWindow() {
        mPhotoDraweeViewController.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    public void setOnSingleTapListener(PhotoDraweeViewController.OnSingleTapListener listener) {
        mPhotoDraweeViewController.setOnSingleTapListener(listener);
    }

    public void setHorizontalMode() {
        mPhotoDraweeViewController.setHorizontalMode();
    }

    public void setVerticalMode() {
        mPhotoDraweeViewController.setVerticalMode();
    }

    public void update(int imageInfoWidth, int imageInfoHeight) {
        mPhotoDraweeViewController.update(imageInfoWidth, imageInfoHeight);
    }

}
