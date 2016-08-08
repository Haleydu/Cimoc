package com.hiroshi.cimoc.ui.custom.photo;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ScrollerCompat;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.DraweeView;

import java.lang.ref.WeakReference;

public class PhotoDraweeViewController implements OnTouchListener, OnScaleDragGestureListener, OnDoubleTapListener {

    public static final float MIN_SCALE = 1.0f;
    public static final float MID_SCALE = 2.0f;
    public static final float MAX_SCALE = 3.0f;

    public interface OnSingleTapListener {
        void onSingleTap(PhotoDraweeView draweeView, float x, float y);
    }

    public interface OnScaleChangeListener {
        void onScaleChange(float scaleFactor, float focusX, float focusY);
    }

    private static final int EDGE_NONE = -1;
    private static final int EDGE_LEFT = 0;
    private static final int EDGE_RIGHT = 1;
    private static final int EDGE_TOP = 0;
    private static final int EDGE_BOTTOM = 1;
    private static final int EDGE_BOTH = 2;

    private static final int MODE_VERTICAL = 0;
    private static final int MODE_HORIZONTAL = 1;

    private final float[] mMatrixValues = new float[9];
    private final RectF mDisplayRect = new RectF();
    private final Interpolator mZoomInterpolator = new AccelerateDecelerateInterpolator();

    private long mZoomDuration = 200L;

    private ScaleDragDetector mScaleDragDetector;
    private GestureDetectorCompat mGestureDetector;

    private boolean mBlockParentIntercept = false;
    private boolean mAllowParentInterceptOnEdge = true;
    private int mScrollEdge = EDGE_BOTH;
    private int mScrollMode = MODE_HORIZONTAL;

    private final Matrix mMatrix = new Matrix();
    private int mImageInfoHeight = -1, mImageInfoWidth = -1;
    private FlingRunnable mCurrentFlingRunnable;
    private WeakReference<PhotoDraweeView> mDraweeView;

    private OnSingleTapListener mSingleTapListener;
    private OnScaleChangeListener mScaleChangeListener;

    public PhotoDraweeViewController(PhotoDraweeView draweeView) {
        mDraweeView = new WeakReference<>(draweeView);
        draweeView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
        draweeView.setOnTouchListener(this);
        mScaleDragDetector = new ScaleDragDetector(draweeView.getContext(), this);
        mGestureDetector = new GestureDetectorCompat(draweeView.getContext(), new GestureDetector.SimpleOnGestureListener());
        mGestureDetector.setOnDoubleTapListener(this);
    }

    public PhotoDraweeView getDraweeView() {
        return mDraweeView.get();
    }

    public float getScale() {
        return (float) Math.sqrt(
                (float) Math.pow(getMatrixValue(mMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow(
                        getMatrixValue(mMatrix, Matrix.MSKEW_Y), 2));
    }

    private void setScale(float scale, float focalX, float focalY, boolean animate) {
        DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();

        if (draweeView == null || scale < MIN_SCALE || scale > MAX_SCALE) {
            return;
        }

        if (animate) {
            draweeView.post(new AnimatedZoomRunnable(getScale(), scale, focalX, focalY));
        } else {
            mMatrix.setScale(scale, scale, focalX, focalY);
            checkMatrixAndInvalidate();
        }
    }

    public void setHorizontalMode() {
        mScrollMode = MODE_HORIZONTAL;
    }

    public void setVerticalMode() {
        mScrollMode = MODE_VERTICAL;
    }

    public void setZoomTransitionDuration(long duration) {
        duration = duration < 0 ? mZoomDuration : duration;
        mZoomDuration = duration;
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAllowParentInterceptOnEdge = allow;
    }

    public void setOnScaleChangeListener(OnScaleChangeListener listener) {
        mScaleChangeListener = listener;
    }

    public void setOnSingleTapListener(OnSingleTapListener listener) {
        mSingleTapListener = listener;
    }

    public OnSingleTapListener getOnSingleTapListener() {
        return mSingleTapListener;
    }

    public void update(int imageInfoWidth, int imageInfoHeight) {
        mImageInfoWidth = imageInfoWidth;
        mImageInfoHeight = imageInfoHeight;
        updateBaseMatrix();
    }

    private int getViewWidth() {
        DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();
        if (draweeView != null) {
            return draweeView.getWidth()
                    - draweeView.getPaddingLeft()
                    - draweeView.getPaddingRight();
        }
        return 0;
    }

    private int getViewHeight() {
        DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();
        if (draweeView != null) {
            return draweeView.getHeight()
                    - draweeView.getPaddingTop()
                    - draweeView.getPaddingBottom();
        }
        return 0;
    }

    private float getMatrixValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    public Matrix getDrawMatrix() {
        return mMatrix;
    }

    public RectF getDisplayRect() {
        switch (mScrollMode) {
            case MODE_HORIZONTAL:
                checkHorizontalBounds();
                break;
            case MODE_VERTICAL:
                checkVerticalBounds();
                break;
        }
        return getDisplayRect(getDrawMatrix());
    }

    public void checkMatrixAndInvalidate() {
        DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();
        if (draweeView == null) {
            return;
        }
        switch (mScrollMode) {
            case MODE_HORIZONTAL:
                if (checkHorizontalBounds()) {
                    draweeView.invalidate();
                }
                break;
            case MODE_VERTICAL:
                if (checkVerticalBounds()) {
                    draweeView.invalidate();
                }
                break;
        }
    }

    public boolean checkVerticalBounds() {
        RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }

        float height = rect.height();
        float width = rect.width();
        float deltaX = 0.0F;
        float deltaY = 0.0F;

        int viewHeight = getViewHeight();
        if (height <= viewHeight) {
            deltaY = (viewHeight - height) / 2 - rect.top;
            mScrollEdge = EDGE_BOTH;
        } else if (rect.top > 0) {
            deltaY = -rect.top;
            mScrollEdge = EDGE_TOP;
        } else if (rect.bottom < viewHeight) {
            deltaY = viewHeight - rect.bottom;
            mScrollEdge = EDGE_BOTTOM;
        } else {
            mScrollEdge = EDGE_NONE;
        }
        int viewWidth = getViewWidth();
        if (width <= (float) viewWidth) {
            deltaX = (viewWidth - width) / 2 - rect.left;
        } else if (rect.left > 0.0F) {
            deltaX = -rect.left;
        } else if (rect.right < (float) viewWidth) {
            deltaX = viewWidth - rect.right;
        }

        mMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    public boolean checkHorizontalBounds() {
        RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }

        float height = rect.height();
        float width = rect.width();
        float deltaX = 0.0F;
        float deltaY = 0.0F;

        int viewHeight = getViewHeight();
        if (height <= (float) viewHeight) {
            deltaY = (viewHeight - height) / 2 - rect.top;
        } else if (rect.top > 0.0F) {
            deltaY = -rect.top;
        } else if (rect.bottom < (float) viewHeight) {
            deltaY = viewHeight - rect.bottom;
        }
        int viewWidth = getViewWidth();
        if (width <= viewWidth) {
            deltaX = (viewWidth - width) / 2 - rect.left;
            mScrollEdge = EDGE_BOTH;
        } else if (rect.left > 0) {
            deltaX = -rect.left;
            mScrollEdge = EDGE_LEFT;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mScrollEdge = EDGE_RIGHT;
        } else {
            mScrollEdge = EDGE_NONE;
        }

        mMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    private RectF getDisplayRect(Matrix matrix) {
        DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();
        if (draweeView == null || (mImageInfoWidth == -1 && mImageInfoHeight == -1)) {
            return null;
        }
        mDisplayRect.set(0.0F, 0.0F, mImageInfoWidth, mImageInfoHeight);
        draweeView.getHierarchy().getActualImageBounds(mDisplayRect);
        matrix.mapRect(mDisplayRect);
        return mDisplayRect;
    }

    private void updateBaseMatrix() {
        if (mImageInfoWidth == -1 && mImageInfoHeight == -1) {
            return;
        }
        resetMatrix();
    }

    private void resetMatrix() {
        mMatrix.reset();
        switch (mScrollMode) {
            case MODE_HORIZONTAL:
                checkHorizontalBounds();
                break;
            case MODE_VERTICAL:
                checkVerticalBounds();
                break;
        }
        DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();
        if (draweeView != null) {
            draweeView.invalidate();
        }
    }

    private void checkMinScale() {
        DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();
        if (draweeView == null) {
            return;
        }

        if (getScale() < MIN_SCALE) {
            RectF rect = getDisplayRect();
            if (null != rect) {
                draweeView.post(new AnimatedZoomRunnable(getScale(), MIN_SCALE, rect.centerX(),
                        rect.centerY()));
            }
        }
    }

    @Override public boolean onSingleTapConfirmed(MotionEvent e) {
        PhotoDraweeView draweeView = mDraweeView.get();
        if (draweeView == null) {
            return false;
        }
        if (mSingleTapListener != null) {
            mSingleTapListener.onSingleTap(draweeView, e.getX(), e.getY());
            return true;
        }
        return false;
    }

    @Override public boolean onDoubleTap(MotionEvent event) {
        try {
            float scale = getScale();
            float x = event.getX();
            float y = event.getY();

            if (scale < MID_SCALE) {
                setScale(MID_SCALE, x, y, true);
            } else {
                setScale(MIN_SCALE, x, y, true);
            }
        } catch (Exception e) {
            // Can sometimes happen when getX() and getY() is called
        }
        return true;
    }

    @Override public boolean onDoubleTapEvent(MotionEvent event) {
        return false;
    }

    @Override public void onScale(float scaleFactor, float focusX, float focusY) {
        if (getScale() < MAX_SCALE || scaleFactor < 1.0F) {

            if (mScaleChangeListener != null) {
                mScaleChangeListener.onScaleChange(scaleFactor, focusX, focusY);
            }

            mMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            checkMatrixAndInvalidate();
        }
    }

    @Override public void onScaleEnd() {
        checkMinScale();
    }

    @Override public void onDrag(float dx, float dy) {
        DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();

        if (draweeView != null && !mScaleDragDetector.isScaling()) {
            mMatrix.postTranslate(dx, dy);
            checkMatrixAndInvalidate();

            ViewParent parent = draweeView.getParent();
            if (parent == null) {
                return;
            }

            if (mAllowParentInterceptOnEdge
                    && !mScaleDragDetector.isScaling()
                    && !mBlockParentIntercept) {
                switch (mScrollMode) {
                    case MODE_HORIZONTAL:
                        if (mScrollEdge == EDGE_BOTH || (mScrollEdge == EDGE_LEFT && dx >= 1f) || (
                                mScrollEdge == EDGE_RIGHT
                                        && dx <= -1f)) {
                            parent.requestDisallowInterceptTouchEvent(false);
                        }
                        break;
                    case MODE_VERTICAL:
                        if (mScrollEdge == EDGE_BOTH || (mScrollEdge == EDGE_TOP && dy >= 1f) || (
                                mScrollEdge == EDGE_BOTTOM
                                        && dy <= -1f)) {
                            parent.requestDisallowInterceptTouchEvent(false);
                        }
                        break;
                }
            } else {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    @Override public void onFling(float startX, float startY, float velocityX, float velocityY) {
        DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();
        if (draweeView == null) {
            return;
        }
        mCurrentFlingRunnable = new FlingRunnable(draweeView.getContext());
        mCurrentFlingRunnable.fling(getViewWidth(), getViewHeight(), (int) velocityX,
                (int) velocityY);
        draweeView.post(mCurrentFlingRunnable);
    }

    @Override public boolean onTouch(View v, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                ViewParent parent = v.getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                cancelFling();
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                ViewParent parent = v.getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
            }
            break;
        }

        boolean wasScaling = mScaleDragDetector.isScaling();
        boolean wasDragging = mScaleDragDetector.isDragging();

        mScaleDragDetector.onTouchEvent(event);

        boolean noScale = !wasScaling && !mScaleDragDetector.isScaling();
        boolean noDrag = !wasDragging && !mScaleDragDetector.isDragging();
        mBlockParentIntercept = noScale && noDrag;

        mGestureDetector.onTouchEvent(event);

        return true;
    }

    private class AnimatedZoomRunnable implements Runnable {
        private final float mFocalX, mFocalY;
        private final long mStartTime;
        private final float mZoomStart, mZoomEnd;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom,
                final float focalX, final float focalY) {
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override public void run() {

            DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();
            if (draweeView == null) {
                return;
            }

            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getScale();

            onScale(deltaScale, mFocalX, mFocalY);

            if (t < 1f) {
                postOnAnimation(draweeView, this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            t = mZoomInterpolator.getInterpolation(t);
            return t;
        }
    }

    private class FlingRunnable implements Runnable {

        private final ScrollerCompat mScroller;
        private int mCurrentX, mCurrentY;

        public FlingRunnable(Context context) {
            mScroller = ScrollerCompat.create(context);
        }

        public void cancelFling() {
            mScroller.abortAnimation();
        }

        public void fling(int viewWidth, int viewHeight, int velocityX, int velocityY) {
            final RectF rect = getDisplayRect();
            if (null == rect) {
                return;
            }

            final int startX = Math.round(-rect.left);
            final int minX, maxX, minY, maxY;

            if (viewWidth < rect.width()) {
                minX = 0;
                maxX = Math.round(rect.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }

            final int startY = Math.round(-rect.top);
            if (viewHeight < rect.height()) {
                minY = 0;
                maxY = Math.round(rect.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }

            mCurrentX = startX;
            mCurrentY = startY;

            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
            }
        }

        @Override public void run() {
            if (mScroller.isFinished()) {
                return;
            }

            DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();

            if (draweeView != null && mScroller.computeScrollOffset()) {
                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();
                mMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
                draweeView.invalidate();
                mCurrentX = newX;
                mCurrentY = newY;
                postOnAnimation(draweeView, this);
            }
        }
    }

    private void cancelFling() {
        if (mCurrentFlingRunnable != null) {
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }

    private void postOnAnimation(View view, Runnable runnable) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.postOnAnimation(runnable);
        } else {
            view.postDelayed(runnable, 16L);
        }
    }

    protected void onDetachedFromWindow() {
        cancelFling();
    }

}