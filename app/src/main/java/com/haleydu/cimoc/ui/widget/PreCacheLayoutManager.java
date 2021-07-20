package com.haleydu.cimoc.ui.widget;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Hiroshi on 2016/8/13.
 */
public class PreCacheLayoutManager extends LinearLayoutManager {

    private int mExtraSpace = 0;

    public PreCacheLayoutManager(Context context) {
        super(context);
    }

    public PreCacheLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public void setExtraSpace(int extraSpace) {
        mExtraSpace = extraSpace;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        if (mExtraSpace > 0) {
            if (getOrientation() == LinearLayoutManager.HORIZONTAL) {
                return mExtraSpace * getWidth();
            } else {
                return mExtraSpace * getHeight();
            }
        }
        return 0;
    }

}
