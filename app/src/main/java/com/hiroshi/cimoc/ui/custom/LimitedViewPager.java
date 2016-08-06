package com.hiroshi.cimoc.ui.custom;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Hiroshi on 2016/7/29.
 */
public class LimitedViewPager extends ViewPager {

    public static final int LIMIT_NONE = 0;
    public static final int LIMIT_LEFT = 1;
    public static final int LIMIT_RIGHT = 2;
    public static final int LIMIT_BOTH = 3;

    private int limit;

    public LimitedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LimitedViewPager(Context context) {
        super(context);
        init();
    }

    private void init() {
        limit = LIMIT_BOTH;
    }

    private float lastX;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (limit == LIMIT_NONE){
            return super.dispatchTouchEvent(ev);
        } else if (limit == LIMIT_BOTH) {
            return true;
        } else {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = ev.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float value = ev.getX() - lastX;
                    if (limit == LIMIT_LEFT && value < 0) {
                        return true;
                    }
                    if (limit == LIMIT_RIGHT && value > 0) {
                        return true;
                    }
                    break;
            }
            return super.dispatchTouchEvent(ev);
        }

    }

    public void nextPage() {
        if (limit != LIMIT_LEFT && limit != LIMIT_BOTH) {
            setCurrentItem(getCurrentItem() + 1);
        }
    }

    public void prevPage() {
        if (limit != LIMIT_RIGHT && limit != LIMIT_BOTH) {
            setCurrentItem(getCurrentItem() - 1);
        }
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

}
