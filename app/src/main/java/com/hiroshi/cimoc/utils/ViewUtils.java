package com.hiroshi.cimoc.utils;

import android.content.Context;

/**
 * Created by Hiroshi on 2016/10/12.
 */

public class ViewUtils {

    public static float dpToPixel(float dp, Context context) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}
