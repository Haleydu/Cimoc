package com.hiroshi.cimoc.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;

/**
 * https://github.com/ongakuer/PhotoDraweeView
 */
public class PhotoDraweeView extends RetryDraweeView implements OnScaleDragGestureListener,
        FlingRunnable.OnFlingRunningListener, GestureDetector.OnDoubleTapListener {

    public static final float MIN_SCALE = 1.0f;
    public static final float MAX_SCALE = 3.0f;

    private static final int EDGE_NONE = -1;
    private static final int EDGE_LEFT = 0;
    private static final int EDGE_RIGHT = 1;
    private static final int EDGE_TOP = 0;
    private static final int EDGE_BOTTOM = 1;
    private static final int EDGE_BOTH = 2;

    public static final int MODE_VERTICAL = 0;
    public static final int MODE_HORIZONTAL = 1;

    private final RectF mDisplayRect = new RectF();

    private ScaleDragDetector mScaleDragDetector;
    private GestureDetectorCompat mGestureDetector;
    private OnTapGestureListener mTapGestureListener;

    private boolean mBlockParentIntercept = false;
    private boolean mAlwaysBlockParent = false;
    private int mScrollEdge = EDGE_BOTH;
    private int mScrollMode = MODE_HORIZONTAL;

    private float mScaleFactor = 2.0f;
    private boolean isDoubleTap = true;

    private final Matrix mMatrix = new Matrix();
    private FlingRunnable mCurrentFlingRunnable;

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
                if (mTapGestureListener != null) {
                    mTapGestureListener.onLongPress(e.getRawX(), e.getRawY());
                }
            }
        });
        mGestureDetector.setOnDoubleTapListener(this);
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
        int count = canvas.save();
        canvas.concat(mMatrix);
        super.onDraw(canvas);
        canvas.restoreToCount(count);
    }

    public void update(int id) {
        Object tag = getTag();
        if (tag == null || (int) tag != id) {
            setTag(id);
            resetMatrix();
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mTapGestureListener != null) {
            mTapGestureListener.onSingleTap(e.getRawX(), e.getRawY());
            return true;
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        if (isDoubleTap) {
            try {
                float scale = ViewUtils.calculateScale(mMatrix);
                float x = event.getX();
                float y = event.getY();

                setScale(scale < mScaleFactor ? mScaleFactor : MIN_SCALE, x, y);
            } catch (Exception e) {
                // Can sometimes happen when getX() and getY() is called
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return false;
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        if (ViewUtils.calculateScale(mMatrix) < MAX_SCALE || scaleFactor < 1.0F) {
            mMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            checkMatrixAndInvalidate();
        }
    }

    @Override
    public void onScaleEnd() {
        if (ViewUtils.calculateScale(mMatrix) < MIN_SCALE) {
            RectF rect = checkAndGetDisplayRect();
            post(new AnimatedScaleRunnable(MIN_SCALE, rect.centerX(), rect.centerY(), this, mMatrix, this));
        }
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

            if (mAlwaysBlockParent) {
                parent.requestDisallowInterceptTouchEvent(mScrollEdge != EDGE_BOTH);
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
        mCurrentFlingRunnable = new FlingRunnable(getContext(), this, this);
        mCurrentFlingRunnable.fling(checkAndGetDisplayRect(), ViewUtils.getViewWidth(this),
                ViewUtils.getViewHeight(this), (int) velocityX, (int) velocityY);
        post(mCurrentFlingRunnable);
    }

    @Override
    public void onFlingRunning(int dx, int dy) {
        mMatrix.postTranslate(dx, dy);
        invalidate();
    }

    private void setScale(float scale, float focalX, float focalY) {
        if (scale < MIN_SCALE || scale > MAX_SCALE) {
            return;
        }

        post(new AnimatedScaleRunnable(scale, focalX, focalY, this, mMatrix, this));
    }

    public void setScaleFactor(float factor) {
        mScaleFactor = factor;
    }

    public void setDoubleTap(boolean enable) {
        isDoubleTap = enable;
    }

    public void setAlwaysBlockParent(boolean block) {
        mAlwaysBlockParent = block;
    }

    public void setScrollMode(int mode) {
        mScrollMode = mode;
    }

    public void setTapListenerListener(OnTapGestureListener listener) {
        mTapGestureListener = listener;
    }

    public RectF checkAndGetDisplayRect() {
        switch (mScrollMode) {
            case MODE_HORIZONTAL:
                checkHorizontalBounds();
                break;
            case MODE_VERTICAL:
                checkVerticalBounds();
                break;
        }
        return getDisplayRect();
    }

    private RectF getDisplayRect() {
        getHierarchy().getActualImageBounds(mDisplayRect);
        mMatrix.mapRect(mDisplayRect);
        return mDisplayRect;
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
        RectF rect = getDisplayRect();

        final float height = rect.height(), width = rect.width();
        final int viewHeight = ViewUtils.getViewHeight(this);
        final int viewWidth = ViewUtils.getViewWidth(this);
        float deltaX = 0.0F, deltaY = 0.0F;

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
        RectF rect = getDisplayRect();

        final float height = rect.height(), width = rect.width();
        final int viewHeight = ViewUtils.getViewHeight(this);
        final int viewWidth = ViewUtils.getViewWidth(this);
        float deltaX = 0.0F, deltaY = 0.0F;

        if (height <= (float) viewHeight) {
            deltaY = (viewHeight - height) / 2 - rect.top;
        } else if (rect.top > 0.0F) {
            deltaY = -rect.top;
        } else if (rect.bottom < (float) viewHeight) {
            deltaY = viewHeight - rect.bottom;
        }

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

    private void cancelFling() {
        if (mCurrentFlingRunnable != null) {
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }

}
