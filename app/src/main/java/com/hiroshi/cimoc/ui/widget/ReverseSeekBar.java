package com.hiroshi.cimoc.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Created by Hiroshi on 2016/8/13.
 */
public class ReverseSeekBar extends DiscreteSeekBar {

    private boolean isReverse = false;

    public ReverseSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ReverseSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReverseSeekBar(Context context) {
        super(context);
    }

    @Override
    public boolean isRtl() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
        invalidate();
    }

}
