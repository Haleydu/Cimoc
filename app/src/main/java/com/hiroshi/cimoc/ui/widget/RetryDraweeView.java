package com.hiroshi.cimoc.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by Hiroshi on 2017/5/27.
 */

public class RetryDraweeView extends SimpleDraweeView {

    public RetryDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public RetryDraweeView(Context context) {
        this(context, null, 0);
    }

    public RetryDraweeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RetryDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean retry() {
        AbstractDraweeController controller = (AbstractDraweeController) getController();
        return controller != null && controller.onClick();
    }

}
