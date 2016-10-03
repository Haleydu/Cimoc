package com.hiroshi.cimoc.utils;

import android.content.Context;
import android.util.TypedValue;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/10/2.
 */

public class ThemeUtils {

    public static final int THEME_BLUE = 0;
    public static final int THEME_GREY = 1;
    public static final int THEME_TEAL = 2;
    public static final int THEME_PURPLE = 3;
    public static final int THEME_PINK = 4;
    public static final int THEME_BROWN = 5;

    public static int getResourceId(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.resourceId;
    }

    public static int getThemeById(int id) {
        switch (id) {
            default:
            case THEME_BLUE:
                return R.style.AppThemeBlue;
            case THEME_GREY:
                return R.style.AppThemeGrey;
            case THEME_TEAL:
                return R.style.AppThemeTeal;
            case THEME_PURPLE:
                return R.style.AppThemePurple;
            case THEME_PINK:
                return R.style.AppThemePink;
            case THEME_BROWN:
                return R.style.AppThemeBrown;
        }
    }

}
