package com.hiroshi.cimoc.component;

import android.support.annotation.ColorRes;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public interface ThemeResponsive {

    void onThemeChange(@ColorRes int primary, @ColorRes int accent);

}
