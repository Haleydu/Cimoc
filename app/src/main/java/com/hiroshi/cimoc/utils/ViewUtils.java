package com.hiroshi.cimoc.utils;

import android.content.Context;

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

}
