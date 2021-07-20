package com.haleydu.cimoc.component;

import androidx.annotation.ColorRes;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public interface ThemeResponsive {

    void onThemeChange(@ColorRes int primary, @ColorRes int accent);

}
