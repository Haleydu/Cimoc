package com.hiroshi.cimoc.ui.custom.photo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * https://github.com/ongakuer/PhotoDraweeView
 */
public class PhotoDraweeView extends SimpleDraweeView implements OnScaleDragGestureListener, GestureDetector.OnDoubleTapListener {

    public static final float MIN_SCALE = 1.0f;
    public static final float MID_SCALE = 2.0f;
    public static final float MAX_SCALE = 3.0f;

    private static final int EDGE_NONE = -1;
    private static final int EDGE_LEFT = 0;
    private static final int EDGE_RIGHT = 1;
    private static final int EDGE_TOP = 0;
    private static final int EDGE_BOTTOM = 1;
    private static final int EDGE_BOTH = 2;

    public static final int MODE_VERTICAL = 0;
    public static final int MODE_HORIZONTAL = 1;

    private final float[] mMatrixValues = new float[9];
    private final RectF mDisplayRect = new RectF();
    private final Interpolator mZoomInterpolator = new AccelerateDecelerateInterpolator();

    private ScaleDragDetector mScaleDragDetector;
    private GestureDetectorCompat mGestureDetector;

    private boolean mBlockParentIntercept = false;
    private int mScrollEdge = EDGE_BOTH;
    private int mScrollMode = MODE_HORIZONTAL;

    private final Matrix mMatrix = new Matrix();
    private int mImageInfoHeight = -1, mImageInfoWidth = -1;
    private FlingRunnable mCurrentFlingRunnable;

    private OnSingleTapListener mSingleTapListener;
    private OnLongPressListener mOnLongPressListener;

    public PhotoDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init();
    }

    public PhotoDraweeView(Context context) {
        this(context, null, 0);
    }

    public PhotoDraweeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelFling();
    }

    protected void init() {
        getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);

        mScaleDragDetector = new ScaleDragDetector(getContext(), this);
        mGestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (mOnLongPressListener != null) {
                    mOnLongPressListener.onLongPress(PhotoDraweeView.this, e.getRawX(), e.getRawY());
                }
            }
        });
        mGestureDetector.setOnDoubleTapListener(this);
    }

    public boolean retry() {
        AbstractDraweeController controller = (AbstractDraweeController) getController();
        return controller != null && controller.onClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        ViewParent parent = getParent();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                cancelFling();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
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

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        canvas.concat(mMatrix);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    public void update(int id, int imageInfoWidth, int imageInfoHeight) {
        Object tag = getTag();
        if (tag == null || (int) tag != id) {
            setTag(id);
            mImageInfoWidth = imageInfoWidth;
            mImageInfoHeight = imageInfoHeight;
            updateBaseMatrix();
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mSingleTapListener != null) {
            mSingleTapListener.onSingleTap(this, e.getRawX(), e.getRawY());
            return true;
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        try {
            float scale = getScale();
            float x = event.getX();
            float y = event.getY();

            setScale(scale < MID_SCALE ? MID_SCALE : MIN_SCALE, x, y);
        } catch (Exception e) {
            // Can sometimes happen when getX() and getY() is called
        }
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return false;
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        if (getScale() < MAX_SCALE || scaleFactor < 1.0F) {
            mMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            checkMatrixAndInvalidate();
        }
    }

    @Override
    public void onScaleEnd() {
        checkMinScale();
    }

    @Override
    public void onDrag(float dx, float dy) {
        if (!mScaleDragDetector.isScaling()) {
            mMatrix.postTranslate(dx, dy);
            checkMatrixAndInvalidate();

            ViewParent parent = getParent();
            if (parent == null) {
                return;
            }

            if (!mScaleDragDetector.isScaling() && !mBlockParentIntercept) {
                switch (mScrollMode) {
                    case MODE_HORIZONTAL:
                        if (mScrollEdge == EDGE_BOTH || (mScrollEdge == EDGE_LEFT && dx >= 1f) || (
                                mScrollEdge == EDGE_RIGHT && dx <= -1f)) {
                            parent.requestDisallowInterceptTouchEvent(false);
                        }
                        break;
                    case MODE_VERTICAL:
                        if (mScrollEdge == EDGE_BOTH || (mScrollEdge == EDGE_TOP && dy >= 1f) || (
                                mScrollEdge == EDGE_BOTTOM && dy <= -1f)) {
                            parent.requestDisallowInterceptTouchEvent(false);
                        }
                        break;
                }
            } else {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY) {
        mCurrentFlingRunnable = new FlingRunnable(getContext());
        mCurrentFlingRunnable.fling(getViewWidth(), getViewHeight(), (int) velocityX,
                (int) velocityY);
        post(mCurrentFlingRunnable);
    }

    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getMatrixValue(mMatrix, Matrix.MSCALE_X), 2) +
                (float) Math.pow(getMatrixValue(mMatrix, Matrix.MSKEW_Y), 2));
    }

    private void setScale(float scale, float focalX, float focalY) {
        if (scale < MIN_SCALE || scale > MAX_SCALE) {
            return;
        }

        post(new AnimatedZoomRunnable(getScale(), scale, focalX, focalY));
    }

    public void setScrollMode(int mode) {
        mScrollMode = mode;
    }

    public void setOnSingleTapListener(OnSingleTapListener listener) {
        mSingleTapListener = listener;
    }

    public void setOnLongPressListener(OnLongPressListener listener) {
        mOnLongPressListener = listener;
    }

    private int getViewWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getViewHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private float getMatrixValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
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
        return getDisplayRect(mMatrix);
    }

    public void checkMatrixAndInvalidate() {
        switch (mScrollMode) {
            case MODE_HORIZONTAL:
                if (checkHorizontalBounds()) {
                    invalidate();
                }
                break;
            case MODE_VERTICAL:
                if (checkVerticalBounds()) {
                    invalidate();
                }
                break;
        }
    }

    public boolean checkVerticalBounds() {
        RectF rect = getDisplayRect(mMatrix);
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
        RectF rect = getDisplayRect(mMatrix);
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
        if (mImageInfoWidth == -1 && mImageInfoHeight == -1) {
            return null;
        }
        mDisplayRect.set(0.0F, 0.0F, mImageInfoWidth, mImageInfoHeight);
        getHierarchy().getActualImageBounds(mDisplayRect);
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
        invalidate();
    }

    private void checkMinScale() {
        if (getScale() < MIN_SCALE) {
            RectF rect = getDisplayRect();
            if (null != rect) {
                post(new AnimatedZoomRunnable(getScale(), MIN_SCALE, rect.centerX(), rect.centerY()));
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
            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getScale();

            onScale(deltaScale, mFocalX, mFocalY);

            if (t < 1f) {
                postOnAnimation(PhotoDraweeView.this, this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / 200;
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

            if (mScroller.computeScrollOffset()) {
                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();
                mMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
                invalidate();
                mCurrentX = newX;
                mCurrentY = newY;
                postOnAnimation(PhotoDraweeView.this, this);
            }
        }
    }

    public interface OnSingleTapListener {
        void onSingleTap(PhotoDraweeView draweeView, float x, float y);
    }

    public interface OnLongPressListener {
        void onLongPress(PhotoDraweeView draweeView, float x, float y);
    }

}
