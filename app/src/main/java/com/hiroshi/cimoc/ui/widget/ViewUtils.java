package com.hiroshi.cimoc.ui.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Build;
import android.view.View;

/**
 * Created by Hiroshi on 2016/10/12.
 */

public class ViewUtils {

    public static float dpToPixel(float dp, Context context) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int getStatusBarHeight(Context context) {
        int result = (int) Math.ceil(25 * context.getResources().getDisplayMetrics().density);
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        int result = (int) Math.ceil(48 * context.getResources().getDisplayMetrics().density);
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void postOnAnimation(View view, Runnable runnable) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.postOnAnimation(runnable);
        } else {
            view.postDelayed(runnable, 16L);
        }
    }

    public static float calculateScale(Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        return (float) Math.sqrt((float) Math.pow(values[Matrix.MSCALE_X], 2) +
                (float) Math.pow(values[Matrix.MSKEW_Y], 2));
    }

    public static int getViewWidth(View view) {
        return view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
    }

    public static int getViewHeight(View view) {
        return view.getHeight() - view.getPaddingTop() - view.getPaddingBottom();
    }

}
